package nus.edu.u.shared.rpc.user;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class UserProfileDTO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String email;
    private String phone;
    private List<Long> roles;
    private boolean isRegistered;
}
