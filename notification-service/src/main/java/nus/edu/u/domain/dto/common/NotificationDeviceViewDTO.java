package nus.edu.u.domain.dto.common;

import lombok.Builder;
import nus.edu.u.enums.common.DeviceStatus;
import nus.edu.u.enums.push.PushPlatform;

@Builder
public record NotificationDeviceViewDTO(
        String id, String token, PushPlatform platform, DeviceStatus status) {}
