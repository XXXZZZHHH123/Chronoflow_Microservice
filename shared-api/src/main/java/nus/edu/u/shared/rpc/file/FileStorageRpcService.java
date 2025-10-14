package nus.edu.u.shared.rpc.file;

import java.util.List;

public interface FileStorageRpcService {
    List<FileResultVO> downloadFilesByTaskLogId(Long taskLogId);

    List<FileResultVO> uploadToTaskLog(FileUploadReqDTO req);
}
