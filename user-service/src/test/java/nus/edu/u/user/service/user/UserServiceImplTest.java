package nus.edu.u.user.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import java.util.List;
import nus.edu.u.common.enums.ErrorCodeConstants;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.common.utils.exception.ServiceExceptionUtil;
import nus.edu.u.user.domain.dataobject.user.UserDO;
import nus.edu.u.user.domain.dataobject.user.UserRoleDO;
import nus.edu.u.user.domain.dto.CreateUserDTO;
import nus.edu.u.user.domain.dto.RoleDTO;
import nus.edu.u.user.domain.dto.UpdateUserDTO;
import nus.edu.u.user.domain.dto.UserRoleDTO;
import nus.edu.u.user.domain.vo.user.BulkUpsertUsersRespVO;
import nus.edu.u.user.enums.user.UserStatusEnum;
import nus.edu.u.user.mapper.role.RoleMapper;
import nus.edu.u.user.mapper.user.UserMapper;
import nus.edu.u.user.mapper.user.UserRoleMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Spy @InjectMocks private UserServiceImpl service;

    @Mock private UserMapper userMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, UserDO.class);
        TableInfoHelper.initTableInfo(assistant, UserRoleDO.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "self", service);
        SaTokenContextMockUtil.setMockContext();
    }

    @AfterEach
    void tearDown() {
        try {
            if (StpUtil.isLogin()) {
                StpUtil.logout();
            }
        } catch (Exception ignored) {
        }
        SaTokenContextMockUtil.clearContext();
    }

    @Test
    void createUserWithRoleIds_persistsUserAndRoles() {
        CreateUserDTO dto =
                CreateUserDTO.builder()
                        .email("user@example.com")
                        .roleIds(List.of(10L, 11L))
                        .remark("remark")
                        .build();

        when(userMapper.existsEmail("user@example.com", null)).thenReturn(false);
        when(roleMapper.countByIds(dto.getRoleIds())).thenReturn(dto.getRoleIds().size());
        doAnswer(
                        invocation -> {
                            UserDO user = invocation.getArgument(0);
                            user.setId(100L);
                            return 1;
                        })
                .when(userMapper)
                .insert(any(UserDO.class));
        when(userRoleMapper.insert(any(UserRoleDO.class))).thenReturn(1);

        Long id = service.createUserWithRoleIds(dto);

        assertThat(id).isEqualTo(100L);
        verify(userRoleMapper, times(dto.getRoleIds().size())).insert(any(UserRoleDO.class));
    }

    @Test
    void createUserWithRoleIds_forbiddenRoleThrows() {
        CreateUserDTO dto =
                CreateUserDTO.builder().email("user@example.com").roleIds(List.of(1L)).build();

        assertThatThrownBy(() -> service.createUserWithRoleIds(dto))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.ROLE_NOT_FOUND.getCode());
    }

    @Test
    void updateUserWithRoleIds_updatesUserAndSynchronizesRoles() {
        UpdateUserDTO dto =
                UpdateUserDTO.builder()
                        .id(200L)
                        .email("new@example.com")
                        .remark("remark")
                        .roleIds(List.of(10L, 12L))
                        .build();

        UserDO existing = new UserDO();
        existing.setId(200L);
        existing.setDeleted(false);

        UserDO updated = new UserDO();
        updated.setId(200L);
        updated.setEmail("new@example.com");

        when(userMapper.selectById(200L)).thenReturn(existing, updated);
        when(userMapper.existsEmail("new@example.com", 200L)).thenReturn(false);
        when(userMapper.update(any(UserDO.class), any())).thenReturn(1);
        when(roleMapper.countByIds(dto.getRoleIds())).thenReturn(dto.getRoleIds().size());
        when(userRoleMapper.selectAliveRoleIdsByUser(200L)).thenReturn(List.of(10L, 11L));
        when(userRoleMapper.batchLogicalDelete(eq(200L), any())).thenReturn(1);
        when(userRoleMapper.batchRevive(eq(200L), any())).thenReturn(1);
        when(userRoleMapper.insertMissing(eq(200L), any())).thenReturn(1);

        UserDO result = service.updateUserWithRoleIds(dto);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void softDeleteUser_marksDeleted() {
        UserDO existing = new UserDO();
        existing.setId(5L);
        existing.setDeleted(false);

        when(userMapper.selectRawById(5L)).thenReturn(existing);
        when(userMapper.update(any(UserDO.class), any())).thenReturn(1);

        service.softDeleteUser(5L);

        verify(userRoleMapper).delete(any());
    }

    @Test
    void restoreUser_reactivatesRecord() {
        UserDO deleted = new UserDO();
        deleted.setId(7L);
        deleted.setDeleted(true);

        when(userMapper.selectRawById(7L)).thenReturn(deleted);
        when(userMapper.update(any(UserDO.class), any())).thenReturn(1);

        service.restoreUser(7L);

        verify(userMapper).update(any(UserDO.class), any());
    }

    @Test
    void disableUser_whenAlreadyDisabled_throws() {
        UserDO user = new UserDO();
        user.setId(8L);
        user.setStatus(UserStatusEnum.DISABLE.getCode());
        user.setDeleted(false);
        when(userMapper.selectById(8L)).thenReturn(user);

        assertThatThrownBy(() -> service.disableUser(8L))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.USER_ALREADY_DISABLED.getCode());
    }

    @Test
    void enableUser_whenAlreadyEnabled_throws() {
        UserDO user = new UserDO();
        user.setId(9L);
        user.setStatus(UserStatusEnum.ENABLE.getCode());
        user.setDeleted(false);
        when(userMapper.selectById(9L)).thenReturn(user);

        assertThatThrownBy(() -> service.enableUser(9L))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.USER_ALREADY_ENABLED.getCode());
    }

    @Test
    void getAllUserProfiles_excludesCurrentUser() {
        StpUtil.login(500L);
        UserRoleDTO self =
                UserRoleDTO.builder()
                        .userId(500L)
                        .username("self")
                        .email("self@example.com")
                        .status(UserStatusEnum.ENABLE.getCode())
                        .roles(List.of(RoleDTO.builder().id(1L).build()))
                        .build();
        UserRoleDTO other =
                UserRoleDTO.builder()
                        .userId(600L)
                        .username("other")
                        .email("other@example.com")
                        .status(UserStatusEnum.ENABLE.getCode())
                        .roles(List.of(RoleDTO.builder().id(2L).build()))
                        .build();
        when(userMapper.selectAllUsersWithRoles()).thenReturn(List.of(self, other));

        var profiles = service.getAllUserProfiles();

        assertThat(profiles).hasSize(1);
        assertThat(profiles.get(0).getId()).isEqualTo(600L);
    }

    @Test
    void bulkUpsertUsers_countsCreatedAndUpdated() {
        CreateUserDTO row1 =
                CreateUserDTO.builder()
                        .email("new@example.com")
                        .roleIds(List.of(10L))
                        .rowIndex(1)
                        .build();
        CreateUserDTO row2 =
                CreateUserDTO.builder()
                        .email("existing@example.com")
                        .roleIds(List.of(12L))
                        .rowIndex(2)
                        .build();

        doReturn(true, false)
                .when(service)
                .tryCreateOrFallbackToUpdate(anyString(), any(), anyList());

        BulkUpsertUsersRespVO resp = service.bulkUpsertUsers(List.of(row1, row2));

        assertThat(resp.getCreatedCount()).isEqualTo(1);
        assertThat(resp.getUpdatedCount()).isEqualTo(1);
        assertThat(resp.getFailedCount()).isEqualTo(0);
    }

    @Test
    void tryCreateOrFallbackToUpdate_existingEmailTriggersUpdate() {
        doThrow(ServiceExceptionUtil.exception(ErrorCodeConstants.EMAIL_EXIST))
                .when(service)
                .createUserWithRoleIds(any(CreateUserDTO.class));
        doReturn(new UserDO())
                .when(service)
                .updateUserWithRoleIds(any(UpdateUserDTO.class));

        when(userMapper.selectIdByEmail("existing@example.com")).thenReturn(3000L);
        when(userMapper.selectById(3000L)).thenReturn(new UserDO());
        when(roleMapper.countByIds(List.of(20L))).thenReturn(1);

        boolean created =
                service.tryCreateOrFallbackToUpdate(
                        "existing@example.com", "remark", List.of(20L));

        assertThat(created).isFalse();
    }

    @Test
    void tryCreateOrFallbackToUpdate_missingUserThrows() {
        doThrow(ServiceExceptionUtil.exception(ErrorCodeConstants.EMAIL_EXIST))
                .when(service)
                .createUserWithRoleIds(any(CreateUserDTO.class));
        when(userMapper.selectIdByEmail("missing@example.com")).thenReturn(null);

        assertThatThrownBy(
                        () ->
                                service.tryCreateOrFallbackToUpdate(
                                        "missing@example.com", "remark", List.of(20L)))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCodeConstants.NULL_USERID.getCode());
    }
}
