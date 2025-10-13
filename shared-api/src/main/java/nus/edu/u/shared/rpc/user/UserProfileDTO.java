package nus.edu.u.shared.rpc.user;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileDTO{
    private Long id;
    private String name;
    private String email;
    private String phone;
    private List<Long> roles;
    private boolean isRegistered;
}
