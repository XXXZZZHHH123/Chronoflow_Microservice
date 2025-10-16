package nus.edu.u.framework.security.satoken;

import static nus.edu.u.common.constant.CacheConstants.USER_PERMISSION_KEY;
import static nus.edu.u.common.constant.CacheConstants.USER_ROLE_KEY;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * @author Lu Shuwen
 * @date 2025-10-15
 */
@Component
public class StpPermissionHandler implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session != null && session.get(USER_PERMISSION_KEY) != null) {
            return (List<String>) session.get(USER_PERMISSION_KEY);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session != null && session.get(USER_ROLE_KEY) != null) {
            return (List<String>) session.get(USER_ROLE_KEY);
        }
        return Collections.emptyList();
    }
}
