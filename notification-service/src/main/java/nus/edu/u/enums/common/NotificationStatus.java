package nus.edu.u.enums.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationStatus {
    CREATED("CREATED"),
    DELIVERED("DELIVERED"),
    FAILED("FAILED");

    private final String code;

    @Override
    public String toString() {
        return code;
    }
}
