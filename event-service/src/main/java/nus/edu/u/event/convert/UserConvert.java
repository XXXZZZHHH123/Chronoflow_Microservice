package nus.edu.u.event.convert;

import java.util.List;
import nus.edu.u.event.domain.dto.user.UserProfileRespVO;
import nus.edu.u.shared.rpc.user.UserProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvert {

    UserProfileRespVO toProfile(UserProfileDTO dto);

    List<UserProfileRespVO> toProfiles(List<UserProfileDTO> dtos);
}
