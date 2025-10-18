package nus.edu.u.task.action;

import static nus.edu.u.common.enums.ErrorCodeConstants.*;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import nus.edu.u.shared.rpc.file.FileStorageRpcService;
import nus.edu.u.shared.rpc.file.FileUploadReqDTO;
import nus.edu.u.shared.rpc.file.FileUploadReqDTO.FileResource;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dto.TaskActionDTO;
import nus.edu.u.task.enums.TaskActionEnum;
import nus.edu.u.task.enums.TaskStatusEnum;
import nus.edu.u.task.mapper.TaskMapper;
import nus.edu.u.task.service.TaskLogApplicationService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public abstract class AbstractTaskStrategy implements TaskStrategy {

    @Resource protected TaskMapper taskMapper;

    @Resource protected TaskLogApplicationService taskLogService;

    @DubboReference(check = false)
    protected FileStorageRpcService fileStorageRpcService;

    public abstract void execute(TaskDO task, TaskActionDTO taskActionDTO, Object... params);

    public abstract TaskActionEnum getType();

    public void validateTimeRange(
            TaskDO task,
            LocalDateTime taskStart,
            LocalDateTime taskEnd,
            LocalDateTime eventStart,
            LocalDateTime eventEnd) {
        if (taskStart != null && taskEnd != null && !taskStart.isBefore(taskEnd)) {
            throw exception(TASK_TIME_RANGE_INVALID);
        }
        if (taskStart != null && eventStart != null && taskStart.isBefore(eventStart)) {
            throw exception(TASK_TIME_OUTSIDE_EVENT);
        }
        if (taskEnd != null && eventEnd != null && taskEnd.isAfter(eventEnd)) {
            task.setStatus(TaskStatusEnum.DELAYED.getStatus());
            taskMapper.updateById(task);
            throw exception(TASK_TIME_OUTSIDE_EVENT);
        }
    }

    protected void uploadFiles(Long taskLogId, Long eventId, List<MultipartFile> files) {
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        if (ObjectUtil.isNull(taskLogId) || ObjectUtil.isNull(eventId)) {
            throw exception(TASK_LOG_FILE_FAILED);
        }

        FileUploadReqDTO reqVO =
                FileUploadReqDTO.builder()
                        .taskLogId(taskLogId)
                        .eventId(eventId)
                        .files(toRpcFiles(files))
                        .build();
        fileStorageRpcService.uploadToTaskLog(reqVO);
    }

    private List<FileResource> toRpcFiles(List<MultipartFile> files) {
        return files.stream()
                .map(
                        file -> {
                            try {
                                byte[] content = file.getBytes();
                                String name = file.getOriginalFilename();
                                if (name == null || name.isBlank()) {
                                    name = file.getName();
                                }
                                return FileResource.builder()
                                        .name(name)
                                        .contentType(file.getContentType())
                                        .content(content)
                                        .size((long) content.length)
                                        .build();
                            } catch (IOException ex) {
                                throw new UncheckedIOException("Failed to read file content", ex);
                            }
                        })
                .toList();
    }
}
