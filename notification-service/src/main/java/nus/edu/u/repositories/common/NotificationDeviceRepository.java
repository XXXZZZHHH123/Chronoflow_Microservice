package nus.edu.u.repositories.common;

import nus.edu.u.domain.dataObject.common.NotificationDeviceDO;
import nus.edu.u.enums.common.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationDeviceRepository extends JpaRepository<NotificationDeviceDO, String> {
    Optional<NotificationDeviceDO> findByToken(String token);
    List<NotificationDeviceDO> findByUserIdAndStatus(String userId, DeviceStatus status);
    boolean existsByUserIdAndToken(String userId, String token);
}
