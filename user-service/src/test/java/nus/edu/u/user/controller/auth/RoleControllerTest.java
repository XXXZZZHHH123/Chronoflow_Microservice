package nus.edu.u.user.controller.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.user.domain.vo.role.RoleAssignReqVO;
import nus.edu.u.user.domain.vo.role.RoleReqVO;
import nus.edu.u.user.domain.vo.role.RoleRespVO;
import nus.edu.u.user.service.role.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock private RoleService roleService;
    @InjectMocks private RoleController controller;

    @Test
    void listRoles_returnsRoles() {
        List<RoleRespVO> roles = List.of(new RoleRespVO());
        when(roleService.listRoles()).thenReturn(roles);

        CommonResult<List<RoleRespVO>> result = controller.listRoles();

        assertThat(result.getData()).isSameAs(roles);
        verify(roleService).listRoles();
    }

    @Test
    void createRole_delegatesToService() {
        RoleReqVO req = new RoleReqVO();
        RoleRespVO created = new RoleRespVO();
        when(roleService.createRole(req)).thenReturn(created);

        CommonResult<RoleRespVO> result = controller.createRole(req);

        assertThat(result.getData()).isSameAs(created);
        verify(roleService).createRole(req);
    }

    @Test
    void getRole_returnsRole() {
        RoleRespVO role = new RoleRespVO();
        when(roleService.getRole(5L)).thenReturn(role);

        CommonResult<RoleRespVO> result = controller.getRole(5L);

        assertThat(result.getData()).isSameAs(role);
        verify(roleService).getRole(5L);
    }

    @Test
    void deleteRole_callsServiceAndReturnsTrue() {
        CommonResult<Boolean> result = controller.deleteRole(7L);

        assertThat(result.getData()).isTrue();
        verify(roleService).deleteRole(7L);
    }

    @Test
    void updateRole_returnsUpdatedRole() {
        RoleReqVO req = new RoleReqVO();
        RoleRespVO updated = new RoleRespVO();
        when(roleService.updateRole(8L, req)).thenReturn(updated);

        CommonResult<RoleRespVO> result = controller.updateRole(8L, req);

        assertThat(result.getData()).isSameAs(updated);
        verify(roleService).updateRole(8L, req);
    }

    @Test
    void assignRole_invokesService() {
        RoleAssignReqVO req = new RoleAssignReqVO();

        CommonResult<Boolean> result = controller.assignRole(req);

        assertThat(result.getData()).isTrue();
        verify(roleService).assignRoles(req);
    }
}
