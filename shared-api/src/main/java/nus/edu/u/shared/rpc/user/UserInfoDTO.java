package nus.edu.u.shared.rpc.user;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private Integer status;
    private Long tenantId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String email;
    private String phone;
    private List<RoleBriefDTO> roles;
}
