package nus.edu.u.user.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nus.edu.u.shared.rpc.user.RoleBriefDTO;
import nus.edu.u.shared.rpc.user.TenantDTO;
import nus.edu.u.shared.rpc.user.UserInfoDTO;
import nus.edu.u.shared.rpc.user.UserProfileDTO;
import nus.edu.u.user.domain.dataobject.role.RoleDO;
import nus.edu.u.user.domain.dataobject.tenant.TenantDO;
import nus.edu.u.user.domain.dataobject.user.UserDO;
import nus.edu.u.user.domain.dataobject.user.UserRoleDO;
import nus.edu.u.user.domain.vo.user.UserProfileRespVO;
import nus.edu.u.user.mapper.role.RoleMapper;
import nus.edu.u.user.mapper.tenant.TenantMapper;
import nus.edu.u.user.mapper.user.UserMapper;
import nus.edu.u.user.mapper.user.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRpcServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private TenantMapper tenantMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private UserService userService;

    @InjectMocks private UserRpcServiceImpl service;

    private UserDO basicUser;

    @BeforeEach
    void init() {
        basicUser =
                UserDO.builder()
                        .id(1L)
                        .username("alice")
                        .status(1)
                        .email("alice@chrono.sg")
                        .phone("123")
                        .build();
        basicUser.setTenantId(10L);
        basicUser.setCreateTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        basicUser.setUpdateTime(LocalDateTime.of(2025, 1, 2, 12, 0));
    }

    @Test
    void exists_returnsFalseForNullId() {
        assertFalse(service.exists(null));
        verifyNoInteractions(userMapper);
    }

    @Test
    void exists_returnsTrueWhenUserFound() {
        when(userMapper.selectById(1L)).thenReturn(basicUser);

        assertThat(service.exists(1L)).isTrue();
    }

    @Test
    void exists_handlesExceptionsByReturningFalse() {
        when(userMapper.selectById(2L)).thenThrow(new RuntimeException("db error"));

        assertThat(service.exists(2L)).isFalse();
    }

    @Test
    void getUsers_withEmptyInput_returnsEmptyMap() {
        Map<Long, UserInfoDTO> result = service.getUsers(List.of());
        assertThat(result).isEmpty();
        verifyNoInteractions(userMapper);
    }

    @Test
    void getUsers_returnsConvertedUsersWithRoles() {
        when(userMapper.selectBatchIds(any(Collection.class))).thenReturn(List.of(basicUser));

        UserRoleDO relation = UserRoleDO.builder().id(11L).userId(1L).roleId(100L).build();
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(relation));

        RoleDO role = RoleDO.builder().id(100L).name("Admin").roleKey("ADMIN").build();
        when(roleMapper.selectBatchIds(any(Collection.class))).thenReturn(List.of(role));

        Map<Long, UserInfoDTO> result = service.getUsers(Set.of(1L));

        assertThat(result).containsKey(1L);
        UserInfoDTO dto = result.get(1L);
        assertThat(dto.getUsername()).isEqualTo("alice");
        assertThat(dto.getRoles()).extracting(RoleBriefDTO::getRoleKey).containsExactly("ADMIN");
    }

    @Test
    void getUsers_whenMapperThrows_returnsEmptyMap() {
        when(userMapper.selectBatchIds(any(Collection.class)))
                .thenThrow(new RuntimeException("boom"));

        Map<Long, UserInfoDTO> result = service.getUsers(Set.of(1L));
        assertThat(result).isEmpty();
    }

    @Test
    void getEnabledUserProfiles_convertsRespVOs() {
        UserProfileRespVO profile = new UserProfileRespVO();
        profile.setId(5L);
        profile.setName("Bob");
        profile.setEmail("bob@chrono.sg");
        profile.setPhone("555");
        profile.setRoles(List.of(1L, 2L));
        profile.setRegistered(true);

        when(userService.getEnabledUserProfiles()).thenReturn(List.of(profile));

        List<UserProfileDTO> dtos = service.getEnabledUserProfiles();

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getName()).isEqualTo("Bob");
        assertThat(dtos.get(0).isRegistered()).isTrue();
    }

    @Test
    void getTenantById_returnsNullForNullRequest() {
        assertNull(service.getTenantById(null));
        verifyNoInteractions(tenantMapper);
    }

    @Test
    void getTenantById_returnsConvertedTenant() {
        TenantDO tenant =
                TenantDO.builder()
                        .id(22L)
                        .name("Tenant")
                        .contactUserId(1L)
                        .contactName("Alice")
                        .contactMobile("555")
                        .address("Earth")
                        .status(1)
                        .tenantCode("ABC")
                        .build();
        when(tenantMapper.selectById(22L)).thenReturn(tenant);

        TenantDTO dto = service.getTenantById(22L);

        assertThat(dto.getName()).isEqualTo("Tenant");
        assertThat(dto.getTenantCode()).isEqualTo("ABC");
    }

    @Test
    void getTenantById_whenMapperThrows_returnsNull() {
        when(tenantMapper.selectById(33L)).thenThrow(new RuntimeException("oops"));

        assertNull(service.getTenantById(33L));
    }
}
