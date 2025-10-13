package nus.edu.u.event.rpc;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;
import nus.edu.u.event.convert.EventConvert;
import nus.edu.u.event.service.EventApplicationService;
import nus.edu.u.shared.rpc.events.EventRespDTO;
import nus.edu.u.shared.rpc.events.EventRpcService;

@DubboService
@RequiredArgsConstructor
public class EventRpcServiceImpl implements EventRpcService{
    private final EventApplicationService eventService;
    private final EventConvert eventConvert;

    public EventRespDTO getEvent(Long eventId){
        return eventConvert.toRpc(eventService.getEvent(eventId));
    }
}
