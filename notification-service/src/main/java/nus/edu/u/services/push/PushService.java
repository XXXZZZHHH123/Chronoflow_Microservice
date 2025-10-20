package nus.edu.u.services.push;

import java.util.Map;
import nus.edu.u.domain.dto.push.PushRequestDTO;

public interface PushService {
    String send(PushRequestDTO dto);

    // new: fan-out to all active devices of a user
    Map<String, String> sendToUser(String userId, PushRequestDTO base);
}
