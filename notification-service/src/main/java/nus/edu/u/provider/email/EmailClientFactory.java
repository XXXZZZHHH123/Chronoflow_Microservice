package nus.edu.u.provider.email;

import java.util.EnumMap;
import java.util.Map;
import nus.edu.u.enums.email.EmailProvider;

public final class EmailClientFactory {
    private static final EmailClientFactory INSTANCE = new EmailClientFactory();

    public static EmailClientFactory getInstance() {
        return INSTANCE;
    }

    private final Map<EmailProvider, EmailClient> cache = new EnumMap<>(EmailProvider.class);

    private EmailClientFactory() {}

    public EmailClient getClient(EmailProvider p) {
        return cache.computeIfAbsent(
                p,
                provider ->
                        switch (provider) {
                            case AWS_SES -> SESEmailClient.defaultClient();
                            default ->
                                    throw new IllegalArgumentException("Unsupported: " + provider);
                        });
    }
}
