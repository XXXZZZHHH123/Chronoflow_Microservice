package nus.edu.u.file.convert;

import java.util.List;
import nus.edu.u.shared.rpc.file.FileResultVO;
import nus.edu.u.shared.rpc.file.FileUploadReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileRpcConvert {

    FileResultVO toRpc(nus.edu.u.file.domain.vo.FileResultVO bean);

    List<FileResultVO> toRpcList(List<nus.edu.u.file.domain.vo.FileResultVO> list);

    nus.edu.u.file.domain.vo.FileUploadReqVO toDomain(FileUploadReqVO req);
}
