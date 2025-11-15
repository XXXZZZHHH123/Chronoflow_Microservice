package nus.edu.u.provider.ws;

import java.util.EnumMap;
import java.util.Map;
import nus.edu.u.enums.ws.WSProvider;

public final class WSClientFactory {
    private static final WSClientFactory INSTANCE = new WSClientFactory();

    public static WSClientFactory getInstance() {
        return INSTANCE;
    }

    private final Map<WSProvider, WSClient> cache = new EnumMap<>(WSProvider.class);

    private WSClientFactory() {}

    public WSClient getClient(WSProvider p) {
        return cache.computeIfAbsent(
                p,
                provider ->
                        switch (provider) {
                            case FLUX -> FluxWSClient.defaultClient();
                            default ->
                                    throw new IllegalArgumentException("Unsupported: " + provider);
                        });
    }
}
