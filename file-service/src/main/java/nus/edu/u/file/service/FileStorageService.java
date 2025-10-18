package nus.edu.u.file.service;

import java.util.List;
import nus.edu.u.file.domain.vo.FileResultVO;
import nus.edu.u.file.domain.vo.FileUploadReqVO;

public interface FileStorageService {
    List<FileResultVO> uploadToTaskLog(FileUploadReqVO req);

    FileResultVO downloadFile(Long fileId);

    List<FileResultVO> downloadFilesByTaskLogId(Long taskLogId);
}
