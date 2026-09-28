package sync

import (
	"context"
	"errors"
	"strings"
	"time"

	"github.com/xaiop/project-sentinel/server/internal/protocol"
	"github.com/xaiop/project-sentinel/server/internal/repository"
)

var (
	ErrUnauthorizedDevice = errors.New("device id does not match authenticated session")
	ErrInvalidTimestamp   = errors.New("invalid sync timestamp")
	ErrMalformedPayload   = errors.New("malformed device sync payload")
)

type Broadcaster interface {
	BroadcastToAdmins(payload []byte)
}

type Service struct {
	repo        repository.SyncRepository
	broadcaster Broadcaster
}

func NewService(repo repository.SyncRepository, broadcaster Broadcaster) *Service {
	return &Service{
		repo:        repo,
		broadcaster: broadcaster,
	}
}

func (s *Service) SetBroadcaster(broadcaster Broadcaster) {
	s.broadcaster = broadcaster
}

// ProcessSync validates and stores the incoming sync payload.
func (s *Service) ProcessSync(ctx context.Context, authenticatedDeviceID string, msg *protocol.DeviceSyncMessage) error {
	if msg == nil {
		return ErrMalformedPayload
	}

	// 1. Validate DeviceID matches authenticated session
	if strings.TrimSpace(msg.DeviceID) == "" || msg.DeviceID != authenticatedDeviceID {
		return ErrUnauthorizedDevice
	}

	// 2. Validate timestamp
	now := time.Now().Unix()
	if msg.Timestamp <= 0 || msg.Timestamp > now+300 {
		return ErrInvalidTimestamp
	}

	// 3. User Privacy Check: If sync is disabled by user, acknowledge and do not store sensitive telemetry
	if !msg.SyncEnabled {
		snapshot := repository.DeviceSyncSnapshot{
			DeviceID:         msg.DeviceID,
			Timestamp:        msg.Timestamp,
			SyncEnabled:      false,
			PermissionStates: msg.PermissionStates,
			ReceivedAt:       time.Now().UTC(),
		}
		if s.repo != nil {
			_ = s.repo.SaveLatest(ctx, snapshot)
		}
		return nil
	}

	// 4. Validate allowed fields against granted permissions
	// Location only if location permission granted
	var location *protocol.LocationSyncPayload
	if msg.Location != nil && msg.PermissionStates["location"] == "granted" {
		location = msg.Location
	}

	// Metadata: filter according to specific permissions
	var metadata *protocol.MetadataSyncPayload
	if msg.ApprovedDeviceMetadata != nil {
		meta := *msg.ApprovedDeviceMetadata
		if msg.PermissionStates["contacts"] != "granted" {
			meta.ContactCount = nil
		}
		if msg.PermissionStates["callLog"] != "granted" {
			meta.CallCount = nil
			meta.LastCallTimestamp = nil
		}
		if msg.PermissionStates["phoneState"] != "granted" && msg.NetworkState != nil {
			msg.NetworkState.CarrierName = nil
		}
		metadata = &meta
	}

	snapshot := repository.DeviceSyncSnapshot{
		DeviceID:               msg.DeviceID,
		Timestamp:              msg.Timestamp,
		SyncEnabled:            true,
		PermissionStates:       msg.PermissionStates,
		BatteryStatus:          msg.BatteryStatus,
		NetworkState:           msg.NetworkState,
		Location:               location,
		ApprovedDeviceMetadata: metadata,
		ReceivedAt:             time.Now().UTC(),
	}

	if s.repo != nil {
		if err := s.repo.SaveLatest(ctx, snapshot); err != nil {
			return err
		}
	}

	// Broadcast update to authenticated admins
	if s.broadcaster != nil {
		updateMsg := protocol.DeviceUpdateMessage{
			Event:    "sync",
			DeviceID: msg.DeviceID,
		}
		if msg.BatteryStatus != nil {
			updateMsg.Battery = &msg.BatteryStatus.Level
		}
		if msg.NetworkState != nil {
			updateMsg.Network = &msg.NetworkState.NetworkType
		}
		if location != nil {
			updateMsg.Latitude = &location.Latitude
			updateMsg.Longitude = &location.Longitude
			updateMsg.Accuracy = &location.Accuracy
		}
		env, err := protocol.NewMessage(protocol.TypeDeviceUpdate, 0, updateMsg)
		if err == nil {
			if payloadBytes, err := json.Marshal(env); err == nil {
				s.broadcaster.BroadcastToAdmins(payloadBytes)
			}
		}
	}

	return nil
}

func (s *Service) GetSync(ctx context.Context, deviceID string) (repository.DeviceSyncSnapshot, bool, error) {
	if s.repo == nil {
		return repository.DeviceSyncSnapshot{}, false, nil
	}
	return s.repo.GetLatest(ctx, deviceID)
}
