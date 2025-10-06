package nus.edu.u.user.domain.vo.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nus.edu.u.user.domain.vo.permission.PermissionRespVO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRespVO {

    private Long id;

    private String name;

    private String key;

    private Boolean isDefault = false;

    private List<PermissionRespVO> permissions;
}
