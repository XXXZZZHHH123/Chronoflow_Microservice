package nus.edu.u.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import nus.edu.u.file.domain.dataobject.FileDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Lu Shuwen
 * @date 2025-08-31
 */
@Mapper
public interface FileMapper extends BaseMapper<FileDO> {
    int insertBatch(@Param("list") List<FileDO> list);
}
