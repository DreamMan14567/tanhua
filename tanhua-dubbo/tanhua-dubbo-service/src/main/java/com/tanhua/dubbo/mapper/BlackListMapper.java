package com.tanhua.dubbo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.UserInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author user_Chubby
 * @date 2021/5/6
 * @Description
 */
public interface BlackListMapper extends BaseMapper<BlackList> {

    @Select(value = "select tui.id,tui.avatar,tui.nickname,tui.gender,tui.age from  tb_user_info tui,tb_black_list tbl where tui.id = tbl.black_user_id and tbl.user_id=#{userId}")
    IPage<UserInfo> findBlackList(Page<UserInfo> page, @Param("userId") Long userId);
}
