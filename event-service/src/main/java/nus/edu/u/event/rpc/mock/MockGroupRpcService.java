package nus.edu.u.event.rpc.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nus.edu.u.shared.rpc.group.GroupDTO;
import nus.edu.u.shared.rpc.group.GroupMemberDTO;
import nus.edu.u.shared.rpc.group.GroupRpcService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class MockGroupRpcService implements GroupRpcService {

    @Override
    public Map<Long, List<GroupDTO>> getGroupsByEventIds(Collection<Long> eventIds) {
        Map<Long, List<GroupDTO>> result = new HashMap<>();
        if (eventIds == null) {
            return result;
        }
        for (Long eventId : eventIds) {
            if (eventId == null) {
                continue;
            }
            result.put(
                    eventId,
                    List.of(
                            GroupDTO.builder()
                                    .eventId(eventId)
                                    .id(500L)
                                    .name("Core Team")
                                    .members(
                                            List.of(
                                                    GroupMemberDTO.builder()
                                                            .userId(1976858423508111361L)
                                                            .username("lushuwen1")
                                                            .build(),
                                                    GroupMemberDTO.builder()
                                                            .userId(2001L)
                                                            .username("alice")
                                                            .build()))
                                    .build()));
        }
        return result;
    }
}
