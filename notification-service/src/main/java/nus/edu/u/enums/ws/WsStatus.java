package nus.edu.u.enums.ws;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WsStatus {
    PENDING("PENDING"),
    SENT("SENT"),
    FAILED("FAILED");

    private final String code;

    @Override
    public String toString() {
        return code;
    }
}
