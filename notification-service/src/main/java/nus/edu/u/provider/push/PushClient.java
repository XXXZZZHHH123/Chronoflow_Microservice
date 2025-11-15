package nus.edu.u.provider.push;

import nus.edu.u.domain.dto.push.PushRequestDTO;

public interface PushClient {
    String send(PushRequestDTO pushRequestDTO) throws Exception;
}
