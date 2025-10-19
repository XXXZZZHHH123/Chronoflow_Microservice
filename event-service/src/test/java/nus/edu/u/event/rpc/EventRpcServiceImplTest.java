package nus.edu.u.event.rpc;

import static nus.edu.u.common.enums.ErrorCodeConstants.EVENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.event.convert.EventConvert;
import nus.edu.u.event.domain.dataobject.event.EventDO;
import nus.edu.u.event.domain.dataobject.user.UserGroupDO;
import nus.edu.u.event.domain.dto.event.EventRespVO;
import nus.edu.u.event.domain.dto.group.GroupRespVO;
import nus.edu.u.event.mapper.EventMapper;
import nus.edu.u.event.mapper.UserGroupMapper;
import nus.edu.u.event.service.GroupApplicationService;
import nus.edu.u.shared.rpc.events.EventRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventRpcServiceImplTest {

    @Mock private EventConvert eventConvert;
    @Mock private EventMapper eventMapper;
    @Mock private UserGroupMapper userGroupMapper;
    @Mock private GroupApplicationService groupApplicationService;

    @InjectMocks private EventRpcServiceImpl rpcService;

    private EventDO event;

    @BeforeEach
    void init() {
        event =
                EventDO.builder()
                        .id(33L)
                        .userId(44L)
                        .name("Tech Day")
                        .description("desc")
                        .startTime(LocalDateTime.of(2025, 1, 10, 9, 0))
                        .endTime(LocalDateTime.of(2025, 1, 10, 17, 0))
                        .status(1)
                        .remark("remark")
                        .build();
        event.setCreateTime(LocalDateTime.of(2024, 12, 1, 9, 0));
    }

    @Test
    void getEvent_convertsAndAggregatesExtras() {
        EventRespVO converted = new EventRespVO();
        converted.setId(event.getId());
        converted.setJoiningParticipants(0);
        converted.setGroups(List.of());

        when(eventMapper.selectById(event.getId())).thenReturn(event);
        when(eventConvert.DOconvertVO(event)).thenReturn(converted);
        when(userGroupMapper.selectList(any()))
                .thenReturn(
                        List.of(
                                UserGroupDO.builder().eventId(event.getId()).userId(1L).build(),
                                UserGroupDO.builder().eventId(event.getId()).userId(1L).build(),
                                UserGroupDO.builder().eventId(event.getId()).userId(2L).build()));
        when(groupApplicationService.getGroupsByEvent(event.getId()))
                .thenReturn(List.of(GroupRespVO.builder().id(5L).name("Group A").build()));
        when(eventConvert.toRpc(converted))
                .thenAnswer(
                        invocation -> {
                            EventRespVO source = invocation.getArgument(0);
                            EventRespDTO dto = new EventRespDTO();
                            dto.setId(source.getId());
                            dto.setJoiningParticipants(source.getJoiningParticipants());
                            dto.setGroups(
                                    source.getGroups().stream()
                                            .map(
                                                    g -> {
                                                        EventRespDTO.GroupVO vo =
                                                                new EventRespDTO.GroupVO();
                                                        vo.setId(g.getId());
                                                        vo.setName(g.getName());
                                                        return vo;
                                                    })
                                            .toList());
                            return dto;
                        });

        EventRespDTO dto = rpcService.getEvent(event.getId());

        assertThat(dto.getId()).isEqualTo(event.getId());
        assertThat(dto.getJoiningParticipants()).isEqualTo(2); // distinct participants
        assertThat(dto.getGroups())
                .singleElement()
                .matches(group -> group.getName().equals("Group A"));
    }

    @Test
    void getEvent_usesFallbackConversionWhenConvertReturnsNull() {
        when(eventMapper.selectById(event.getId())).thenReturn(event);
        when(eventConvert.DOconvertVO(event)).thenReturn(null);
        when(eventConvert.toRpc(any(EventRespVO.class)))
                .thenAnswer(
                        invocation -> {
                            EventRespVO source = invocation.getArgument(0);
                            EventRespDTO dto = new EventRespDTO();
                            dto.setId(source.getId());
                            dto.setName(source.getName());
                            return dto;
                        });
        when(groupApplicationService.getGroupsByEvent(event.getId())).thenReturn(List.of());
        when(userGroupMapper.selectList(any())).thenReturn(List.of());

        EventRespDTO dto = rpcService.getEvent(event.getId());

        assertThat(dto.getName()).isEqualTo(event.getName());
    }

    @Test
    void getEvent_throwsWhenEventMissing() {
        when(eventMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> rpcService.getEvent(1L))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(EVENT_NOT_FOUND.getCode());
    }

    @Test
    void exists_returnsFalseWhenNullOrMissing() {
        assertThat(rpcService.exists(null)).isFalse();
        when(eventMapper.selectById(100L)).thenReturn(null);
        assertThat(rpcService.exists(100L)).isFalse();
    }

    @Test
    void exists_returnsTrueWhenEventPresent() {
        when(eventMapper.selectById(event.getId())).thenReturn(event);
        assertThat(rpcService.exists(event.getId())).isTrue();
    }
}
