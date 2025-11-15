package nus.edu.u.services.notification;

import nus.edu.u.domain.dto.common.NotificationRequestDTO;

public interface TransportImplementor {

    void process(NotificationRequestDTO notification);
}
