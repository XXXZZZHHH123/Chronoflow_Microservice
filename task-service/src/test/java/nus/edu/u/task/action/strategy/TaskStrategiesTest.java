package nus.edu.u.task.action.strategy;

import static nus.edu.u.common.enums.ErrorCodeConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import java.time.LocalDateTime;
import java.util.List;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.shared.rpc.file.FileStorageRpcService;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dto.TaskActionDTO;
import nus.edu.u.task.enums.TaskActionEnum;
import nus.edu.u.task.enums.TaskStatusEnum;
import nus.edu.u.task.mapper.TaskMapper;
import nus.edu.u.task.service.TaskLogApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskStrategiesTest {

    @Mock private TaskMapper taskMapper;
    @Mock private TaskLogApplicationService taskLogService;
    @Mock private FileStorageRpcService fileStorageRpcService;

    private CreateTask createTask;
    private AssignTask assignTask;
    private AcceptTask acceptTask;
    private ApproveTask approveTask;
    private BlockTask blockTask;
    private SubmitTask submitTask;
    private RejectTask rejectTask;
    private DeleteTask deleteTask;
    private UpdateTask updateTask;

    @BeforeEach
    void setUp() {
        createTask = initStrategy(new CreateTask());
        assignTask = initStrategy(new AssignTask());
        acceptTask = initStrategy(new AcceptTask());
        approveTask = initStrategy(new ApproveTask());
        blockTask = initStrategy(new BlockTask());
        submitTask = initStrategy(new SubmitTask());
        rejectTask = initStrategy(new RejectTask());
        deleteTask = initStrategy(new DeleteTask());
        updateTask = initStrategy(new UpdateTask());
    }

    @AfterEach
    void tearDown() {
        SaTokenContextMockUtil.clearContext();
    }

    private <T> T initStrategy(T strategy) {
        ReflectionTestUtils.setField(strategy, "taskMapper", taskMapper);
        ReflectionTestUtils.setField(strategy, "taskLogService", taskLogService);
        ReflectionTestUtils.setField(strategy, "fileStorageRpcService", fileStorageRpcService);
        return strategy;
    }

    // deterministic helper so each test starts from the same baseline TaskDO
    private TaskDO baseTask() {
        TaskDO task =
                TaskDO.builder()
                        .id(100L)
                        .eventId(200L)
                        .userId(300L)
                        .status(TaskStatusEnum.PENDING.getStatus())
                        .startTime(LocalDateTime.of(2025, 1, 1, 9, 0))
                        .endTime(LocalDateTime.of(2025, 1, 1, 11, 0))
                        .name("Initial")
                        .description("desc")
                        .build();
        task.setCreator("creator");
        return task;
    }

    private TaskActionDTO baseActionDto() {
        return TaskActionDTO.builder()
                .eventStartTime(LocalDateTime.of(2025, 1, 1, 8, 0))
                .eventEndTime(LocalDateTime.of(2025, 1, 1, 18, 0))
                .targetUserId(400L)
                .remark("remark")
                .build();
    }

    @Test
    void createTask_successfullyInsertsAndUploadsFiles() {
        TaskDO task = baseTask();
        MockMultipartFile file =
                new MockMultipartFile("doc", "doc.txt", "text/plain", "hi".getBytes());
        TaskActionDTO dto = baseActionDto();
        dto.setFiles(List.of(file));
        dto.setTargetUserId(500L);

        when(taskMapper.insert(task)).thenReturn(1);
        when(taskLogService.insertTaskLog(
                        task.getId(), 500L, TaskActionEnum.CREATE.getCode(), "remark"))
                .thenReturn(88L);

        createTask.execute(task, dto);

        assertThat(task.getStatus()).isEqualTo(TaskStatusEnum.PENDING.getStatus());
        assertThat(task.getUserId()).isEqualTo(500L);
        verify(taskMapper).insert(task);
        verify(taskLogService)
                .insertTaskLog(task.getId(), 500L, TaskActionEnum.CREATE.getCode(), "remark");
        ArgumentCaptor<nus.edu.u.shared.rpc.file.FileUploadReqDTO> uploadCaptor =
                ArgumentCaptor.forClass(nus.edu.u.shared.rpc.file.FileUploadReqDTO.class);
        verify(fileStorageRpcService).uploadToTaskLog(uploadCaptor.capture());
        assertThat(uploadCaptor.getValue().getTaskLogId()).isEqualTo(88L);
        assertThat(uploadCaptor.getValue().getEventId()).isEqualTo(task.getEventId());
        assertThat(uploadCaptor.getValue().getFiles()).hasSize(1);
    }

    @Test
    void createTask_whenInsertFails_throwsException() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();
        when(taskMapper.insert(task)).thenReturn(0);

        assertThatThrownBy(() -> createTask.execute(task, dto))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(TASK_CREATE_FAILED.getCode());

        verify(taskLogService, never()).insertTaskLog(any(), any(), any(), any());
        verify(fileStorageRpcService, never()).uploadToTaskLog(any());
    }

    @Test
    void createTask_whenEndAfterEvent_marksDelayedAndThrows() {
        TaskDO task = baseTask();
        task.setEndTime(LocalDateTime.of(2025, 1, 1, 20, 0));
        TaskActionDTO dto = baseActionDto();

        when(taskMapper.updateById(task)).thenReturn(1);

        assertThatThrownBy(() -> createTask.execute(task, dto))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(TASK_TIME_OUTSIDE_EVENT.getCode());
        assertThat(task.getStatus()).isEqualTo(TaskStatusEnum.DELAYED.getStatus());
        verify(taskMapper).updateById(task);
        verify(taskMapper, never()).insert(task);
    }

    @Test
    void assignTask_updatesAssigneeAndStatus() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();

        when(taskMapper.updateById(task)).thenReturn(1);
        when(taskLogService.insertTaskLog(
                        task.getId(), 400L, TaskActionEnum.ASSIGN.getCode(), "remark"))
                .thenReturn(66L);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:assign")).thenAnswer(invocation -> null);

            assignTask.execute(task, dto);

            assertThat(task.getStatus()).isEqualTo(TaskStatusEnum.PENDING.getStatus());
            assertThat(task.getUserId()).isEqualTo(400L);
            verify(taskMapper).updateById(task);
            verify(taskLogService)
                    .insertTaskLog(task.getId(), 400L, TaskActionEnum.ASSIGN.getCode(), "remark");
            verify(fileStorageRpcService, never()).uploadToTaskLog(any());
        }
    }

    @Test
    void assignTask_whenUpdateFails_throwsException() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();
        when(taskMapper.updateById(task)).thenReturn(0);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:assign")).thenAnswer(invocation -> null);

            assertThatThrownBy(() -> assignTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(ASSIGN_TASK_FAILED.getCode());
        }
    }

    @Test
    void acceptTask_successfullyMovesToInProgress() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();
        dto.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 30));
        dto.setEndTime(LocalDateTime.of(2025, 1, 1, 10, 30));
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());
            when(taskMapper.updateById(task)).thenReturn(1);

            acceptTask.execute(task, dto);

            assertThat(task.getStatus()).isEqualTo(TaskStatusEnum.PROGRESS.getStatus());
            verify(taskMapper).updateById(task);
            verify(taskLogService)
                    .insertTaskLog(task.getId(), null, TaskActionEnum.ACCEPT.getCode(), "remark");
        }
    }

    @Test
    void acceptTask_wrongStatusThrows() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.COMPLETED.getStatus());
        TaskActionDTO dto = baseActionDto();
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());

            assertThatThrownBy(() -> acceptTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(MODIFY_WRONG_TASK_STATUS.getCode());
        }
    }

    @Test
    void acceptTask_whenNotOwner_throws() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(999L);

            assertThatThrownBy(() -> acceptTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(MODIFY_OTHER_TASK_ERROR.getCode());
        }
    }

    @Test
    void acceptTask_whenUpdateFails_throws() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();
        dto.setStartTime(task.getStartTime());
        dto.setEndTime(task.getEndTime());
        when(taskMapper.updateById(task)).thenReturn(0);
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());

            assertThatThrownBy(() -> acceptTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(ACCEPT_TASK_FAILED.getCode());
        }
    }

    @Test
    void approveTask_allowsOrganizerToComplete() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PENDING_APPROVAL.getStatus());
        task.setCreator("owner");
        TaskActionDTO dto = baseActionDto();
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:update")).thenAnswer(invocation -> null);
            stp.when(StpUtil::getLoginId).thenReturn("owner");
            when(taskMapper.updateById(task)).thenReturn(1);

            approveTask.execute(task, dto);

            assertThat(task.getStatus()).isEqualTo(TaskStatusEnum.COMPLETED.getStatus());
            verify(taskLogService)
                    .insertTaskLog(task.getId(), null, TaskActionEnum.APPROVE.getCode(), "remark");
        }
    }

    @Test
    void approveTask_whenNotCreator_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PENDING_APPROVAL.getStatus());
        task.setCreator("owner");
        TaskActionDTO dto = baseActionDto();
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:update")).thenAnswer(invocation -> null);
            stp.when(StpUtil::getLoginId).thenReturn("someone");

            assertThatThrownBy(() -> approveTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(MODIFY_OTHER_TASK_ERROR.getCode());
        }
    }

    @Test
    void approveTask_whenStatusInvalid_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PENDING.getStatus());
        task.setCreator("owner");
        TaskActionDTO dto = baseActionDto();
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:update")).thenAnswer(invocation -> null);
            stp.when(StpUtil::getLoginId).thenReturn("owner");

            assertThatThrownBy(() -> approveTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(MODIFY_WRONG_TASK_STATUS.getCode());
        }
    }

    @Test
    void approveTask_whenUpdateFails_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PENDING_APPROVAL.getStatus());
        task.setCreator("owner");
        TaskActionDTO dto = baseActionDto();
        when(taskMapper.updateById(task)).thenReturn(0);
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:update")).thenAnswer(invocation -> null);
            stp.when(StpUtil::getLoginId).thenReturn("owner");

            assertThatThrownBy(() -> approveTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(APPROVE_TASK_FAILED.getCode());
        }
    }

    @Test
    void blockTask_updatesStatusAndLogs() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        MockMultipartFile file =
                new MockMultipartFile("e", "e.txt", "text/plain", "data".getBytes());
        TaskActionDTO dto = baseActionDto();
        dto.setFiles(List.of(file));
        when(taskMapper.updateById(task)).thenReturn(1);
        when(taskLogService.insertTaskLog(
                        task.getId(), null, TaskActionEnum.BLOCK.getCode(), "remark"))
                .thenReturn(70L);

        blockTask.execute(task, dto);

        assertThat(task.getStatus()).isEqualTo(TaskStatusEnum.BLOCKED.getStatus());
        verify(fileStorageRpcService).uploadToTaskLog(any());
    }

    @Test
    void blockTask_wrongStatusThrows() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();

        assertThatThrownBy(() -> blockTask.execute(task, dto))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(MODIFY_WRONG_TASK_STATUS.getCode());
    }

    @Test
    void blockTask_whenUpdateFails_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        TaskActionDTO dto = baseActionDto();
        when(taskMapper.updateById(task)).thenReturn(0);

        assertThatThrownBy(() -> blockTask.execute(task, dto))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(BLOCK_TASK_FAILED.getCode());
    }

    @Test
    void submitTask_movesToPendingApproval() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        TaskActionDTO dto = baseActionDto();
        when(taskMapper.updateById(task)).thenReturn(1);
        when(taskLogService.insertTaskLog(
                        task.getId(), null, TaskActionEnum.SUBMIT.getCode(), "remark"))
                .thenReturn(44L);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());

            submitTask.execute(task, dto);

            assertThat(task.getStatus()).isEqualTo(TaskStatusEnum.PENDING_APPROVAL.getStatus());
        }
    }

    @Test
    void submitTask_whenWrongUser_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        TaskActionDTO dto = baseActionDto();

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(999L);

            assertThatThrownBy(() -> submitTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(MODIFY_OTHER_TASK_ERROR.getCode());
        }
    }

    @Test
    void submitTask_whenStatusInvalid_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PENDING.getStatus());
        TaskActionDTO dto = baseActionDto();
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());

            assertThatThrownBy(() -> submitTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(MODIFY_WRONG_TASK_STATUS.getCode());
        }
    }

    @Test
    void submitTask_whenUpdateFails_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        TaskActionDTO dto = baseActionDto();
        when(taskMapper.updateById(task)).thenReturn(0);
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());

            assertThatThrownBy(() -> submitTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(SUBMIT_TASK_FAILED.getCode());
        }
    }

    @Test
    void rejectTask_updatesStatus() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();
        when(taskMapper.updateById(task)).thenReturn(1);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());

            rejectTask.execute(task, dto);

            assertThat(task.getStatus()).isEqualTo(TaskStatusEnum.REJECTED.getStatus());
            verify(taskLogService)
                    .insertTaskLog(task.getId(), null, TaskActionEnum.REJECT.getCode(), "remark");
        }
    }

    @Test
    void rejectTask_whenUpdateFails_throws() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();
        when(taskMapper.updateById(task)).thenReturn(0);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());

            assertThatThrownBy(() -> rejectTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(REJECT_TASK_FAILED.getCode());
        }
    }

    @Test
    void rejectTask_whenNotOwner_throws() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(999L);

            assertThatThrownBy(() -> rejectTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(MODIFY_OTHER_TASK_ERROR.getCode());
        }
    }

    @Test
    void rejectTask_whenStatusInvalid_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        TaskActionDTO dto = baseActionDto();
        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());

            assertThatThrownBy(() -> rejectTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(MODIFY_WRONG_TASK_STATUS.getCode());
        }
    }

    @Test
    void deleteTask_successfulDeletionLogsEntry() {
        TaskDO task = baseTask();
        when(taskMapper.deleteById(task.getId())).thenReturn(1);

        deleteTask.execute(task, baseActionDto());

        verify(taskMapper).deleteById(task.getId());
        verify(taskLogService)
                .insertTaskLog(task.getId(), null, TaskActionEnum.DELETE.getCode(), null);
    }

    @Test
    void deleteTask_whenDeleteFails_throws() {
        TaskDO task = baseTask();
        when(taskMapper.deleteById(task.getId())).thenReturn(0);

        assertThatThrownBy(() -> deleteTask.execute(task, baseActionDto()))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(TASK_DELETE_FAILED.getCode());
    }

    @Test
    void updateTask_updatesEditableFields() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        MockMultipartFile file = new MockMultipartFile("c", "c.txt", "text/plain", "x".getBytes());
        TaskActionDTO dto = baseActionDto();
        dto.setName("updated");
        dto.setDescription("desc2");
        dto.setStartTime(task.getStartTime());
        dto.setEndTime(task.getEndTime());
        dto.setFiles(List.of(file));
        when(taskMapper.updateById(task)).thenReturn(1);
        when(taskLogService.insertTaskLog(
                        task.getId(), null, TaskActionEnum.UPDATE.getCode(), "remark"))
                .thenReturn(123L);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:update")).thenAnswer(invocation -> null);

            updateTask.execute(task, dto);

            assertThat(task.getName()).isEqualTo("updated");
            assertThat(task.getStatus()).isEqualTo(TaskStatusEnum.PENDING.getStatus());
            verify(fileStorageRpcService).uploadToTaskLog(any());
        }
    }

    @Test
    void updateTask_whenCompleted_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.COMPLETED.getStatus());
        TaskActionDTO dto = baseActionDto();

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:update")).thenAnswer(invocation -> null);

            assertThatThrownBy(() -> updateTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(MODIFY_WRONG_TASK_STATUS.getCode());
        }
    }

    @Test
    void updateTask_whenUpdateFails_throws() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        TaskActionDTO dto = baseActionDto();
        dto.setStartTime(task.getStartTime());
        dto.setEndTime(task.getEndTime());
        when(taskMapper.updateById(task)).thenReturn(0);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:update")).thenAnswer(invocation -> null);

            assertThatThrownBy(() -> updateTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(TASK_UPDATE_FAILED.getCode());
        }
    }

    @Test
    void updateTask_whenUploadFieldsMissing_throwsTaskLogFileFailed() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        task.setEventId(null);
        TaskActionDTO dto = baseActionDto();
        dto.setFiles(List.of(new MockMultipartFile("f", "f.txt", null, "d".getBytes())));

        when(taskMapper.updateById(task)).thenReturn(1);
        when(taskLogService.insertTaskLog(
                        task.getId(), null, TaskActionEnum.UPDATE.getCode(), "remark"))
                .thenReturn(22L);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(() -> StpUtil.checkPermission("task:update")).thenAnswer(invocation -> null);

            assertThatThrownBy(() -> updateTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(TASK_LOG_FILE_FAILED.getCode());
        }
    }

    @Test
    void createTask_whenStartNotBeforeEnd_throwsInvalidRange() {
        TaskDO task = baseTask();
        task.setStartTime(task.getEndTime());
        TaskActionDTO dto = baseActionDto();

        assertThatThrownBy(() -> createTask.execute(task, dto))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(TASK_TIME_RANGE_INVALID.getCode());
        verify(taskMapper, never()).insert(task);
    }

    @Test
    void createTask_whenStartBeforeEventStart_throwsOutsideEvent() {
        TaskDO task = baseTask();
        TaskActionDTO dto = baseActionDto();
        dto.setEventStartTime(task.getStartTime().plusHours(1));

        assertThatThrownBy(() -> createTask.execute(task, dto))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(TASK_TIME_OUTSIDE_EVENT.getCode());
        verify(taskMapper, never()).insert(task);
    }

    @Test
    void submitTask_whenTaskLogIdNull_throwsTaskLogFileFailed() {
        TaskDO task = baseTask();
        task.setStatus(TaskStatusEnum.PROGRESS.getStatus());
        TaskActionDTO dto = baseActionDto();
        dto.setFiles(List.of(new MockMultipartFile("g", "g.txt", "text/plain", "data".getBytes())));
        when(taskMapper.updateById(task)).thenReturn(1);
        when(taskLogService.insertTaskLog(
                        task.getId(), null, TaskActionEnum.SUBMIT.getCode(), "remark"))
                .thenReturn(null);

        try (MockedStatic<StpUtil> stp = Mockito.mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn(task.getUserId());

            assertThatThrownBy(() -> submitTask.execute(task, dto))
                    .isInstanceOf(ServiceException.class)
                    .extracting("code")
                    .isEqualTo(TASK_LOG_FILE_FAILED.getCode());
        }
        verify(fileStorageRpcService, never()).uploadToTaskLog(any());
    }
}
