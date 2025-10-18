package nus.edu.u.user.service.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import java.util.ArrayList;
import java.util.List;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.user.domain.dataobject.permission.PermissionDO;
import nus.edu.u.user.domain.dataobject.role.RoleDO;
import nus.edu.u.user.domain.dataobject.role.RolePermissionDO;
import nus.edu.u.user.domain.dataobject.user.UserRoleDO;
import nus.edu.u.user.domain.dto.RoleDTO;
import nus.edu.u.user.domain.dto.UserRoleDTO;
import nus.edu.u.user.domain.vo.role.RoleAssignReqVO;
import nus.edu.u.user.domain.vo.role.RoleReqVO;
import nus.edu.u.user.domain.vo.role.RoleRespVO;
import nus.edu.u.user.mapper.permission.PermissionMapper;
import nus.edu.u.user.mapper.role.RoleMapper;
import nus.edu.u.user.mapper.role.RolePermissionMapper;
import nus.edu.u.user.mapper.user.UserRoleMapper;
import nus.edu.u.user.service.auth.AuthService;
import nus.edu.u.user.service.user.UserService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceImplTest {

    @Mock private RoleMapper roleMapper;
    @Mock private PermissionMapper permissionMapper;
    @Mock private RolePermissionMapper rolePermissionMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private UserService userService;
    @Mock private AuthService authService;

    @InjectMocks private RoleServiceImpl service;

    private RoleReqVO reqVO;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, RoleDO.class);
        TableInfoHelper.initTableInfo(assistant, RolePermissionDO.class);
        TableInfoHelper.initTableInfo(assistant, UserRoleDO.class);
    }

    @BeforeEach
    void setUp() {
        reqVO = new RoleReqVO();
        reqVO.setName("Reviewer");
        reqVO.setKey("REVIEWER");
        reqVO.setPermissions(List.of(1L, 2L));
    }

    @Test
    void listRoles_filtersReservedOnes() {
        RoleDO organizer =
                RoleDO.builder().id(1L).roleKey(RoleServiceImpl.ORGANIZER_ROLE_KEY).build();
        RoleDO custom = RoleDO.builder().id(2L).roleKey("CUSTOM").build();
        when(roleMapper.selectList(null)).thenReturn(List.of(organizer, custom));

        List<RoleRespVO> roles = service.listRoles();

        assertThat(roles)
                .singleElement()
                .satisfies(vo -> assertThat(vo.getKey()).isEqualTo("CUSTOM"));
    }

    @Test
    void createRole_insertsRoleAndPermissions() {
        when(roleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(roleMapper.insert(any(RoleDO.class)))
                .thenAnswer(
                        invocation -> {
                            RoleDO role = invocation.getArgument(0);
                            role.setId(100L);
                            return 1;
                        });
        when(rolePermissionMapper.insert(any(RolePermissionDO.class))).thenReturn(1);
        when(permissionMapper.selectBatchIds(reqVO.getPermissions()))
                .thenReturn(
                        List.of(
                                PermissionDO.builder()
                                        .id(1L)
                                        .name("View")
                                        .permissionKey("system:view")
                                        .description("desc")
                                        .build()));

        RoleRespVO resp = service.createRole(reqVO);

        assertThat(resp.getId()).isEqualTo(100L);
        verify(rolePermissionMapper, times(reqVO.getPermissions().size()))
                .insert(any(RolePermissionDO.class));
    }

    @Test
    void createRole_whenRoleExists_throws() {
        when(roleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(new RoleDO()));

        assertThatThrownBy(() -> service.createRole(reqVO))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(
                        nus.edu.u.common.enums.ErrorCodeConstants.EXISTING_ROLE_FAILED.getCode());
    }

    @Test
    void getRole_returnsConvertedRole() {
        RoleDO role =
                RoleDO.builder()
                        .id(5L)
                        .name("Reviewer")
                        .roleKey("REVIEWER")
                        .permissionList(List.of(1L))
                        .build();
        when(roleMapper.selectById(5L)).thenReturn(role);
        when(permissionMapper.selectBatchIds(List.of(1L)))
                .thenReturn(
                        List.of(
                                PermissionDO.builder()
                                        .id(1L)
                                        .name("View")
                                        .permissionKey("system:view")
                                        .description("desc")
                                        .build()));

        RoleRespVO resp = service.getRole(5L);

        assertThat(resp.getPermissions())
                .singleElement()
                .satisfies(p -> assertThat(p.getName()).isEqualTo("View"));
    }

    @Test
    void deleteRole_whenAssignedUsers_throws() {
        RoleDO role = RoleDO.builder().id(7L).roleKey("CUSTOM").build();
        when(roleMapper.selectById(7L)).thenReturn(role);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(UserRoleDO.builder().build()));

        assertThatThrownBy(() -> service.deleteRole(7L))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(nus.edu.u.common.enums.ErrorCodeConstants.CANNOT_DELETE_ROLE.getCode());
    }

    @Test
    void deleteRole_successfullyRemovesRole() {
        RoleDO role = RoleDO.builder().id(8L).roleKey("CUSTOM").build();
        when(roleMapper.selectById(8L)).thenReturn(role);
        when(userRoleMapper.selectList(any())).thenReturn(List.of());
        when(roleMapper.deleteById(8L)).thenReturn(1);
        when(rolePermissionMapper.delete(any())).thenReturn(1);

        service.deleteRole(8L);

        verify(roleMapper).deleteById(8L);
        verify(rolePermissionMapper).delete(any());
    }

    @Test
    void updateRole_synchronizesPermissions() {
        RoleDO role =
                RoleDO.builder()
                        .id(9L)
                        .name("Reviewer")
                        .roleKey("REVIEWER")
                        .permissionList(new ArrayList<>(List.of(1L, 2L)))
                        .build();
        when(roleMapper.selectById(9L)).thenReturn(role);
        when(roleMapper.updateById(any(RoleDO.class))).thenReturn(1);
        when(rolePermissionMapper.delete(any())).thenReturn(1);
        when(rolePermissionMapper.insert(any(RolePermissionDO.class))).thenReturn(1);
        when(permissionMapper.selectBatchIds(any()))
                .thenReturn(List.of(PermissionDO.builder().id(2L).name("View").build()));

        RoleRespVO resp = service.updateRole(9L, reqVO);

        assertThat(resp.getName()).isEqualTo("Reviewer");
    }

    @Test
    void assignRoles_updatesBindings() {
        RoleAssignReqVO assignReq = new RoleAssignReqVO();
        assignReq.setUserId(1L);
        assignReq.setRoles(List.of(10L, 11L));

        UserRoleDTO userRole =
                UserRoleDTO.builder()
                        .userId(1L)
                        .roles(
                                List.of(
                                        RoleDTO.builder().id(10L).roleKey("EXIST").build(),
                                        RoleDTO.builder()
                                                .id(20L)
                                                .roleKey(RoleServiceImpl.ORGANIZER_ROLE_KEY)
                                                .build()))
                        .build();
        when(userService.selectUserWithRole(1L)).thenReturn(userRole);
        when(userRoleMapper.delete(any())).thenReturn(1);
        when(userRoleMapper.insert(any(UserRoleDO.class))).thenReturn(1);

        service.assignRoles(assignReq);

        verify(userRoleMapper).insert(any(UserRoleDO.class));
    }

    @Test
    void assignRoles_whenUserMissing_throws() {
        RoleAssignReqVO assignReq = new RoleAssignReqVO();
        assignReq.setUserId(2L);
        assignReq.setRoles(List.of(1L));
        when(userService.selectUserWithRole(2L)).thenReturn(null);

        assertThatThrownBy(() -> service.assignRoles(assignReq))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(nus.edu.u.common.enums.ErrorCodeConstants.USER_NOTFOUND.getCode());
    }
}
