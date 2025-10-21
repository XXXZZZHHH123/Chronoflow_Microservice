package nus.edu.u.attendee.service.qrcode;

import static nus.edu.u.common.enums.ErrorCodeConstants.QRCODE_GENERATION_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.Base64;
import nus.edu.u.attendee.domain.vo.qrcode.QrCodeReqVO;
import nus.edu.u.attendee.domain.vo.qrcode.QrCodeRespVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class QrCodeServiceImplTest {

    private QrCodeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new QrCodeServiceImpl();
        ReflectionTestUtils.setField(service, "baseUrl", "http://test-host");
    }

    @Test
    void generateQrCode_returnsEncodedImage() {
        QrCodeReqVO req =
                QrCodeReqVO.builder().content("hello-world").size(250).format("PNG").build();

        QrCodeRespVO resp = service.generateQrCode(req);

        assertThat(resp.getContentType()).isEqualTo("image/png");
        assertThat(Base64.getDecoder().decode(resp.getBase64Image())).isNotEmpty();
        assertThat(resp.getSize()).isEqualTo(250);
    }

    @Test
    void generateQrCodeBytes_createsByteArray() throws IOException {
        byte[] bytes = service.generateQrCodeBytes("payload", 200, "PNG");

        assertThat(bytes).isNotEmpty();
    }

    @Test
    void generateEventCheckInQrWithToken_usesConfiguredBaseUrl() {
        QrCodeRespVO resp = service.generateEventCheckInQrWithToken("token-value");

        assertThat(resp.getContentType()).isEqualTo("image/png");
        assertThat(Base64.getDecoder().decode(resp.getBase64Image())).isNotEmpty();
    }

    @Test
    void generateQrCode_whenBytesGenerationFails_throwsServiceException() throws Exception {
        QrCodeServiceImpl spyService = spy(new QrCodeServiceImpl());
        ReflectionTestUtils.setField(spyService, "baseUrl", "http://test-host");

        QrCodeReqVO req = QrCodeReqVO.builder().content("boom").size(100).format("PNG").build();

        doThrow(new IOException("encode failed"))
                .when(spyService)
                .generateQrCodeBytes(anyString(), anyInt(), anyString());

        assertThatThrownBy(() -> spyService.generateQrCode(req))
                .isInstanceOf(nus.edu.u.common.exception.ServiceException.class)
                .extracting("code")
                .isEqualTo(QRCODE_GENERATION_FAILED.getCode());
    }
}
