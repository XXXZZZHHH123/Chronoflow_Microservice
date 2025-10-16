package nus.edu.u.shared.rpc.group;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO implements Serializable {
    private Long eventId;
    private Long id;
    private String name;
    private Integer sort;
    private Long leadUserId;
    private String remark;
    private Integer status;
    private List<GroupMemberDTO> members;
}
