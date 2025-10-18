package nus.edu.u.shared.rpc.events;

public interface EventRpcService {
    EventRespDTO getEvent(Long eventId);

    boolean exists(Long eventId);
}
