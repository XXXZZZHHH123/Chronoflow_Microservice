package nus.edu.u.task.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import java.util.List;
import java.util.Map;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.task.domain.vo.task.TaskCreateReqVO;
import nus.edu.u.task.domain.vo.task.TaskDashboardRespVO;
import nus.edu.u.task.domain.vo.task.TaskRespVO;
import nus.edu.u.task.domain.vo.task.TaskUpdateReqVO;
import nus.edu.u.task.domain.vo.taskLog.TaskLogRespVO;
import nus.edu.u.task.service.TaskApplicationService;
import nus.edu.u.task.service.TaskLogApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock private TaskApplicationService taskApplicationService;
    @Mock private TaskLogApplicationService taskLogApplicationService;
    @Mock private com.google.cloud.spring.pubsub.core.PubSubTemplate pubSubTemplate;
    @Mock private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks private TaskController controller;

    @BeforeEach
    void setUp() {
        SaTokenContextMockUtil.setMockContext();
    }

    @AfterEach
    void tearDown() {
        try {
            if (StpUtil.isLogin()) {
                StpUtil.logout();
            }
        } catch (Exception ignored) {
        }
        SaTokenContextMockUtil.clearContext();
    }

    @Test
    void create_returnsSuccessResponse() {
        long eventId = 11L;
        TaskCreateReqVO req = new TaskCreateReqVO();
        req.setName("Task");
        TaskRespVO resp = new TaskRespVO();
        when(taskApplicationService.createTask(eventId, req)).thenReturn(resp);

        CommonResult<TaskRespVO> result = controller.create(eventId, req);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isSameAs(resp);
        verify(taskApplicationService).createTask(eventId, req);
    }

    @Test
    void getTask_returnsTask() {
        long eventId = 2L;
        long taskId = 10L;
        TaskRespVO resp = new TaskRespVO();
        when(taskApplicationService.getTask(eventId, taskId)).thenReturn(resp);

        CommonResult<TaskRespVO> result = controller.getTask(eventId, taskId);

        assertThat(result.getData()).isSameAs(resp);
        verify(taskApplicationService).getTask(eventId, taskId);
    }

    @Test
    void listByEvent_returnsTasks() {
        long eventId = 9L;
        List<TaskRespVO> responses = List.of(new TaskRespVO());
        when(taskApplicationService.listTasksByEvent(eventId)).thenReturn(responses);

        CommonResult<List<TaskRespVO>> result = controller.listByEvent(eventId);

        assertThat(result.getData()).isSameAs(responses);
        verify(taskApplicationService).listTasksByEvent(eventId);
    }

    @Test
    void dashboard_usesCurrentLoginId() {
        StpUtil.login(555L);
        TaskDashboardRespVO dashboard = new TaskDashboardRespVO();
        when(taskApplicationService.getByMemberId(555L)).thenReturn(dashboard);

        CommonResult<TaskDashboardRespVO> result = controller.dashboard();

        assertThat(result.getData()).isSameAs(dashboard);
        verify(taskApplicationService).getByMemberId(555L);
    }

    @Test
    void update_delegatesToService() {
        long eventId = 5L;
        long taskId = 8L;
        TaskUpdateReqVO req = new TaskUpdateReqVO();
        req.setType(4);
        TaskRespVO resp = new TaskRespVO();
        when(taskApplicationService.updateTask(eventId, taskId, req, req.getType()))
                .thenReturn(resp);

        CommonResult<TaskRespVO> result = controller.update(eventId, taskId, req);

        assertThat(result.getData()).isSameAs(resp);
        verify(taskApplicationService).updateTask(eventId, taskId, req, req.getType());
    }

    @Test
    void delete_returnsTrue() {
        long eventId = 7L;
        long taskId = 12L;

        CommonResult<Boolean> result = controller.delete(eventId, taskId);

        assertThat(result.getData()).isTrue();
        verify(taskApplicationService).deleteTask(eventId, taskId);
    }

    @Test
    void logs_returnsTaskLogs() {
        long eventId = 21L;
        long taskId = 66L;
        List<TaskLogRespVO> logs = List.of(TaskLogRespVO.builder().id(taskId).build());
        when(taskLogApplicationService.getTaskLog(taskId)).thenReturn(logs);

        CommonResult<List<TaskLogRespVO>> result = controller.logs(eventId, taskId);

        assertThat(result.getData()).isSameAs(logs);
        verify(taskLogApplicationService).getTaskLog(taskId);
    }

    @Test
    void sendRawEmailPubSubMessage_publishesAndReturnsOk() {
        ResponseEntity<?> response = controller.sendRawEmailPubSubMessage();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody())
                .isInstanceOf(Map.class)
                .extracting(body -> ((Map<?, ?>) body).get("status"))
                .isEqualTo("PUBLISHED");
        verify(pubSubTemplate).publish(eq("chronoflow-notification"), anyString());
    }

    @Test
    void sendRawEmailPubSubMessage_whenPublishFails_returnsServerError() {
        doThrow(new RuntimeException("network down"))
                .when(pubSubTemplate)
                .publish(anyString(), anyString());

        ResponseEntity<?> response = controller.sendRawEmailPubSubMessage();

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody())
                .isInstanceOf(Map.class)
                .extracting(body -> ((Map<?, ?>) body).get("error").toString())
                .asString()
                .contains("network down");
    }
}
