package nus.edu.u.framework.mybatis.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Tenant basic class
 *
 * @author Lu Shuwen
 * @date 2025-08-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TenantBaseDO extends BaseDO {

    private Long tenantId;
}
