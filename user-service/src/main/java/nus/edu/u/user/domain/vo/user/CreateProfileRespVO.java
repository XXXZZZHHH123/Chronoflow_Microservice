package nus.edu.u.user.domain.vo.user;

import lombok.Data;

@Data
public class CreateProfileRespVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Integer status;
}
