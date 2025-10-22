package nus.edu.u.core.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import org.junit.jupiter.api.Test;

class NotificationSenderFactoryTest {

    @Test
    void strategyReturnsMatchingSender() {
        NotificationSender emailSender =
                new StubSender(NotificationChannel.EMAIL, "email-message-id");
        NotificationSender pushSender = new StubSender(NotificationChannel.PUSH, "push-message-id");

        NotificationSenderFactory factory =
                new NotificationSenderFactory(List.of(emailSender, pushSender));

        assertThat(factory.strategy(NotificationChannel.EMAIL)).isSameAs(emailSender);
        assertThat(factory.strategy(NotificationChannel.PUSH)).isSameAs(pushSender);
    }

    @Test
    void strategyThrowsWhenChannelMissing() {
        NotificationSenderFactory factory =
                new NotificationSenderFactory(
                        List.of(new StubSender(NotificationChannel.EMAIL, "ignored")));

        assertThatThrownBy(() -> factory.strategy(NotificationChannel.WS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("WS");
    }

    @Test
    void constructorFailsWhenSenderSupportsNoChannel() {
        NotificationSender unsupportedSender =
                new NotificationSender() {
                    @Override
                    public boolean supports(NotificationChannel channel) {
                        return false;
                    }

                    @Override
                    public String send(NotificationRequestDTO request) {
                        return "never";
                    }
                };

        assertThatThrownBy(() -> new NotificationSenderFactory(List.of(unsupportedSender)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("supports no channel");
    }

    private static final class StubSender implements NotificationSender {

        private final NotificationChannel channel;
        private final String response;

        private StubSender(NotificationChannel channel, String response) {
            this.channel = channel;
            this.response = response;
        }

        @Override
        public boolean supports(NotificationChannel channel) {
            return this.channel == channel;
        }

        @Override
        public String send(NotificationRequestDTO request) {
            return response;
        }
    }
}
