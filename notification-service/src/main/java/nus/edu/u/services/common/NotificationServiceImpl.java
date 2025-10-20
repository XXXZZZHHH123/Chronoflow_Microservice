package nus.edu.u.services.common;

import lombok.RequiredArgsConstructor;
import nus.edu.u.core.common.NotificationSenderFactory;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationSenderFactory factory;

    @Override
    public String send(NotificationRequestDTO request) {
        return factory.strategy(request.getChannel()).send(request);
    }
}
