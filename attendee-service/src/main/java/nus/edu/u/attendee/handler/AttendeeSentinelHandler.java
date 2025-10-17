package nus.edu.u.attendee.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.attendee.domain.vo.attendee.AttendeeQrCodeRespVO;
import nus.edu.u.attendee.domain.vo.attendee.AttendeeReqVO;
import nus.edu.u.attendee.domain.vo.checkin.CheckInReqVO;
import nus.edu.u.attendee.domain.vo.checkin.CheckInRespVO;
import nus.edu.u.attendee.domain.vo.checkin.GenerateQrCodesReqVO;
import nus.edu.u.attendee.domain.vo.checkin.GenerateQrCodesRespVO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

/**
 * Attendee Service Sentinel 限流降级处理器
 */
@Slf4j
@Component
public class AttendeeSentinelHandler {

    // ==================== 限流处理 ====================

    /**
     * 生成二维码限流处理
     */
    public static GenerateQrCodesRespVO generateQrCodesBlockHandler(
            GenerateQrCodesReqVO reqVO,
            BlockException ex) {
        log.warn("生成二维码被限流, eventId: {}, 参会者数: {}",
                reqVO.getEventId(), reqVO.getAttendees().size());
        GenerateQrCodesRespVO resp = new GenerateQrCodesRespVO();
        resp.setAttendees(Collections.emptyList());
        return resp;
    }

    /**
     * Excel导入生成二维码限流处理
     */
    public static GenerateQrCodesRespVO generateQrCodeByExcelBlockHandler(
            Long eventId,
            MultipartFile file,
            BlockException ex) {
        log.warn("Excel导入生成二维码被限流, eventId: {}", eventId);
        GenerateQrCodesRespVO resp = new GenerateQrCodesRespVO();
        resp.setAttendees(Collections.emptyList());
        return resp;
    }

    /**
     * 签到限流处理
     */
    public static CheckInRespVO checkInBlockHandler(
            CheckInReqVO reqVO,
            BlockException ex) {
        log.warn("签到被限流");
        CheckInRespVO resp = new CheckInRespVO();
        resp.setSuccess(false);
        resp.setMessage("签到人数过多，请稍后再试");
        return resp;
    }

    /**
     * 更新参会者限流处理
     */
    public static AttendeeQrCodeRespVO updateAttendeeBlockHandler(
            Long attendeeId,
            AttendeeReqVO reqVO,
            BlockException ex) {
        log.warn("更新参会者被限流, attendeeId: {}", attendeeId);
        AttendeeQrCodeRespVO resp = new AttendeeQrCodeRespVO();
        resp.setId(attendeeId);
        return resp;
    }

    /**
     * 删除参会者限流处理
     */
    public static Boolean deleteAttendeeBlockHandler(
            Long attendeeId,
            BlockException ex) {
        log.warn("删除参会者被限流, attendeeId: {}", attendeeId);
        return Boolean.FALSE;
    }

    // ==================== 降级处理 ====================

    /**
     * 生成二维码降级处理
     */
    public static GenerateQrCodesRespVO generateQrCodesFallback(
            GenerateQrCodesReqVO reqVO,
            Throwable ex) {
        log.error("生成二维码异常降级, eventId: {}, 异常: {}",
                reqVO.getEventId(), ex.getMessage(), ex);
        GenerateQrCodesRespVO resp = new GenerateQrCodesRespVO();
        resp.setAttendees(Collections.emptyList());
        return resp;
    }

    /**
     * Excel导入生成二维码降级处理
     */
    public static GenerateQrCodesRespVO generateQrCodeByExcelFallback(
            Long eventId,
            MultipartFile file,
            Throwable ex) {
        log.error("Excel导入生成二维码异常降级, eventId: {}", eventId);
        GenerateQrCodesRespVO resp = new GenerateQrCodesRespVO();
        resp.setAttendees(Collections.emptyList());
        return resp;
    }

    /**
     * 签到降级处理
     */
    public static CheckInRespVO checkInFallback(
            CheckInReqVO reqVO,
            Throwable ex) {
        log.error("签到异常降级, 异常: {}", ex.getMessage());
        CheckInRespVO resp = new CheckInRespVO();
        resp.setSuccess(false);
        resp.setMessage("签到失败，请重试");
        return resp;
    }

    /**
     * 更新参会者降级处理
     */
    public static AttendeeQrCodeRespVO updateAttendeeFallback(
            Long attendeeId,
            AttendeeReqVO reqVO,
            Throwable ex) {
        log.error("更新参会者异常降级, attendeeId: {}", attendeeId);
        AttendeeQrCodeRespVO resp = new AttendeeQrCodeRespVO();
        resp.setId(attendeeId);
        return resp;
    }

    /**
     * 删除参会者降级处理
     */
    public static Boolean deleteAttendeeFallback(
            Long attendeeId,
            Throwable ex) {
        log.error("删除参会者异常降级, attendeeId: {}", attendeeId);
        return Boolean.FALSE;
    }
}