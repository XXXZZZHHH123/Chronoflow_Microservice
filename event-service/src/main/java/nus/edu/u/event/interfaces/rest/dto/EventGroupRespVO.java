package nus.edu.u.event.interfaces.rest.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventGroupRespVO {

    private Long id;

    private String name;

    private List<Member> members;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Member {

        private Long id;

        private String username;
    }
}
