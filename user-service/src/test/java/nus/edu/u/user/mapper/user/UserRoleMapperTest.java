package nus.edu.u.user.mapper.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import java.util.List;
import java.util.Set;
import nus.edu.u.user.domain.dataobject.user.UserRoleDO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserRoleMapperTest {

    private UserRoleMapper mapper;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, UserRoleDO.class);
    }

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(UserRoleMapper.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    void selectAliveRoleIdsByUser_returnsRoleIds() {
        when(mapper.selectList(any()))
                .thenReturn(
                        List.of(
                                UserRoleDO.builder().roleId(1L).build(),
                                UserRoleDO.builder().roleId(2L).build()));

        List<Long> result = mapper.selectAliveRoleIdsByUser(10L);

        assertThat(result).containsExactly(1L, 2L);
        verify(mapper).selectList(any());
    }

    @Test
    void batchLogicalDelete_whenIdsEmpty_returnsZero() {
        int result = mapper.batchLogicalDelete(11L, Set.of());

        assertThat(result).isZero();
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void batchLogicalDelete_updatesMatchingRows() {
        when(mapper.update(any(), any())).thenReturn(3);

        int result = mapper.batchLogicalDelete(11L, Set.of(1L, 2L));

        assertThat(result).isEqualTo(3);
        verify(mapper).update(any(), any());
    }

    @Test
    void batchRevive_whenIdsEmpty_returnsZero() {
        int result = mapper.batchRevive(12L, Set.of());

        assertThat(result).isZero();
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void batchRevive_updatesMatchingRows() {
        when(mapper.update(any(), any())).thenReturn(2);

        int result = mapper.batchRevive(12L, Set.of(3L));

        assertThat(result).isEqualTo(2);
        verify(mapper).update(any(), any());
    }

    @Test
    void insertMissing_whenIdsNull_returnsZero() {
        int result = mapper.insertMissing(13L, null);

        assertThat(result).isZero();
        verify(mapper, never()).selectList(any());
    }

    @Test
    void insertMissing_whenAllExist_returnsZeroAndSkipsInsert() {
        when(mapper.selectList(any())).thenReturn(List.of(UserRoleDO.builder().roleId(1L).build()));

        int result = mapper.insertMissing(13L, Set.of(1L));

        assertThat(result).isZero();
        verify(mapper, never()).insert(any());
    }

    @Test
    void insertMissing_insertsOnlyMissingRoleIds() {
        when(mapper.selectList(any()))
                .thenReturn(
                        List.of(
                                UserRoleDO.builder().roleId(1L).build(),
                                UserRoleDO.builder().roleId(3L).build()));
        when(mapper.insert(any())).thenReturn(1);

        int result = mapper.insertMissing(14L, Set.of(1L, 2L, 3L));

        assertThat(result).isEqualTo(1);
        verify(mapper, times(1)).insert(any(UserRoleDO.class));
    }

    @Test
    void selectRoleIdsByUserId_returnsRoleIds() {
        when(mapper.selectObjs(any())).thenReturn(List.of(5L, 6L));

        List<Long> result = mapper.selectRoleIdsByUserId(15L);

        assertThat(result).containsExactly(5L, 6L);
        verify(mapper).selectObjs(any());
    }
}
