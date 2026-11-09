package com.campus.aed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.aed.entity.AedDevice;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AED设备 Mapper
 */
public interface AedDeviceMapper extends BaseMapper<AedDevice> {

    /**
     * 统计指定设备编号的占用数(含已逻辑删除的记录):
     * 已删除的行仍占用数据库唯一索引 uk_device_code, 而普通 count 查询会被 @TableLogic
     * 自动追加 deleted=0 过滤掉, 无法发现冲突, 因此这里用原生 SQL 连已删除记录一起统计
     *
     * @param deviceCode 设备编号
     * @param excludeId  需要排除的自身 id(修改场景), 可为 null
     */
    @Select("<script>SELECT COUNT(*) FROM aed_device WHERE device_code = #{deviceCode}" +
            "<if test='excludeId != null'> AND id != #{excludeId}</if></script>")
    Long countByDeviceCodeIncludingDeleted(@Param("deviceCode") String deviceCode, @Param("excludeId") Long excludeId);
}
