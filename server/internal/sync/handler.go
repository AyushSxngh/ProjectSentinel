package sync

import (
	"context"
	"errors"

	"github.com/xaiop/project-sentinel/server/internal/protocol"
)

type Session interface {
	AuthenticatedDeviceID() string
	IsAuthenticated() bool
}

type Handler struct {
	service *Service
}

func NewHandler(service *Service) *Handler {
	return &Handler{
		service: service,
	}
}

func (h *Handler) HandleSync(ctx context.Context, session Session, msg *protocol.Message) (*protocol.Message, error) {
	if !session.IsAuthenticated() {
		return protocol.NewError(msg.Sequence, 401, "Unauthorized"), errors.New("unauthorized")
	}

	var syncData protocol.DeviceSyncMessage
	if err := msg.DecodeData(&syncData); err != nil {
		return protocol.NewError(msg.Sequence, 400, "Bad Request: malformed sync data"), err
	}

	err := h.service.ProcessSync(ctx, session.AuthenticatedDeviceID(), &syncData)
	if err != nil {
		if errors.Is(err, ErrUnauthorizedDevice) {
			return protocol.NewError(msg.Sequence, 403, "Forbidden: device mismatch"), err
		}
		return protocol.NewError(msg.Sequence, 400, err.Error()), err
	}

	ackMsg, err := protocol.NewMessage(protocol.TypeDeviceSyncAck, msg.Sequence, protocol.DeviceSyncAckMessage{
		Success: true,
	})
	if err != nil {
		return nil, err
	}

	return ackMsg, nil
}
