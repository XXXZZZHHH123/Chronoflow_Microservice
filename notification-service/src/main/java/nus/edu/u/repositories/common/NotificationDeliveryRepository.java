package nus.edu.u.repositories.common;

import nus.edu.u.domain.dataObject.common.NotificationDeliveryDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationDeliveryRepository
        extends JpaRepository<NotificationDeliveryDO, String> {}
