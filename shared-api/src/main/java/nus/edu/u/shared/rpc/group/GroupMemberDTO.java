package nus.edu.u.shared.rpc.group;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDTO implements Serializable {
    private Long userId;
    private String username;
}
