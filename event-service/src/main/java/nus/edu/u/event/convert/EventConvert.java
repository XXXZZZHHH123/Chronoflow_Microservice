package nus.edu.u.event.convert;

import java.util.List;
import nus.edu.u.event.domain.dataobject.event.EventDO;
import nus.edu.u.event.domain.dto.event.EventCreateReqVO;
import nus.edu.u.event.domain.dto.event.EventDTO;
import nus.edu.u.event.domain.dto.event.EventRespVO;
import nus.edu.u.event.domain.dto.event.EventUpdateReqVO;
import nus.edu.u.event.domain.dto.event.UpdateEventRespVO;
import nus.edu.u.shared.rpc.events.EventRespDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventConvert {

    EventDTO convert(EventCreateReqVO bean);

    EventDTO convert(EventUpdateReqVO bean);

    @Mapping(target = "userId", source = "organizerId")
    @Mapping(target = "remark", source = "remarks")
    @Mapping(target = "name", source = "eventName")
    EventDO convert(EventDTO bean);

    @Mapping(target = "organizerId", source = "userId")
    @Mapping(target = "eventName", source = "name")
    @Mapping(target = "remarks", source = "remark")
    EventDTO convert(EventDO bean);

    @Mappings({
        @Mapping(target = "name", source = "eventName"),
        @Mapping(target = "remark", source = "remarks")
    })
    EventRespVO convertResp(EventDTO bean);

    @Mappings({
        @Mapping(target = "organizerId", source = "userId"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "description", source = "description"),
        @Mapping(target = "location", source = "location"),
        @Mapping(target = "startTime", source = "startTime"),
        @Mapping(target = "endTime", source = "endTime"),
        @Mapping(target = "status", source = "status"),
        @Mapping(target = "remark", source = "remark"),
        @Mapping(target = "createTime", source = "createTime")
    })
    EventRespVO DOconvertVO(EventDO bean);

    List<EventRespVO> convertList(List<EventDO> list);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
        @Mapping(target = "name", source = "eventName"),
        @Mapping(target = "remark", source = "remark"),
        @Mapping(target = "userId", source = "organizerId")
    })
    void patch(@MappingTarget EventDO target, EventUpdateReqVO source);

    @Mappings({
        @Mapping(target = "eventName", source = "name"),
        @Mapping(target = "remarks", source = "remark"),
        @Mapping(target = "organizerId", source = "userId"),
        @Mapping(target = "location", source = "location"),
        @Mapping(target = "status", source = "status"),
        @Mapping(target = "startTime", source = "startTime"),
        @Mapping(target = "endTime", source = "endTime")
    })
    UpdateEventRespVO toUpdateResp(EventDO bean);

    EventRespDTO toRpc(EventRespVO bean);
}
