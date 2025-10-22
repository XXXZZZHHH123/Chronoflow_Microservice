package nus.edu.u.services.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nus.edu.u.domain.dataObject.common.NotificationDeviceDO;
import nus.edu.u.domain.dto.common.DeviceRegisterDTO;
import nus.edu.u.domain.dto.common.NotificationDeviceViewDTO;
import nus.edu.u.enums.common.DeviceStatus;
import nus.edu.u.enums.push.PushPlatform;
import nus.edu.u.repositories.common.NotificationDeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class DeviceRegistryServiceImplTest {

    @Mock private NotificationDeviceRepository repository;

    @InjectMocks private DeviceRegistryServiceImpl service;

    @Test
    void register_newToken_savesDeviceWithDefaults() {
        when(repository.findByToken("token-123")).thenReturn(Optional.empty());

        DeviceRegisterDTO dto = new DeviceRegisterDTO();
        dto.setToken(" token-123 ");
        dto.setPlatform(null);

        service.register("user-1", dto);

        verify(repository).findByToken("token-123");
        var captor = org.mockito.ArgumentCaptor.forClass(NotificationDeviceDO.class);
        verify(repository).save(captor.capture());

        NotificationDeviceDO saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getToken()).isEqualTo("token-123");
        assertThat(saved.getPlatform()).isEqualTo(PushPlatform.WEB);
        assertThat(saved.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
    }

    @Test
    void register_existingActiveDeviceWithSameAttributes_doesNothing() {
        NotificationDeviceDO existing =
                NotificationDeviceDO.builder()
                        .userId("user-1")
                        .token("token-123")
                        .platform(PushPlatform.WEB)
                        .status(DeviceStatus.ACTIVE)
                        .build();
        when(repository.findByToken("token-123")).thenReturn(Optional.of(existing));

        DeviceRegisterDTO dto =
                DeviceRegisterDTO.builder()
                        .token("token-123")
                        .platform(PushPlatform.WEB)
                        .status(DeviceStatus.ACTIVE)
                        .build();

        service.register("user-1", dto);

        verify(repository, never()).save(any(NotificationDeviceDO.class));
    }

    @Test
    void register_existingDeviceWithDifferentUser_updatesEntity() {
        NotificationDeviceDO existing =
                NotificationDeviceDO.builder()
                        .userId("old-user")
                        .token("token-123")
                        .platform(PushPlatform.ANDROID)
                        .status(DeviceStatus.REVOKED)
                        .build();
        when(repository.findByToken("token-123")).thenReturn(Optional.of(existing));

        DeviceRegisterDTO dto =
                DeviceRegisterDTO.builder().token("token-123").platform(PushPlatform.IOS).build();

        service.register("new-user", dto);

        assertThat(existing.getUserId()).isEqualTo("new-user");
        assertThat(existing.getPlatform()).isEqualTo(PushPlatform.IOS);
        assertThat(existing.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        verify(repository).save(existing);
    }

    @Test
    void register_retryAfterDataIntegrityViolation() {
        when(repository.findByToken("dup-token"))
                .thenReturn(Optional.empty())
                .thenReturn(
                        Optional.of(
                                NotificationDeviceDO.builder()
                                        .userId("original")
                                        .token("dup-token")
                                        .platform(PushPlatform.WEB)
                                        .status(DeviceStatus.REVOKED)
                                        .build()));

        doThrow(new DataIntegrityViolationException("duplicate"))
                .doAnswer(invocation -> invocation.getArgument(0))
                .when(repository)
                .save(any(NotificationDeviceDO.class));

        DeviceRegisterDTO dto =
                DeviceRegisterDTO.builder().token("dup-token").platform(PushPlatform.IOS).build();

        service.register("resolved-user", dto);

        var captor = org.mockito.ArgumentCaptor.forClass(NotificationDeviceDO.class);
        verify(repository, times(2)).save(captor.capture());
        List<NotificationDeviceDO> saved = captor.getAllValues();
        NotificationDeviceDO retried = saved.get(saved.size() - 1);
        assertThat(retried.getUserId()).isEqualTo("resolved-user");
        assertThat(retried.getPlatform()).isEqualTo(PushPlatform.IOS);
        assertThat(retried.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
    }

    @Test
    void register_requiresValidInputs() {
        DeviceRegisterDTO dto = DeviceRegisterDTO.builder().token("token").build();

        assertThatThrownBy(() -> service.register(null, dto))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.register("user", null))
                .isInstanceOf(IllegalArgumentException.class);

        DeviceRegisterDTO emptyToken = DeviceRegisterDTO.builder().token(" ").build();
        assertThatThrownBy(() -> service.register("user", emptyToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void revokeByToken_marksDeviceAsRevoked() {
        NotificationDeviceDO device =
                NotificationDeviceDO.builder()
                        .userId("user")
                        .token("token-1")
                        .status(DeviceStatus.ACTIVE)
                        .build();
        when(repository.findByToken("token-1")).thenReturn(Optional.of(device));

        service.revokeByToken("token-1");

        assertThat(device.getStatus()).isEqualTo(DeviceStatus.REVOKED);
        verify(repository).save(device);
    }

    @Test
    void revokeByToken_ignoresBlankTokens() {
        service.revokeByToken(" ");
        verify(repository, never()).findByToken(any());
    }

    @Test
    void activeDevices_requiresUserId() {
        assertThatThrownBy(() -> service.activeDevices(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void activeDeviceViews_transformsEntities() {
        NotificationDeviceDO device =
                NotificationDeviceDO.builder()
                        .id("id-1")
                        .userId("user")
                        .token("tok")
                        .platform(PushPlatform.ANDROID)
                        .status(DeviceStatus.ACTIVE)
                        .build();
        when(repository.findByUserIdAndStatus("user", DeviceStatus.ACTIVE))
                .thenReturn(List.of(device));

        List<NotificationDeviceViewDTO> results = service.activeDeviceViews("user");

        assertThat(results)
                .containsExactly(
                        NotificationDeviceViewDTO.builder()
                                .id("id-1")
                                .token("tok")
                                .platform(PushPlatform.ANDROID)
                                .status(DeviceStatus.ACTIVE)
                                .build());
    }

    @Test
    void revokeAllForUser_setsStatusAndSavesAll() {
        NotificationDeviceDO active1 =
                NotificationDeviceDO.builder().userId("user").status(DeviceStatus.ACTIVE).build();
        NotificationDeviceDO active2 =
                NotificationDeviceDO.builder().userId("user").status(DeviceStatus.ACTIVE).build();
        List<NotificationDeviceDO> activeDevices = new ArrayList<>(List.of(active1, active2));
        when(repository.findByUserIdAndStatus("user", DeviceStatus.ACTIVE))
                .thenReturn(activeDevices);

        service.revokeAllForUser("user");

        assertThat(activeDevices).allMatch(device -> device.getStatus() == DeviceStatus.REVOKED);
        verify(repository).saveAll(activeDevices);
    }
}
