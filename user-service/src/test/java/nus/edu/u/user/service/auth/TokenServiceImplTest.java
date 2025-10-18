package nus.edu.u.user.service.auth;

import static nus.edu.u.common.constant.CacheConstants.LOGIN_REFRESH_TOKEN_KEY;
import static nus.edu.u.common.exception.enums.GlobalErrorCodeConstants.EXPIRED_LOGIN_CREDENTIALS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import java.util.concurrent.TimeUnit;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.user.config.SecurityProperties;
import nus.edu.u.user.domain.dto.UserTokenDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private TokenServiceImpl service;

    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        securityProperties.setRefreshTokenExpire(3600L);
        ReflectionTestUtils.setField(service, "securityProperties", securityProperties);
        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void clearContext() {
        try {
            if (StpUtil.isLogin()) {
                StpUtil.logout();
            }
        } catch (Exception ignored) {
            // ignore
        }
        SaTokenContextMockUtil.clearContext();
    }

    @Test
    void createRefreshToken_whenRememberStoresInRedis() {
        UserTokenDTO dto = new UserTokenDTO(1L, 2L, 3L, true);

        String token = service.createRefreshToken(dto);

        assertThat(token).isNotBlank();
        verify(valueOperations)
                .set(eq(LOGIN_REFRESH_TOKEN_KEY + token), eq("1"), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void createRefreshToken_whenNotRememberSkipsRedis() {
        UserTokenDTO dto = new UserTokenDTO(1L, 2L, 3L, false);

        String token = service.createRefreshToken(dto);

        assertThat(token).isNotBlank();
        verify(valueOperations, never()).set(any(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void removeToken_deletesFromRedisAndLogsOut() {
        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(10L);

        service.removeToken("refresh");

        verify(stringRedisTemplate).delete(LOGIN_REFRESH_TOKEN_KEY + "refresh");
        assertThat(StpUtil.isLogin()).isFalse();
    }

    @Test
    void removeToken_whenRedisDeleteFailsThrows() {
        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(10L);
        doThrow(new RuntimeException("fail"))
                .when(stringRedisTemplate)
                .delete(LOGIN_REFRESH_TOKEN_KEY + "bad");

        ServiceException ex =
                assertThrows(ServiceException.class, () -> service.removeToken("bad"));

        assertThat(ex.getCode()).isEqualTo(EXPIRED_LOGIN_CREDENTIALS.getCode());
    }

    @Test
    void getUserIdFromRefreshToken_returnsUserId() {
        when(valueOperations.get(LOGIN_REFRESH_TOKEN_KEY + "token")).thenReturn("5");

        Long userId = service.getUserIdFromRefreshToken("token");

        assertThat(userId).isEqualTo(5L);
    }

    @Test
    void getUserIdFromRefreshToken_whenMissingReturnsNull() {
        when(valueOperations.get(LOGIN_REFRESH_TOKEN_KEY + "missing")).thenReturn(null);

        Long userId = service.getUserIdFromRefreshToken("missing");

        assertThat(userId).isNull();
    }
}
