package sync

import (
	"context"
	"encoding/json"
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
		var snapshot repository.DeviceSyncSnapshot
		if s.repo != nil {
			if existing, found, _ := s.repo.GetLatest(ctx, msg.DeviceID); found {
				snapshot = existing
				snapshot.Timestamp = msg.Timestamp
				snapshot.SyncEnabled = false
				snapshot.PermissionStates = msg.PermissionStates
				if len(msg.Permissions) > 0 {
					snapshot.Permissions = msg.Permissions
				}
				if msg.LastSyncFailureTime != nil {
					snapshot.LastSyncFailureTime = msg.LastSyncFailureTime
				}
				snapshot.ReceivedAt = time.Now().UTC()
			} else {
				snapshot = repository.DeviceSyncSnapshot{
					DeviceID:            msg.DeviceID,
					Timestamp:           msg.Timestamp,
					SyncEnabled:         false,
					PermissionStates:    msg.PermissionStates,
					Permissions:         msg.Permissions,
					LastSyncFailureTime: msg.LastSyncFailureTime,
					ReceivedAt:          time.Now().UTC(),
				}
			}
			_ = s.repo.SaveLatest(ctx, snapshot)
		}
		return nil
	}

	// 4. Validate allowed fields against granted permissions
	// Location only if location permission granted
	var location *protocol.LocationSyncPayload
	isLocationGranted := msg.PermissionStates["location"] == "granted" ||
		msg.PermissionStates["ACCESS_FINE_LOCATION"] == "granted" ||
		msg.PermissionStates["ACCESS_COARSE_LOCATION"] == "granted"
	if msg.Location != nil && isLocationGranted {
		location = msg.Location
	}

	// Call Logs: only if READ_CALL_LOG granted
	isCallLogGranted := msg.PermissionStates["callLog"] == "granted" ||
		msg.PermissionStates["READ_CALL_LOG"] == "granted"
	var callLogs []protocol.CallLogRecord
	if isCallLogGranted {
		callLogs = msg.CallLogs
	} else if s.repo != nil {
		// If permission is denied, keep previously retained records
		if existing, found, _ := s.repo.GetLatest(ctx, msg.DeviceID); found {
			callLogs = existing.CallLogs
		}
	}

	// Metadata: filter according to specific permissions
	var metadata *protocol.MetadataSyncPayload
	if msg.ApprovedDeviceMetadata != nil {
		meta := *msg.ApprovedDeviceMetadata
		isContactsGranted := msg.PermissionStates["contacts"] == "granted" ||
			msg.PermissionStates["READ_CONTACTS"] == "granted"
		if !isContactsGranted {
			meta.ContactCount = nil
		}
		if !isCallLogGranted {
			meta.CallCount = nil
			meta.LastCallTimestamp = nil
		}
		isPhoneStateGranted := msg.PermissionStates["phoneState"] == "granted" ||
			msg.PermissionStates["READ_PHONE_STATE"] == "granted"
		if !isPhoneStateGranted && msg.NetworkState != nil {
			msg.NetworkState.CarrierName = nil
		}
		metadata = &meta
	}

	snapshot := repository.DeviceSyncSnapshot{
		DeviceID:               msg.DeviceID,
		Timestamp:              msg.Timestamp,
		SyncEnabled:            true,
		PermissionStates:       msg.PermissionStates,
		Permissions:            msg.Permissions,
		BatteryStatus:          msg.BatteryStatus,
		NetworkState:           msg.NetworkState,
		Location:               location,
		ApprovedDeviceMetadata: metadata,
		CallLogs:               callLogs,
		SystemUptimeSeconds:    msg.SystemUptimeSeconds,
		LastSyncSuccessTime:    msg.LastSyncSuccessTime,
		LastSyncFailureTime:    msg.LastSyncFailureTime,
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
