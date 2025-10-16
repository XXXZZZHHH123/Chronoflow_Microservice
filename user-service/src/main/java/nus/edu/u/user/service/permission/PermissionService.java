package nus.edu.u.user.service.permission;

import java.util.List;
import nus.edu.u.user.domain.vo.permission.PermissionReqVO;
import nus.edu.u.user.domain.vo.permission.PermissionRespVO;

/**
 * @author Lu Shuwen
 * @date 2025-09-29
 */
public interface PermissionService {

    /**
     * Get All user permissions
     *
     * @return
     */
    List<PermissionRespVO> listPermissions();

    Long createPermission(PermissionReqVO permissionReqVO);

    PermissionRespVO getPermission(Long id);

    PermissionRespVO updatePermission(Long id, PermissionReqVO reqVO);

    Boolean deletePermission(Long id);
}
