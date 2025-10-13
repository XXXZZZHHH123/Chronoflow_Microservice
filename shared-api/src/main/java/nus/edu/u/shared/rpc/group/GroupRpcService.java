package nus.edu.u.shared.rpc.group;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface GroupRpcService {

    Map<Long, List<GroupDTO>> getGroupsByEventIds(Collection<Long> eventIds);
}
