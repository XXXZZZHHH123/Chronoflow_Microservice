package nus.edu.u.core.common;

import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;

public interface NotificationSender {
    boolean supports(NotificationChannel channel);

    String send(NotificationRequestDTO request);
}
