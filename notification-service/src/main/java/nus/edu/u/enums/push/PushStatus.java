package nus.edu.u.enums.push;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushStatus {
    PENDING("PENDING"),
    SENT("SENT"),
    FAILED("FAILED");

    private final String code;

    @Override
    public String toString() {
        return code;
    }
}
