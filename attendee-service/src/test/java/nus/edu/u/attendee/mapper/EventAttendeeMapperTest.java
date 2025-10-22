package nus.edu.u.attendee.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import nus.edu.u.attendee.domain.dataobject.EventAttendeeDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EventAttendeeMapperTest {

    private EventAttendeeMapper mapper;

    private EventAttendeeDO sample;

    @BeforeEach
    void setUp() {
        mapper = mock(EventAttendeeMapper.class, Mockito.CALLS_REAL_METHODS);

        sample = EventAttendeeDO.builder().id(11L).eventId(2L).attendeeEmail("e@e.com").build();
    }

    @Test
    void selectByToken_returnsDO_whenUnderlyingSelectOneReturns() {
        when(mapper.selectOne(any())).thenReturn(sample);

        EventAttendeeDO got = mapper.selectByToken("tok");

        assertEquals(11L, got.getId());
    }

    @Test
    void selectByEventAndEmail_returnsDO_whenUnderlyingSelectOneReturns() {
        when(mapper.selectOne(any())).thenReturn(sample);

        EventAttendeeDO got = mapper.selectByEventAndEmail(2L, "e@e.com");

        assertEquals(11L, got.getId());
    }

    @Test
    void selectByEventId_returnsList_whenUnderlyingSelectListReturns() {
        when(mapper.selectList(any())).thenReturn(List.of(sample));

        var list = mapper.selectByEventId(2L);

        assertEquals(1, list.size());
        assertEquals(11L, list.get(0).getId());
    }
}
