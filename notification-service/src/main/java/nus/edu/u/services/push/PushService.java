package nus.edu.u.services.push;



import nus.edu.u.domain.dto.push.PushRequestDTO;

import java.util.Map;

public interface PushService {
    String send(PushRequestDTO dto);

    // new: fan-out to all active devices of a user
    Map<String, String> sendToUser(String userId, PushRequestDTO base);
}
