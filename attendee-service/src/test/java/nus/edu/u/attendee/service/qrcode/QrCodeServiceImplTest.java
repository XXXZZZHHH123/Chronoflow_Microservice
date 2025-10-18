package nus.edu.u.attendee.service.qrcode;

import static org.assertj.core.api.Assertions.assertThat;

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
}
