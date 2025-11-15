package nus.edu.u.provider.push;

import java.util.EnumMap;
import java.util.Map;
import nus.edu.u.enums.push.PushProvider;

public final class PushClientFactory {

    private static final PushClientFactory INSTANCE = new PushClientFactory();

    public static PushClientFactory getInstance() {
        return INSTANCE;
    }

    private final Map<PushProvider, PushClient> cache = new EnumMap<>(PushProvider.class);

    private PushClientFactory() {}

    public PushClient getClient(PushProvider p) {
        return cache.computeIfAbsent(
                p,
                provider ->
                        switch (provider) {
                            case FCM -> FcmPushClient.defaultClient();
                            default ->
                                    throw new IllegalArgumentException("Unsupported: " + provider);
                        });
    }
}
