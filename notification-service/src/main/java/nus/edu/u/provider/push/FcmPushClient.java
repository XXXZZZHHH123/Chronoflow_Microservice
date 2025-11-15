package nus.edu.u.provider.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.push.PushRequestDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FcmPushClient implements PushClient {

    private final FirebaseMessaging firebaseMessaging;

    public static FcmPushClient defaultClient() {
        return new FcmPushClient(FirebaseMessaging.getInstance());
    }

    @Override
    public String send(PushRequestDTO pushRequestDTO) throws Exception {
        // Build the firebase notification (visible title/body)
        Notification notification =
                Notification.builder()
                        .setTitle(pushRequestDTO.getTitle())
                        .setBody(pushRequestDTO.getBody())
                        .build();

        // Build the message to a device token (you can extend to topics if needed)
        Message.Builder msg =
                Message.builder().setToken(pushRequestDTO.getToken()).setNotification(notification);

        // FCM "data" payload must be Map<String, String>
        if (pushRequestDTO.getData() != null && !pushRequestDTO.getData().isEmpty()) {
            Map<String, String> stringData = new HashMap<>(pushRequestDTO.getData().size());
            for (Map.Entry<String, Object> e : pushRequestDTO.getData().entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue; // skip nulls
                stringData.put(e.getKey(), String.valueOf(e.getValue()));
            }
            if (!stringData.isEmpty()) {
                msg.putAllData(stringData);
            }
        }

        // Send and return provider message ID
        return firebaseMessaging.send(msg.build());
    }
}
