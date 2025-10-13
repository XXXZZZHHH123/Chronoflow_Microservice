package nus.edu.u.shared.rpc.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long contactUserId;

    private String contactName;

    private String contactMobile;

    private String address;

    private Integer status;

    private String tenantCode;
}
