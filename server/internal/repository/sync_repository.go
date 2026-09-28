package repository

import (
	"context"
	"encoding/json"
	"fmt"
	"sync"
	"time"

	"github.com/xaiop/project-sentinel/server/internal/protocol"
)

// DefaultSyncTTL is the Redis expiration for device sync snapshots.
const DefaultSyncTTL = 24 * time.Hour

// DeviceSyncSnapshot contains the latest validated device sync state.
type DeviceSyncSnapshot struct {
	DeviceID               string                            `json:"deviceId"`
	Timestamp              int64                             `json:"timestamp"`
	SyncEnabled            bool                              `json:"syncEnabled"`
	PermissionStates       map[string]string                 `json:"permissionStates"`
	Permissions            []protocol.PermissionStatusRecord `json:"permissions,omitempty"`
	BatteryStatus          *protocol.BatterySyncPayload      `json:"batteryStatus,omitempty"`
	NetworkState           *protocol.NetworkSyncPayload      `json:"networkState,omitempty"`
	Location               *protocol.LocationSyncPayload     `json:"location,omitempty"`
	ApprovedDeviceMetadata *protocol.MetadataSyncPayload     `json:"approvedDeviceMetadata,omitempty"`
	CallLogs               []protocol.CallLogRecord          `json:"callLogs,omitempty"`
	SystemUptimeSeconds    *int64                            `json:"systemUptimeSeconds,omitempty"`
	LastSyncSuccessTime    *int64                            `json:"lastSyncSuccessTime,omitempty"`
	LastSyncFailureTime    *int64                            `json:"lastSyncFailureTime,omitempty"`
	ReceivedAt             time.Time                         `json:"receivedAt"`
}

// SyncRepository persists and retrieves device sync snapshots.
type SyncRepository interface {
	SaveLatest(ctx context.Context, snapshot DeviceSyncSnapshot) error
	GetLatest(ctx context.Context, deviceID string) (DeviceSyncSnapshot, bool, error)
}

// RedisSyncRepository stores device sync snapshots in Redis with in-memory fallback.
type RedisSyncRepository struct {
	client  RedisStore
	ttl     time.Duration
	mu      sync.RWMutex
	inMemDb map[string]DeviceSyncSnapshot
}

// NewRedisSyncRepository creates a sync repository.
func NewRedisSyncRepository(client RedisStore, ttl time.Duration) *RedisSyncRepository {
	if ttl <= 0 {
		ttl = DefaultSyncTTL
	}

	return &RedisSyncRepository{
		client:  client,
		ttl:     ttl,
		inMemDb: make(map[string]DeviceSyncSnapshot),
	}
}

func (r *RedisSyncRepository) SaveLatest(ctx context.Context, snapshot DeviceSyncSnapshot) error {
	r.mu.Lock()
	r.inMemDb[snapshot.DeviceID] = snapshot
	r.mu.Unlock()

	if r.client == nil {
		return nil
	}

	payload, err := json.Marshal(snapshot)
	if err != nil {
		return fmt.Errorf("marshal sync snapshot: %w", err)
	}

	// Attempt redis save; non-fatal if redis is down
	_ = r.client.Set(ctx, syncKey(snapshot.DeviceID), payload, r.ttl)

	return nil
}

func (r *RedisSyncRepository) GetLatest(ctx context.Context, deviceID string) (DeviceSyncSnapshot, bool, error) {
	if r.client != nil {
		payload, found, err := r.client.Get(ctx, syncKey(deviceID))
		if err == nil && found {
			var snapshot DeviceSyncSnapshot
			if err := json.Unmarshal(payload, &snapshot); err == nil {
				return snapshot, true, nil
			}
		}
	}

	r.mu.RLock()
	defer r.mu.RUnlock()
	snapshot, found := r.inMemDb[deviceID]
	return snapshot, found, nil
}

func syncKey(deviceID string) string {
	return "sync:" + deviceID
}
