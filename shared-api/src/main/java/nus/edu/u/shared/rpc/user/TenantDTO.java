package nus.edu.u.shared.rpc.user;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long contactUserId;

    private String contactName;

    private String contactMobile;

    private String address;

    private Integer status;

    private String tenantCode;
}
