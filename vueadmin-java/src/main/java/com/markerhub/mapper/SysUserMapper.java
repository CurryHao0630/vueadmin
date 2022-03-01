package com.markerhub.mapper;

import com.markerhub.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 我的github：CurryHao0630
 * @since 2022-02-09
 */
@Repository
public interface SysUserMapper extends BaseMapper<SysUser> {


    List<Long> getNavMenuIds(Long userId);

    List<SysUser> listByMenuId(Long menuId);

}
