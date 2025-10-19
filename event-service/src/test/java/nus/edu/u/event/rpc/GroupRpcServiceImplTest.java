package nus.edu.u.event.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import nus.edu.u.event.service.GroupApplicationService;
import nus.edu.u.shared.rpc.group.GroupDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupRpcServiceImplTest {

    @Mock private GroupApplicationService groupApplicationService;
    @InjectMocks private GroupRpcServiceImpl rpcService;

    @Test
    void getGroupsByEventIds_delegatesToApplicationService() {
        Collection<Long> ids = List.of(1L, 2L);
        Map<Long, List<GroupDTO>> expected =
                Map.of(1L, List.of(GroupDTO.builder().id(10L).build()));
        when(groupApplicationService.getGroupDTOsByEventIds(ids)).thenReturn(expected);

        Map<Long, List<GroupDTO>> result = rpcService.getGroupsByEventIds(ids);

        assertThat(result).isSameAs(expected);
        verify(groupApplicationService).getGroupDTOsByEventIds(ids);
    }
}
