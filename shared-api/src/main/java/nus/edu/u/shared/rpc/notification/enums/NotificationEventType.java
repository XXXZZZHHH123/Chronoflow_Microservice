package nus.edu.u.shared.rpc.notification.enums;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationEventType {
    ORGANIZER_WELCOME("organizer-welcome"),
    MEMBER_INVITE("member-invite"),
    ATTENDEE_INVITE("attendee-invite"),
    NEW_TASK_ASSIGN("new-task-assigned");

    private final String code;

    public static String buildEventId(NotificationEventType type, Object... parts) {
        return type.getCode()
                + ":"
                + Arrays.stream(parts).map(String::valueOf).collect(Collectors.joining(":"));
    }

    @Override
    public String toString() {
        return code;
    }
}
