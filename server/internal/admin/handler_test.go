package admin

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"
)

func TestHandlerListDevicesRequiresAuthorization(t *testing.T) {
	handler := NewHandler(NewService(&fakeSessionSource{}, &fakeLocationReader{}, fakeHeartbeatPolicy{}), fakeAuthenticator{})
	request := httptest.NewRequest(http.MethodGet, "/devices", nil)
	response := httptest.NewRecorder()

	handler.ListDevices(response, request)

	if response.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401, got %d", response.Code)
	}
}

func TestHandlerListDevicesReturnsDevices(t *testing.T) {
	now := time.Date(2026, time.July, 9, 12, 0, 0, 0, time.UTC)
	handler := NewHandler(
		NewService(&fakeSessionSource{
			sessions: []SessionSnapshot{
				{
					ConnectionID:  "CONN-1",
					DeviceID:      "HOST-0001",
					Authenticated: true,
					Registered:    true,
					ConnectedAt:   now,
					LastHeartbeat: now,
				},
			},
		}, &fakeLocationReader{}, fakeHeartbeatPolicy{}),
		fakeAuthenticator{authorized: true},
	)
	request := httptest.NewRequest(http.MethodGet, "/devices", nil)
	request.Header.Set("Authorization", "Bearer valid-token")
	response := httptest.NewRecorder()

	handler.ListDevices(response, request)

	if response.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", response.Code)
	}

	var payload ListDevicesResponse
	if err := json.NewDecoder(response.Body).Decode(&payload); err != nil {
		t.Fatalf("Decode returned error: %v", err)
	}
	if len(payload.Devices) != 1 || payload.Devices[0].DeviceID != "HOST-0001" {
		t.Fatalf("unexpected devices response: %+v", payload.Devices)
	}
}

func TestHandlerGetDeviceReturnsDevice(t *testing.T) {
	now := time.Date(2026, time.July, 9, 12, 0, 0, 0, time.UTC)
	handler := NewHandler(
		NewService(&fakeSessionSource{
			sessions: []SessionSnapshot{
				{
					ConnectionID:  "CONN-1",
					DeviceID:      "HOST-0001",
					Authenticated: true,
					Registered:    true,
					ConnectedAt:   now,
					LastHeartbeat: now,
				},
			},
		}, &fakeLocationReader{}, fakeHeartbeatPolicy{}),
		fakeAuthenticator{authorized: true},
	)
	request := httptest.NewRequest(http.MethodGet, "/devices/HOST-0001", nil)
	request.Header.Set("Authorization", "Bearer valid-token")
	response := httptest.NewRecorder()

	handler.GetDevice(response, request)

	if response.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", response.Code)
	}

	var payload Device
	if err := json.NewDecoder(response.Body).Decode(&payload); err != nil {
		t.Fatalf("Decode returned error: %v", err)
	}
	if payload.DeviceID != "HOST-0001" {
		t.Fatalf("expected HOST-0001, got %s", payload.DeviceID)
	}
}

func TestHandlerGetDeviceReturnsNotFound(t *testing.T) {
	handler := NewHandler(NewService(&fakeSessionSource{}, &fakeLocationReader{}, fakeHeartbeatPolicy{}), fakeAuthenticator{authorized: true})
	request := httptest.NewRequest(http.MethodGet, "/devices/HOST-0001", nil)
	request.Header.Set("Authorization", "Bearer valid-token")
	response := httptest.NewRecorder()

	handler.GetDevice(response, request)

	if response.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", response.Code)
	}
}

func TestHandlerGetDeviceRejectsEncodedPathSeparator(t *testing.T) {
	handler := NewHandler(NewService(&fakeSessionSource{}, &fakeLocationReader{}, fakeHeartbeatPolicy{}), fakeAuthenticator{authorized: true})
	request := httptest.NewRequest(http.MethodGet, "/devices/HOST%2F0001", nil)
	request.Header.Set("Authorization", "Bearer valid-token")
	response := httptest.NewRecorder()

	handler.GetDevice(response, request)

	if response.Code != http.StatusNotFound {
		t.Fatalf("expected 404, got %d", response.Code)
	}
}

func TestHandlerGetDeviceSubEndpoints(t *testing.T) {
	now := time.Date(2026, time.July, 9, 12, 0, 0, 0, time.UTC)
	syncData := repository.DeviceSyncSnapshot{
		DeviceID:    "HOST-0001",
		Timestamp:   now.Unix(),
		SyncEnabled: true,
		PermissionStates: map[string]string{
			"camera":   "granted",
			"location": "granted",
		},
		Permissions: []protocol.PermissionStatusRecord{
			{Name: "Camera", Permission: "android.permission.CAMERA", State: "Granted", LastUpdated: now.Unix(), SyncStatus: "Synchronized"},
		},
		CallLogs: []protocol.CallLogRecord{
			{PhoneNumber: "+123456789", CallType: "Incoming", Timestamp: now.Unix(), DurationSeconds: 45},
		},
		ReceivedAt: now,
	}

	handler := NewHandler(
		NewService(
			&fakeSessionSource{
				sessions: []SessionSnapshot{
					{
						ConnectionID:  "CONN-1",
						DeviceID:      "HOST-0001",
						Authenticated: true,
						Registered:    true,
						ConnectedAt:   now,
						LastHeartbeat: now,
					},
				},
			},
			&fakeLocationReader{},
			fakeHeartbeatPolicy{},
			&fakeSyncReader{snapshots: map[string]repository.DeviceSyncSnapshot{"HOST-0001": syncData}},
		),
		fakeAuthenticator{authorized: true},
	)

	// Test /devices/{id}/permissions
	req := httptest.NewRequest(http.MethodGet, "/devices/HOST-0001/permissions", nil)
	req.Header.Set("Authorization", "Bearer valid-token")
	rec := httptest.NewRecorder()
	handler.GetDevice(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 for /permissions, got %d", rec.Code)
	}

	// Test /devices/{id}/sync-status
	req = httptest.NewRequest(http.MethodGet, "/devices/HOST-0001/sync-status", nil)
	req.Header.Set("Authorization", "Bearer valid-token")
	rec = httptest.NewRecorder()
	handler.GetDevice(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 for /sync-status, got %d", rec.Code)
	}

	// Test /devices/{id}/call-logs
	req = httptest.NewRequest(http.MethodGet, "/devices/HOST-0001/call-logs", nil)
	req.Header.Set("Authorization", "Bearer valid-token")
	rec = httptest.NewRecorder()
	handler.GetDevice(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 for /call-logs, got %d", rec.Code)
	}

	// Test /api/admin/devices/{id}
	req = httptest.NewRequest(http.MethodGet, "/api/admin/devices/HOST-0001", nil)
	req.Header.Set("Authorization", "Bearer valid-token")
	rec = httptest.NewRecorder()
	handler.GetDevice(rec, req)
	if rec.Code != http.StatusOK {
		t.Fatalf("expected 200 for /api/admin/devices/HOST-0001, got %d", rec.Code)
	}
}

type fakeSyncReader struct {
	snapshots map[string]repository.DeviceSyncSnapshot
}

func (r *fakeSyncReader) GetLatest(ctx context.Context, deviceID string) (repository.DeviceSyncSnapshot, bool, error) {
	_ = ctx
	snapshot, ok := r.snapshots[deviceID]
	return snapshot, ok, nil
}

func TestHandlerRejectsNonGET(t *testing.T) {
	handler := NewHandler(NewService(&fakeSessionSource{}, &fakeLocationReader{}, fakeHeartbeatPolicy{}), fakeAuthenticator{authorized: true})
	request := httptest.NewRequest(http.MethodPost, "/devices", strings.NewReader("{}"))
	request.Header.Set("Authorization", "Bearer valid-token")
	response := httptest.NewRecorder()

	handler.ListDevices(response, request)

	if response.Code != http.StatusMethodNotAllowed {
		t.Fatalf("expected 405, got %d", response.Code)
	}
}

type fakeAuthenticator struct {
	authorized bool
}

func (a fakeAuthenticator) Authenticate(token string) (string, error) {
	if a.authorized && token == "valid-token" {
		return "ADMIN-0001", nil
	}

	return "", errFakeUnauthorized
}

var errFakeUnauthorized = &fakeUnauthorizedError{}

type fakeUnauthorizedError struct {
}

func (e *fakeUnauthorizedError) Error() string {
	return "unauthorized"
}
