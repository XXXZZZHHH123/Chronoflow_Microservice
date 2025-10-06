package nus.edu.u.user.domain.vo.role;

import lombok.Data;

import java.util.List;

/**
 * @author Lu Shuwen
 * @date 2025-09-29
 */
@Data
public class RoleAssignReqVO {

    private Long userId;

    private List<Long> roles;
}
