package nus.edu.u.task.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.task.domain.vo.task.TaskCreateReqVO;
import nus.edu.u.task.domain.vo.task.TaskRespVO;
import nus.edu.u.task.domain.vo.task.TaskUpdateReqVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Task Service Sentinel 限流降级处理器
 */
@Slf4j
@Component
public class TaskSentinelHandler {

    // ==================== 限流处理 BlockHandler ====================

    /**
     * 创建任务限流处理
     */
    public static TaskRespVO createTaskBlockHandler(
            Long eventId,
            TaskCreateReqVO request,
            BlockException ex) {
        log.warn("创建任务被限流, eventId: {}, 原因: {}", eventId, ex.getClass().getSimpleName());
        TaskRespVO resp = new TaskRespVO();
        resp.setName("系统繁忙，请稍后重试");
        return resp;
    }

    /**
     * 获取任务限流处理
     */
    public static TaskRespVO getTaskBlockHandler(
            Long eventId,
            Long taskId,
            BlockException ex) {
        log.warn("获取任务被限流, taskId: {}, 原因: {}", taskId, ex.getClass().getSimpleName());
        TaskRespVO resp = new TaskRespVO();
        resp.setId(taskId);
        resp.setName("系统繁忙");
        return resp;
    }

    /**
     * 更新任务限流处理
     */
    public static TaskRespVO updateTaskBlockHandler(
            Long eventId,
            Long taskId,
            TaskUpdateReqVO request,
            BlockException ex) {
        log.warn("更新任务被限流, taskId: {}", taskId);
        TaskRespVO resp = new TaskRespVO();
        resp.setId(taskId);
        resp.setName("系统繁忙，请稍后重试");
        return resp;
    }

    /**
     * 删除任务限流处理
     */
    public static Boolean deleteTaskBlockHandler(
            Long eventId,
            Long taskId,
            BlockException ex) {
        log.warn("删除任务被限流, taskId: {}", taskId);
        return Boolean.FALSE;
    }

    /**
     * 列表查询限流处理
     */
    public static List<TaskRespVO> listTasksBlockHandler(
            Long eventId,
            BlockException ex) {
        log.warn("任务列表查询被限流, eventId: {}", eventId);
        return Collections.emptyList();
    }

    // ==================== 降级处理 Fallback ====================

    /**
     * 创建任务降级处理
     */
    public static TaskRespVO createTaskFallback(
            Long eventId,
            TaskCreateReqVO request,
            Throwable ex) {
        log.error("创建任务异常降级, eventId: {}, 异常: {}", eventId, ex.getMessage(), ex);
        TaskRespVO resp = new TaskRespVO();
        resp.setName("创建失败，请稍后重试");
        return resp;
    }

    /**
     * 获取任务降级处理
     */
    public static TaskRespVO getTaskFallback(
            Long eventId,
            Long taskId,
            Throwable ex) {
        log.error("获取任务异常降级, taskId: {}, 异常: {}", taskId, ex.getMessage());
        TaskRespVO resp = new TaskRespVO();
        resp.setId(taskId);
        resp.setName("数据加载失败");
        return resp;
    }

    /**
     * 更新任务降级处理
     */
    public static TaskRespVO updateTaskFallback(
            Long eventId,
            Long taskId,
            TaskUpdateReqVO request,
            Throwable ex) {
        log.error("更新任务异常降级, taskId: {}", taskId);
        TaskRespVO resp = new TaskRespVO();
        resp.setId(taskId);
        resp.setName("更新失败");
        return resp;
    }

    /**
     * 删除任务降级处理
     */
    public static Boolean deleteTaskFallback(
            Long eventId,
            Long taskId,
            Throwable ex) {
        log.error("删除任务异常降级, taskId: {}, 异常: {}", taskId, ex.getMessage());
        return Boolean.FALSE;
    }

    /**
     * 列表查询降级处理
     */
    public static List<TaskRespVO> listTasksFallback(
            Long eventId,
            Throwable ex) {
        log.error("任务列表查询异常降级, eventId: {}", eventId);
        return Collections.emptyList();
    }
}