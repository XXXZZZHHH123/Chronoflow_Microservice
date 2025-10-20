package nus.edu.u.user.domain.vo.role;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nus.edu.u.user.domain.vo.permission.PermissionRespVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRespVO {

    private Long id;

    private String name;

    private String key;

    @Builder.Default private Boolean isDefault = false;

    private List<PermissionRespVO> permissions;
}
