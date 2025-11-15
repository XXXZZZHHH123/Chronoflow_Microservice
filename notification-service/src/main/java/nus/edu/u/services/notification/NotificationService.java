package nus.edu.u.services.notification;

import nus.edu.u.domain.dto.common.NotificationRequestDTO;

public abstract class NotificationService {
    protected final TransportImplementor transportImplementor;
    protected final TemplateEngineImplementor templateEngineImplementor;

    protected NotificationService(
            TransportImplementor transportImplementor,
            TemplateEngineImplementor templateEngineImplementor) {
        this.transportImplementor = transportImplementor;
        this.templateEngineImplementor = templateEngineImplementor;
    }

    public abstract void send(NotificationRequestDTO notificationRequestDTO);
}
