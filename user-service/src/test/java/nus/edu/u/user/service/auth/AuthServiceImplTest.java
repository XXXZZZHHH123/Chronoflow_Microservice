package nus.edu.u.user.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import java.time.LocalDateTime;
import java.util.List;
import nus.edu.u.common.enums.CommonStatusEnum;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.user.domain.dataobject.user.UserDO;
import nus.edu.u.user.domain.dto.RoleDTO;
import nus.edu.u.user.domain.dto.UserPermissionDTO;
import nus.edu.u.user.domain.dto.UserRoleDTO;
import nus.edu.u.user.domain.dto.UserTokenDTO;
import nus.edu.u.user.domain.vo.auth.LoginReqVO;
import nus.edu.u.user.domain.vo.auth.LoginRespVO;
import nus.edu.u.user.domain.vo.auth.UserVO;
import nus.edu.u.user.domain.vo.role.RoleRespVO;
import nus.edu.u.user.service.role.RoleService;
import nus.edu.u.user.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
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
class AuthServiceImplTest {

    @Mock private UserService userService;
    @Mock private TokenService tokenService;
    @Mock private RoleService roleService;

    @InjectMocks private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
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
    void authenticate_successfulLogin() {
        UserDO user =
                UserDO.builder()
                        .id(10L)
                        .username("alice")
                        .password("encoded")
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .build();
        when(userService.getUserByUsername("alice")).thenReturn(user);
        when(userService.isPasswordMatch("pwd", "encoded")).thenReturn(true);

        UserDO result = service.authenticate("alice", "pwd");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void authenticate_whenUserMissing_throws() {
        when(userService.getUserByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> service.authenticate("missing", "pwd"))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(
                        nus.edu.u.common.enums.ErrorCodeConstants.AUTH_LOGIN_BAD_CREDENTIALS
                                .getCode());
    }

    @Test
    void authenticate_whenDisabled_throws() {
        UserDO user =
                UserDO.builder()
                        .id(11L)
                        .username("alice")
                        .password("encoded")
                        .status(CommonStatusEnum.DISABLE.getStatus())
                        .build();
        when(userService.getUserByUsername("alice")).thenReturn(user);
        when(userService.isPasswordMatch("pwd", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> service.authenticate("alice", "pwd"))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(
                        nus.edu.u.common.enums.ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED
                                .getCode());
    }

    @Test
    void login_returnsUserInfoAndRefreshToken() {
        LoginReqVO req =
                LoginReqVO.builder().username("bob").password("pwd").remember(true).build();
        UserDO user =
                UserDO.builder()
                        .id(20L)
                        .password("encoded")
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .loginTime(LocalDateTime.now())
                        .build();
        user.setTenantId(1000L);

        when(userService.getUserByUsername("bob")).thenReturn(user);
        when(userService.isPasswordMatch("pwd", "encoded")).thenReturn(true);
        when(tokenService.createRefreshToken(any(UserTokenDTO.class))).thenReturn("refresh");
        UserRoleDTO userRole =
                UserRoleDTO.builder()
                        .userId(20L)
                        .username("bob")
                        .email("bob@example.com")
                        .roles(List.of(RoleDTO.builder().id(1L).roleKey("ADMIN").build()))
                        .build();
        when(userService.selectUserWithRole(20L)).thenReturn(userRole);
        UserPermissionDTO permission = new UserPermissionDTO();
        permission.setPermissionKey("perm");
        when(userService.getUserPermissions(20L)).thenReturn(List.of(permission));
        when(roleService.getRole(1L)).thenReturn(RoleRespVO.builder().id(1L).name("Admin").build());

        LoginRespVO resp = service.login(req);

        assertThat(resp.getRefreshToken()).isEqualTo("refresh");
        UserVO userVO = resp.getUser();
        assertThat(userVO.getId()).isEqualTo(20L);
        assertThat(userVO.getRole()).contains("ADMIN");
    }

    @Test
    void logout_invokesTokenService() {
        StpUtil.login(30L);

        service.logout("refresh");

        verify(tokenService).removeToken("refresh");
    }

    @Test
    void refresh_whenLoggedInReturnsInfo() {
        StpUtil.login(40L);

        UserRoleDTO userRole =
                UserRoleDTO.builder()
                        .userId(40L)
                        .username("alice")
                        .roles(List.of(RoleDTO.builder().id(1L).roleKey("MEMBER").build()))
                        .build();
        when(userService.selectUserWithRole(40L)).thenReturn(userRole);
        when(userService.getUserPermissions(40L)).thenReturn(List.of());
        when(roleService.getRole(1L))
                .thenReturn(RoleRespVO.builder().id(1L).name("Member").build());

        LoginRespVO resp = service.refresh("refresh");

        assertThat(resp.getUser().getId()).isEqualTo(40L);
    }

    @Test
    void refresh_whenTokenInvalid_throws() {
        when(tokenService.getUserIdFromRefreshToken("invalid")).thenReturn(null);

        assertThatThrownBy(() -> service.refresh("invalid"))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(nus.edu.u.common.enums.ErrorCodeConstants.REFRESH_TOKEN_WRONG.getCode());
    }

    @Test
    void refresh_whenAccountMissing_throwsAccountError() {
        when(tokenService.getUserIdFromRefreshToken("refresh")).thenReturn(50L);
        when(userService.selectUserWithRole(50L)).thenReturn(null);

        assertThatThrownBy(() -> service.refresh("refresh"))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(nus.edu.u.common.enums.ErrorCodeConstants.ACCOUNT_ERROR.getCode());
    }
}
