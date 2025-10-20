package nus.edu.u.configuration.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseAdminConfig {

    private static volatile FirebaseApp INSTANCE;

    public static final String FIREBASE_SERVICE_ENV_NAME = "FIREBASE_SERVICE_ACCOUNT_JSON";

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (INSTANCE == null) {
            synchronized (FirebaseAdminConfig.class) {
                if (INSTANCE == null) {
                    var credentials = loadGoogleCredentials();
                    var options = FirebaseOptions.builder().setCredentials(credentials).build();
                    INSTANCE =
                            (FirebaseApp.getApps().isEmpty())
                                    ? FirebaseApp.initializeApp(options)
                                    : FirebaseApp.getInstance();
                }
            }
        }
        return INSTANCE;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }

    private GoogleCredentials loadGoogleCredentials() throws IOException {
        String encoded = System.getenv(FIREBASE_SERVICE_ENV_NAME);
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalStateException(FIREBASE_SERVICE_ENV_NAME + " is not set");
        }
        byte[] decoded = java.util.Base64.getDecoder().decode(encoded);
        try (var in = new java.io.ByteArrayInputStream(decoded)) {
            return GoogleCredentials.fromStream(in);
        }
    }
}
