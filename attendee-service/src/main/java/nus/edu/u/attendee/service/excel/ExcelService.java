package nus.edu.u.attendee.service.excel;

import static nus.edu.u.common.enums.ErrorCodeConstants.EMPTY_EXCEL;
import static nus.edu.u.common.exception.enums.GlobalErrorCodeConstants.EXCEL_FORMAT_ERROR;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.attendee.domain.vo.attendee.AttendeeReqVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ExcelService {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public List<AttendeeReqVO> importAttendees(MultipartFile file) {
        try {
            List<AttendeeReqVO> attendeeList = new ArrayList<>();

            EasyExcel.read(
                            file.getInputStream(),
                            AttendeeReqVO.class,
                            new AnalysisEventListener<AttendeeReqVO>() {
                                @Override
                                public void invoke(
                                        AttendeeReqVO attendee, AnalysisContext context) {
                                    attendeeList.add(attendee);
                                }

                                @Override
                                public void doAfterAllAnalysed(AnalysisContext context) {
                                    // no-op
                                }
                            })
                    .sheet()
                    .doRead();

            if (CollectionUtil.isEmpty(attendeeList)) {
                throw exception(EMPTY_EXCEL);
            }

            Set<String> emailSet = new HashSet<>();
            for (AttendeeReqVO attendee : attendeeList) {
                // 1️⃣ 校验必填字段
                Set<ConstraintViolation<AttendeeReqVO>> violations = validator.validate(attendee);
                if (!violations.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Invalid data for attendee: " + attendee.getEmail());
                }

                // 2️⃣ 校验 Excel 内部重复
                if (!emailSet.add(attendee.getEmail())) {
                    throw new IllegalArgumentException(
                            "Duplicate email in Excel: " + attendee.getEmail());
                }
            }

            log.info("Imported {} attendees", attendeeList.size());

            return attendeeList;
        } catch (IOException e) {
            throw exception(EXCEL_FORMAT_ERROR);
        }
    }
}
