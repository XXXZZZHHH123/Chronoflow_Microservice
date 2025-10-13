package nus.edu.u.event.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    PENDING(0, "Pending"),
    PROGRESS(1, "Progress"),
    COMPLETED(2, "Completed"),
    DELAYED(3, "Delayed"),
    BLOCKED(4, "Blocked"),
    PENDING_APPROVAL(5, "Pending approval"),
    REJECTED(6, "Rejected");

    private final Integer status;
    private final String name;

    private static final Map<Integer, TaskStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(TaskStatusEnum::getStatus, e -> e));

    public static TaskStatusEnum fromStatus(Integer status) {
        if (status == null) {
            return null;
        }
        return CODE_MAP.get(status);
    }

    public static TaskStatusEnum fromStatusOrDefault(Integer status) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.status, status))
                .findFirst()
                .orElse(PENDING);
    }
}
