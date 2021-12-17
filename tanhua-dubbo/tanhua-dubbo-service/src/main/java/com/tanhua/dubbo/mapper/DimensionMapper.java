package com.tanhua.dubbo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.domain.db.Dimension;
import com.tanhua.domain.db.Result;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author user_Chubby
 * @date 2021/5/24
 * @Description
 */
public interface DimensionMapper extends BaseMapper<Dimension> {

    @Select("SELECT  id,type_id,`key`,`value`,report_id,created,updated  FROM tb_dimension WHERE report_id = #{reportId}")
    List<Dimension> selectList1(Long reportId);

    @Insert("inset into tb_dimension(type_id,`key`,`value`,report_id) values(#{typeId},#{key},#{value},#{reportId}")
    void insertInMyWay(Dimension dimension);
}
