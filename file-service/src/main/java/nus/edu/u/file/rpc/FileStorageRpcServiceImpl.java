package nus.edu.u.file.rpc;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nus.edu.u.file.convert.FileRpcConvert;
import nus.edu.u.file.service.FileStorageService;
import nus.edu.u.shared.rpc.file.FileResultVO;
import nus.edu.u.shared.rpc.file.FileStorageRpcService;
import nus.edu.u.shared.rpc.file.FileUploadReqVO;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class FileStorageRpcServiceImpl implements FileStorageRpcService {

    private final FileStorageService fileStorageService;
    private final FileRpcConvert fileRpcConvert;

    @Override
    public List<FileResultVO> downloadFilesByTaskLogId(Long taskLogId) {
        return defaultList(fileRpcConvert.toRpcList(fileStorageService.downloadFilesByTaskLogId(taskLogId)));
    }

    @Override
    public void uploadToTaskLog(FileUploadReqVO req) {
        if (req == null) {
            throw new IllegalArgumentException("req must not be null");
        }
        fileStorageService.uploadToTaskLog(fileRpcConvert.toDomain(req));
    }

    private List<FileResultVO> defaultList(List<FileResultVO> source) {
        return source == null ? Collections.emptyList() : source;
    }
}
