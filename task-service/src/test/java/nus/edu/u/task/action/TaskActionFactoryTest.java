package nus.edu.u.task.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import nus.edu.u.task.enums.TaskActionEnum;
import org.junit.jupiter.api.Test;

class TaskActionFactoryTest {

    @Test
    void getStrategy_returnsRegisteredStrategy() {
        TaskStrategy createStrategy = mock(TaskStrategy.class);
        when(createStrategy.getType()).thenReturn(TaskActionEnum.CREATE);

        TaskStrategy updateStrategy = mock(TaskStrategy.class);
        when(updateStrategy.getType()).thenReturn(TaskActionEnum.UPDATE);

        TaskActionFactory factory = new TaskActionFactory(List.of(createStrategy, updateStrategy));

        assertThat(factory.getStrategy(TaskActionEnum.CREATE)).isSameAs(createStrategy);
        assertThat(factory.getStrategy(TaskActionEnum.UPDATE)).isSameAs(updateStrategy);
    }

    @Test
    void getStrategy_returnsNullForMissingStrategy() {
        TaskActionFactory factory = new TaskActionFactory(List.of());
        assertThat(factory.getStrategy(TaskActionEnum.DELETE)).isNull();
    }
}
