package nus.edu.u.core.common;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import nus.edu.u.enums.common.NotificationChannel;
import org.springframework.stereotype.Component;

/**
 * Factory that dynamically selects the correct NotificationSender implementation based on the
 * provided NotificationChannel.
 */
@Component
public class NotificationSenderFactory {

    private final Map<NotificationChannel, NotificationSender> registry;

    public NotificationSenderFactory(List<NotificationSender> senders) {
        this.registry =
                senders.stream()
                        .collect(
                                Collectors.toMap(
                                        sender ->
                                                Arrays.stream(NotificationChannel.values())
                                                        .filter(sender::supports)
                                                        .findFirst()
                                                        .orElseThrow(
                                                                () ->
                                                                        new IllegalStateException(
                                                                                sender.getClass()
                                                                                                .getSimpleName()
                                                                                        + " supports no channel")),
                                        Function.identity()));
    }

    public NotificationSender strategy(NotificationChannel channel) {
        NotificationSender sender = registry.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("No sender found for channel: " + channel);
        }
        return sender;
    }
}
