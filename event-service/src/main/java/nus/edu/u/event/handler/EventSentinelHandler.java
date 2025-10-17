package nus.edu.u.event.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.event.domain.dto.event.EventCreateReqVO;
import nus.edu.u.event.domain.dto.event.EventRespVO;
import nus.edu.u.event.domain.dto.event.UpdateEventRespVO;
import nus.edu.u.event.domain.dto.event.EventUpdateReqVO;
import org.springframework.stereotype.Component;

/**
 * Event Service Sentinel 限流降级处理器
 */
@Slf4j
@Component
public class EventSentinelHandler {

    // ==================== 限流处理 ====================

    /**
     * 创建活动限流处理
     */
    public static EventRespVO createEventBlockHandler(
            EventCreateReqVO request,
            BlockException ex) {
        log.warn("创建活动被限流, 活动名: {}", request.getEventName());
        EventRespVO resp = new EventRespVO();
        resp.setName("系统繁忙，请稍后重试");
        return resp;
    }

    /**
     * 更新活动限流处理
     */
    public static UpdateEventRespVO updateEventBlockHandler(
            Long id,
            EventUpdateReqVO request,
            BlockException ex) {
        log.warn("更新活动被限流, eventId: {}", id);
        UpdateEventRespVO resp = new UpdateEventRespVO();
        // 设置必要的响应字段
        return resp;
    }

    /**
     * 删除活动限流处理
     */
    public static Boolean deleteEventBlockHandler(
            Long id,
            BlockException ex) {
        log.warn("删除活动被限流, eventId: {}", id);
        return Boolean.FALSE;
    }

    /**
     * 恢复活动限流处理
     */
    public static Boolean restoreEventBlockHandler(
            Long id,
            BlockException ex) {
        log.warn("恢复活动被限流, eventId: {}", id);
        return Boolean.FALSE;
    }

    // ==================== 降级处理 ====================

    /**
     * 创建活动降级处理
     */
    public static EventRespVO createEventFallback(
            EventCreateReqVO request,
            Throwable ex) {
        log.error("创建活动异常降级, 活动名: {}, 异常: {}",
                request.getEventName(), ex.getMessage(), ex);
        EventRespVO resp = new EventRespVO();
        resp.setName("创建失败，请稍后重试");
        return resp;
    }

    /**
     * 更新活动降级处理
     */
    public static UpdateEventRespVO updateEventFallback(
            Long id,
            EventUpdateReqVO request,
            Throwable ex) {
        log.error("更新活动异常降级, eventId: {}", id);
        UpdateEventRespVO resp = new UpdateEventRespVO();
        return resp;
    }

    /**
     * 删除活动降级处理
     */
    public static Boolean deleteEventFallback(
            Long id,
            Throwable ex) {
        log.error("删除活动异常降级, eventId: {}", id);
        return Boolean.FALSE;
    }

    /**
     * 恢复活动降级处理
     */
    public static Boolean restoreEventFallback(
            Long id,
            Throwable ex) {
        log.error("恢复活动异常降级, eventId: {}", id);
        return Boolean.FALSE;
    }
}