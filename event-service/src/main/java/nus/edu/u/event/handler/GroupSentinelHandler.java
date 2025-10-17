package nus.edu.u.event.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.event.domain.dto.group.AddMembersReqVO;
import nus.edu.u.event.domain.dto.group.CreateGroupReqVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Group Service Sentinel 限流降级处理器
 */
@Slf4j
@Component
public class GroupSentinelHandler {

    // ==================== 限流处理 ====================

    /**
     * 创建小组限流处理
     */
    public static Long createGroupBlockHandler(
            CreateGroupReqVO reqVO,
            BlockException ex) {
        log.warn("创建小组被限流, 小组名: {}", reqVO.getName());
        return -1L;
    }

    /**
     * 批量添加成员限流处理
     */
    public static Boolean addMembersBlockHandler(
            Long groupId,
            AddMembersReqVO reqVO,
            BlockException ex) {
        log.warn("批量添加成员被限流, groupId: {}, 成员数: {}",
                groupId, reqVO.getUserIds().size());
        return Boolean.FALSE;
    }

    /**
     * 批量删除成员限流处理
     */
    public static Boolean deleteMembersBlockHandler(
            Long groupId,
            List<Long> userIds,
            BlockException ex) {
        log.warn("批量删除成员被限流, groupId: {}, 成员数: {}",
                groupId, userIds.size());
        return Boolean.FALSE;
    }

    // ==================== 降级处理 ====================

    /**
     * 创建小组降级处理
     */
    public static Long createGroupFallback(
            CreateGroupReqVO reqVO,
            Throwable ex) {
        log.error("创建小组异常降级, 小组名: {}, 异常: {}",
                reqVO.getName(), ex.getMessage());
        return -1L;
    }

    /**
     * 批量添加成员降级处理
     */
    public static Boolean addMembersFallback(
            Long groupId,
            AddMembersReqVO reqVO,
            Throwable ex) {
        log.error("批量添加成员异常降级, groupId: {}", groupId);
        return Boolean.FALSE;
    }

    /**
     * 批量删除成员降级处理
     */
    public static Boolean deleteMembersFallback(
            Long groupId,
            List<Long> userIds,
            Throwable ex) {
        log.error("批量删除成员异常降级, groupId: {}", groupId);
        return Boolean.FALSE;
    }
}