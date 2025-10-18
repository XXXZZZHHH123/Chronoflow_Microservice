package nus.edu.u.shared.rpc.file;

import java.util.List;

public interface FileStorageRpcService {
    List<FileResultDTO> downloadFilesByTaskLogId(Long taskLogId);

    void uploadToTaskLog(FileUploadReqDTO req);
}
