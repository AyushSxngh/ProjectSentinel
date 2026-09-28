package repository

import (
	"context"
	"encoding/json"
	"fmt"
	"sync"
	"time"
)

// DefaultLocationTTL is the Redis expiration for latest locations.
const DefaultLocationTTL = 5 * time.Minute

// Location contains the latest validated location state for a device.
type Location struct {
	DeviceID   string    `json:"deviceId"`
	Latitude   float64   `json:"latitude"`
	Longitude  float64   `json:"longitude"`
	Accuracy   float32   `json:"accuracy"`
	Battery    int       `json:"battery"`
	Network    string    `json:"network"`
	RecordedAt time.Time `json:"recordedAt"`
}

// LocationRepository stores realtime device locations.
type LocationRepository interface {
	SaveLatest(ctx context.Context, location Location) error
}

// RedisStore is the Redis operation subset required by location persistence.
type RedisStore interface {
	Set(ctx context.Context, key string, value []byte, ttl time.Duration) error
	Get(ctx context.Context, key string) ([]byte, bool, error)
}

// RedisLocationRepository stores latest locations in Redis with in-memory fallback.
type RedisLocationRepository struct {
	client  RedisStore
	ttl     time.Duration
	mu      sync.RWMutex
	inMemDb map[string]Location
}

// NewRedisLocationRepository creates a Redis location repository.
func NewRedisLocationRepository(client RedisStore, ttl time.Duration) *RedisLocationRepository {
	if ttl <= 0 {
		ttl = DefaultLocationTTL
	}

	return &RedisLocationRepository{
		client:  client,
		ttl:     ttl,
		inMemDb: make(map[string]Location),
	}
}

// SaveLatest stores the latest location for a device.
func (r *RedisLocationRepository) SaveLatest(ctx context.Context, location Location) error {
	if r == nil {
		return nil
	}

	r.mu.Lock()
	r.inMemDb[location.DeviceID] = location
	r.mu.Unlock()

	if r.client == nil {
		return nil
	}

	payload, err := json.Marshal(location)
	if err != nil {
		return fmt.Errorf("marshal latest location: %w", err)
	}

	// Attempt redis save; non-fatal if redis is unavailable
	_ = r.client.Set(ctx, locationKey(location.DeviceID), payload, r.ttl)

	return nil
}

// GetLatest returns the latest stored location for a device.
func (r *RedisLocationRepository) GetLatest(ctx context.Context, deviceID string) (Location, bool, error) {
	if r == nil {
		return Location{}, false, nil
	}

	if r.client != nil {
		payload, found, err := r.client.Get(ctx, locationKey(deviceID))
		if err == nil && found {
			var location Location
			if err := json.Unmarshal(payload, &location); err == nil {
				return location, true, nil
			}
		}
	}

	r.mu.RLock()
	defer r.mu.RUnlock()
	location, found := r.inMemDb[deviceID]
	return location, found, nil
}

func locationKey(deviceID string) string {
	return "location:" + deviceID
}
