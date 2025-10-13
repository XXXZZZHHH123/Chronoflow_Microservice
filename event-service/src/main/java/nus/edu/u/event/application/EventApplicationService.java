package nus.edu.u.event.application;

import java.util.List;
import nus.edu.u.event.interfaces.rest.dto.EventCreateReqVO;
import nus.edu.u.event.interfaces.rest.dto.EventGroupRespVO;
import nus.edu.u.event.interfaces.rest.dto.EventRespVO;
import nus.edu.u.event.interfaces.rest.dto.EventUpdateReqVO;
import nus.edu.u.event.interfaces.rest.dto.UpdateEventRespVO;

public interface EventApplicationService {

    EventRespVO createEvent(EventCreateReqVO reqVO);

    EventRespVO getEvent(Long eventId);

    List<EventRespVO> getEventsByOrganizer(Long organizerId);

    UpdateEventRespVO updateEvent(Long id, EventUpdateReqVO reqVO);

    boolean deleteEvent(Long id);

    boolean restoreEvent(Long id);

    List<EventGroupRespVO> findAssignableGroups(Long eventId);
}
