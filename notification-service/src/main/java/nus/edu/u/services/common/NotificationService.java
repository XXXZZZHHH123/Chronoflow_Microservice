package nus.edu.u.services.common;

import nus.edu.u.domain.dto.common.NotificationRequestDTO;

public interface NotificationService {
    String send(NotificationRequestDTO request);
}
