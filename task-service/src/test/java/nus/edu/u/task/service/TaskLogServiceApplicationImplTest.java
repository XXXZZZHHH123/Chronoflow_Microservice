package nus.edu.u.task.service;

import static nus.edu.u.common.enums.ErrorCodeConstants.TASK_LOG_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.shared.rpc.file.FileResultDTO;
import nus.edu.u.shared.rpc.file.FileStorageRpcService;
import nus.edu.u.shared.rpc.user.UserInfoDTO;
import nus.edu.u.shared.rpc.user.UserRpcService;
import nus.edu.u.task.domain.dataobject.task.TaskLogDO;
import nus.edu.u.task.domain.vo.taskLog.TaskLogRespVO;
import nus.edu.u.task.mapper.TaskLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskLogServiceApplicationImplTest {

    @Mock private TaskLogMapper taskLogMapper;
    @Mock private UserRpcService userRpcService;
    @Mock private FileStorageRpcService fileStorageRpcService;

    @InjectMocks private TaskLogServiceApplicationImpl service;

    private TaskLogDO sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog =
                TaskLogDO.builder()
                        .id(100L)
                        .taskId(50L)
                        .action(1)
                        .targetUserId(200L)
                        .remark("Remark")
                        .build();
        sampleLog.setCreator("300");
        sampleLog.setCreateTime(LocalDateTime.now());
    }

    @Test
    void insertTaskLog_persistsAndReturnsId() {
        doAnswer(
                        invocation -> {
                            TaskLogDO log = invocation.getArgument(0);
                            log.setId(999L);
                            return 1;
                        })
                .when(taskLogMapper)
                .insert(any(TaskLogDO.class));

        Long id = service.insertTaskLog(10L, 20L, 3, "hello");

        assertThat(id).isEqualTo(999L);
        ArgumentCaptor<TaskLogDO> captor = ArgumentCaptor.forClass(TaskLogDO.class);
        verify(taskLogMapper).insert(captor.capture());
        TaskLogDO saved = captor.getValue();
        assertThat(saved.getTaskId()).isEqualTo(10L);
        assertThat(saved.getTargetUserId()).isEqualTo(20L);
        assertThat(saved.getAction()).isEqualTo(3);
        assertThat(saved.getRemark()).isEqualTo("hello");
    }

    @Test
    void insertTaskLog_failureThrowsServiceException() {
        when(taskLogMapper.insert(any(TaskLogDO.class))).thenReturn(0);

        ServiceException ex =
                assertThrows(
                        ServiceException.class, () -> service.insertTaskLog(1L, 2L, 3, "fail"));
        assertThat(ex.getCode()).isEqualTo(TASK_LOG_ERROR.getCode());
    }

    @Test
    void getTaskLog_returnsTransformedLogs() {
        when(taskLogMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(sampleLog));

        UserInfoDTO target =
                UserInfoDTO.builder().id(200L).username("Alice").email("alice@example.com").build();
        UserInfoDTO source =
                UserInfoDTO.builder().id(300L).username("Bob").email("bob@example.com").build();
        when(userRpcService.getUsers(any())).thenReturn(Map.of(200L, target, 300L, source));
        when(fileStorageRpcService.downloadFilesByTaskLogId(100L))
                .thenReturn(
                        List.of(
                                FileResultDTO.builder()
                                        .objectName("obj")
                                        .name("file.txt")
                                        .build()));

        List<TaskLogRespVO> logs = service.getTaskLog(50L);

        assertThat(logs).hasSize(1);
        TaskLogRespVO log = logs.get(0);
        assertThat(log.getTargetUser().getName()).isEqualTo("Alice");
        assertThat(log.getSourceUser().getName()).isEqualTo("Bob");
        assertThat(log.getFileResults()).hasSize(1);
        verify(fileStorageRpcService).downloadFilesByTaskLogId(100L);
    }

    @Test
    void getTaskLog_handlesMissingUsersGracefully() {
        TaskLogDO log =
                TaskLogDO.builder()
                        .id(101L)
                        .taskId(60L)
                        .action(2)
                        .targetUserId(400L)
                        .remark("none")
                        .build();
        log.setCreator(null);
        log.setCreateTime(LocalDateTime.now());

        when(taskLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(log));
        when(userRpcService.getUsers(any())).thenReturn(java.util.Collections.emptyMap());
        when(fileStorageRpcService.downloadFilesByTaskLogId(101L)).thenReturn(List.of());

        List<TaskLogRespVO> logs = service.getTaskLog(60L);

        assertThat(logs).hasSize(1);
        TaskLogRespVO respVO = logs.get(0);
        assertThat(respVO.getTargetUser()).isNull();
        assertThat(respVO.getSourceUser()).isNotNull();
        assertThat(respVO.getFileResults()).isEmpty();
    }

    @Test
    void getTaskLog_whenNoRecords_returnsEmptyList() {
        when(taskLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        assertThat(service.getTaskLog(1L)).isEmpty();
    }

    @Test
    void getTaskLog_whenSelectListNull_returnsEmptyList() {
        when(taskLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThat(service.getTaskLog(777L)).isEmpty();
        verify(userRpcService, never()).getUsers(any());
        verify(fileStorageRpcService, never()).downloadFilesByTaskLogId(anyLong());
    }

    @Test
    void getTaskLog_whenUserServiceReturnsNull_usesEmptyUserMap() {
        when(taskLogMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(sampleLog));
        when(userRpcService.getUsers(any())).thenReturn(null);
        when(fileStorageRpcService.downloadFilesByTaskLogId(sampleLog.getId())).thenReturn(null);

        List<TaskLogRespVO> logs = service.getTaskLog(sampleLog.getTaskId());

        assertThat(logs).hasSize(1);
        TaskLogRespVO log = logs.get(0);
        assertThat(log.getTargetUser()).isNull();
        assertThat(log.getSourceUser()).isNotNull();
        assertThat(log.getFileResults()).isNull();
        verify(userRpcService).getUsers(any());
        verify(fileStorageRpcService).downloadFilesByTaskLogId(sampleLog.getId());
    }
}
