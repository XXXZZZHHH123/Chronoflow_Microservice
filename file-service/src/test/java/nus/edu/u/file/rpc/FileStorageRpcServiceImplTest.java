package nus.edu.u.file.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nus.edu.u.file.domain.vo.FileResultVO;
import nus.edu.u.file.domain.vo.FileUploadReqVO;
import nus.edu.u.file.service.FileStorageService;
import nus.edu.u.shared.rpc.file.FileResultDTO;
import nus.edu.u.shared.rpc.file.FileUploadReqDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileStorageRpcServiceImplTest {

    @Mock private FileStorageService fileStorageService;
    @Mock private nus.edu.u.file.convert.FileRpcConvert fileRpcConvert;

    @InjectMocks private FileStorageRpcServiceImpl rpcService;

    private FileUploadReqDTO requestDto;

    @BeforeEach
    void setUp() {
        requestDto =
                FileUploadReqDTO.builder().taskLogId(10L).eventId(20L).files(List.of()).build();
    }

    @Test
    void downloadFilesByTaskLogId_returnsConvertedList() {
        List<FileResultVO> domainList =
                List.of(
                        FileResultVO.builder()
                                .objectName("obj")
                                .contentType("text/plain")
                                .size(5)
                                .build());
        List<FileResultDTO> rpcList =
                List.of(
                        FileResultDTO.builder()
                                .objectName("obj")
                                .contentType("text/plain")
                                .build());

        when(fileStorageService.downloadFilesByTaskLogId(99L)).thenReturn(domainList);
        when(fileRpcConvert.toRpcList(domainList)).thenReturn(rpcList);

        List<FileResultDTO> result = rpcService.downloadFilesByTaskLogId(99L);

        assertThat(result).isEqualTo(rpcList);
        verify(fileStorageService).downloadFilesByTaskLogId(99L);
        verify(fileRpcConvert).toRpcList(domainList);
    }

    @Test
    void downloadFilesByTaskLogId_defaultsToEmptyListWhenConverterReturnsNull() {
        when(fileStorageService.downloadFilesByTaskLogId(1L))
                .thenReturn(List.of(FileResultVO.builder().objectName("obj").build()));
        when(fileRpcConvert.toRpcList(any())).thenReturn(null);

        List<FileResultDTO> result = rpcService.downloadFilesByTaskLogId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void uploadToTaskLog_invokesServiceWithConvertedRequest() {
        FileUploadReqVO domainReq = new FileUploadReqVO();
        domainReq.setTaskLogId(10L);
        domainReq.setEventId(20L);
        when(fileRpcConvert.toDomain(requestDto)).thenReturn(domainReq);

        rpcService.uploadToTaskLog(requestDto);

        verify(fileRpcConvert).toDomain(requestDto);
        verify(fileStorageService).uploadToTaskLog(domainReq);
    }

    @Test
    void uploadToTaskLog_whenRequestNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> rpcService.uploadToTaskLog(null));
    }
}
