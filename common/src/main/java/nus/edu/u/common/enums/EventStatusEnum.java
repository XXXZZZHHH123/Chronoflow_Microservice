package nus.edu.u.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nus.edu.u.common.core.ArrayValuable;

@Getter
@AllArgsConstructor
public enum EventStatusEnum implements ArrayValuable<Integer> {
    NOT_STARTED(0, "not_started"),
    ACTIVE(1, "active"),
    COMPLETED(2, "completed");

    public static final Integer[] ARRAYS = new Integer[] {0, 1, 2};

    private final int code;
    private final String description;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static EventStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EventStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknow event status code: " + code);
    }
}
