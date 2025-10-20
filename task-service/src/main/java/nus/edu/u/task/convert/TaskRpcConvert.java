package nus.edu.u.task.convert;

import java.util.List;
import nus.edu.u.shared.rpc.task.TaskDTO;
import nus.edu.u.task.domain.vo.task.TaskRespVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface TaskRpcConvert {

    TaskDTO toDto(TaskRespVO source);

    List<TaskDTO> toDtoList(List<TaskRespVO> source);
}
