package nus.edu.u.file.rpc;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nus.edu.u.file.convert.FileRpcConvert;
import nus.edu.u.file.service.FileStorageService;
import nus.edu.u.shared.rpc.file.FileResultDTO;
import nus.edu.u.shared.rpc.file.FileStorageRpcService;
import nus.edu.u.shared.rpc.file.FileUploadReqDTO;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(retries = 0, cluster = "failfast")
@RequiredArgsConstructor
public class FileStorageRpcServiceImpl implements FileStorageRpcService {

    private final FileStorageService fileStorageService;
    private final FileRpcConvert fileRpcConvert;

    @Override
    public List<FileResultDTO> downloadFilesByTaskLogId(Long taskLogId) {
        return defaultList(
                fileRpcConvert.toRpcList(fileStorageService.downloadFilesByTaskLogId(taskLogId)));
    }

    @Override
    public void uploadToTaskLog(FileUploadReqDTO req) {
        if (req == null) {
            throw new IllegalArgumentException("req must not be null");
        }
        fileStorageService.uploadToTaskLog(fileRpcConvert.toDomain(req));
    }

    private List<FileResultDTO> defaultList(List<FileResultDTO> source) {
        return source == null ? Collections.emptyList() : source;
    }
}
