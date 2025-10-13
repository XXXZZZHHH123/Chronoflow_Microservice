package nus.edu.u.event.rpc.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nus.edu.u.shared.rpc.user.UserInfoDTO;
import nus.edu.u.shared.rpc.user.UserRpcService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class MockUserRpcService implements UserRpcService {

    private final Map<Long, UserInfoDTO> store = new ConcurrentHashMap<>();

    public MockUserRpcService() {
        store.put(1976858423508111361L, UserInfoDTO.builder().id(1976858423508111361L).username("lushuwen1").status(1).build());
        store.put(2001L, UserInfoDTO.builder().id(2001L).username("alice").status(1).build());
        store.put(2002L, UserInfoDTO.builder().id(2002L).username("bob").status(1).build());
    }

    @Override
    public boolean exists(Long userId) {
        return store.containsKey(userId);
    }

    @Override
    public Map<Long, UserInfoDTO> getUsers(Collection<Long> userIds) {
        Map<Long, UserInfoDTO> result = new HashMap<>();
        if (userIds == null) {
            return result;
        }
        for (Long id : userIds) {
            if (id != null && store.containsKey(id)) {
                result.put(id, store.get(id));
            }
        }
        return result;
    }
}
