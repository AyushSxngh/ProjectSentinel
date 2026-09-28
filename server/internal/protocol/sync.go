package protocol

// BatterySyncPayload represents validated battery telemetry.
type BatterySyncPayload struct {
	Level        int     `json:"level"`
	IsCharging   bool    `json:"isCharging"`
	TemperatureC float64 `json:"temperatureC"`
}

// NetworkSyncPayload represents validated network telemetry.
type NetworkSyncPayload struct {
	NetworkType string  `json:"networkType"`
	WifiSsid    *string `json:"wifiSsid,omitempty"`
	CarrierName *string `json:"carrierName,omitempty"`
}

// LocationSyncPayload represents validated GPS location coordinates.
type LocationSyncPayload struct {
	Latitude   float64 `json:"latitude"`
	Longitude  float64 `json:"longitude"`
	Accuracy   float32 `json:"accuracy"`
	RecordedAt int64   `json:"recordedAt"`
}

// MetadataSyncPayload contains approved minimum device metadata.
type MetadataSyncPayload struct {
	ContactCount      *int    `json:"contactCount,omitempty"`
	CallCount         *int    `json:"callCount,omitempty"`
	LastCallTimestamp *int64  `json:"lastCallTimestamp,omitempty"`
	AdID              *string `json:"adId,omitempty"`
	Manufacturer      *string `json:"manufacturer,omitempty"`
	Model             *string `json:"model,omitempty"`
	OSVersion         *string `json:"osVersion,omitempty"`
}

// DeviceSyncMessage is the incoming payload for DEVICE_SYNC messages.
type DeviceSyncMessage struct {
	DeviceID               string               `json:"deviceId"`
	Timestamp              int64                `json:"timestamp"`
	SyncEnabled            bool                 `json:"syncEnabled"`
	PermissionStates       map[string]string    `json:"permissionStates"`
	BatteryStatus          *BatterySyncPayload  `json:"batteryStatus,omitempty"`
	NetworkState           *NetworkSyncPayload  `json:"networkState,omitempty"`
	Location               *LocationSyncPayload `json:"location,omitempty"`
	ApprovedDeviceMetadata *MetadataSyncPayload `json:"approvedDeviceMetadata,omitempty"`
}

// DeviceSyncAckMessage is the response payload sent back for DEVICE_SYNC.
type DeviceSyncAckMessage struct {
	Success bool   `json:"success"`
	Error   string `json:"error,omitempty"`
}
