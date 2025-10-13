package nus.edu.u.event.domain.dto.group;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class AddMembersReqVO {

    @NotEmpty(message = "User ID list cannot be empty")
    private List<Long> userIds;
}
