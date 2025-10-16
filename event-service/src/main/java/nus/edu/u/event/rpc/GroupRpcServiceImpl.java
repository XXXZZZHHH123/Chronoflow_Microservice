package nus.edu.u.event.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nus.edu.u.event.service.GroupApplicationService;
import nus.edu.u.shared.rpc.group.GroupDTO;
import nus.edu.u.shared.rpc.group.GroupRpcService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class GroupRpcServiceImpl implements GroupRpcService {

    private final GroupApplicationService groupApplicationService;

    @Override
    public Map<Long, List<GroupDTO>> getGroupsByEventIds(Collection<Long> eventIds) {
        return groupApplicationService.getGroupDTOsByEventIds(eventIds);
    }
}
