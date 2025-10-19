package nus.edu.u.user.controller.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.user.domain.vo.permission.PermissionReqVO;
import nus.edu.u.user.domain.vo.permission.PermissionRespVO;
import nus.edu.u.user.service.permission.PermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    @Mock private PermissionService permissionService;
    @InjectMocks private PermissionController controller;

    @Test
    void list_returnsPermissions() {
        List<PermissionRespVO> permissions = List.of(new PermissionRespVO());
        when(permissionService.listPermissions()).thenReturn(permissions);

        CommonResult<List<PermissionRespVO>> result = controller.list();

        assertThat(result.getData()).isSameAs(permissions);
        verify(permissionService).listPermissions();
    }

    @Test
    void create_delegatesToService() {
        PermissionReqVO req = new PermissionReqVO();
        when(permissionService.createPermission(req)).thenReturn(9L);

        CommonResult<Long> result = controller.create(req);

        assertThat(result.getData()).isEqualTo(9L);
        verify(permissionService).createPermission(req);
    }

    @Test
    void getPermission_returnsPermission() {
        PermissionRespVO permission = new PermissionRespVO();
        when(permissionService.getPermission(3L)).thenReturn(permission);

        CommonResult<PermissionRespVO> result = controller.getPermission(3L);

        assertThat(result.getData()).isSameAs(permission);
        verify(permissionService).getPermission(3L);
    }

    @Test
    void update_returnsUpdatedPermission() {
        PermissionReqVO req = new PermissionReqVO();
        PermissionRespVO updated = new PermissionRespVO();
        when(permissionService.updatePermission(5L, req)).thenReturn(updated);

        CommonResult<PermissionRespVO> result = controller.update(5L, req);

        assertThat(result.getData()).isSameAs(updated);
        verify(permissionService).updatePermission(5L, req);
    }

    @Test
    void delete_returnsDeletionResult() {
        when(permissionService.deletePermission(7L)).thenReturn(true);

        CommonResult<Boolean> result = controller.delete(7L);

        assertThat(result.getData()).isTrue();
        verify(permissionService).deletePermission(7L);
    }
}
