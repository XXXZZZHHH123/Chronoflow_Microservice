package nus.edu.u.user.controller.user;

import static nus.edu.u.common.enums.ErrorCodeConstants.REG_FAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.user.domain.vo.reg.RegMemberReqVO;
import nus.edu.u.user.domain.vo.reg.RegOrganizerReqVO;
import nus.edu.u.user.domain.vo.reg.RegSearchReqVO;
import nus.edu.u.user.domain.vo.reg.RegSearchRespVO;
import nus.edu.u.user.service.user.RegService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegControllerTest {

    @Mock private RegService regService;
    @InjectMocks private RegController controller;

    @Test
    void search_delegatesToService() {
        RegSearchReqVO req = new RegSearchReqVO();
        RegSearchRespVO resp = new RegSearchRespVO();
        when(regService.search(req)).thenReturn(resp);

        CommonResult<RegSearchRespVO> result = controller.search(req);

        assertThat(result.getData()).isSameAs(resp);
        verify(regService).search(req);
    }

    @Test
    void registerAsMember_whenServiceSucceeds_returnsSuccess() {
        RegMemberReqVO req = new RegMemberReqVO();
        when(regService.registerAsMember(req)).thenReturn(true);

        CommonResult<Boolean> result = controller.registerAsMember(req);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();
        verify(regService).registerAsMember(req);
    }

    @Test
    void registerAsMember_whenServiceFails_returnsError() {
        RegMemberReqVO req = new RegMemberReqVO();
        when(regService.registerAsMember(req)).thenReturn(false);

        CommonResult<Boolean> result = controller.registerAsMember(req);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(REG_FAIL.getCode());
    }

    @Test
    void registerAsOrganizer_whenServiceSucceeds_returnsSuccess() {
        RegOrganizerReqVO req = new RegOrganizerReqVO();
        when(regService.registerAsOrganizer(req)).thenReturn(true);

        CommonResult<Boolean> result = controller.registerAsOrganizer(req);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();
        verify(regService).registerAsOrganizer(req);
    }

    @Test
    void registerAsOrganizer_whenServiceFails_returnsError() {
        RegOrganizerReqVO req = new RegOrganizerReqVO();
        when(regService.registerAsOrganizer(req)).thenReturn(false);

        CommonResult<Boolean> result = controller.registerAsOrganizer(req);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(REG_FAIL.getCode());
    }
}
