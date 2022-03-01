package com.markerhub.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markerhub.common.dto.SysMenuDto;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.SysMenu;
import com.markerhub.entity.SysRoleMenu;
import com.markerhub.entity.SysUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 我的github：CurryHao0630
 * @since 2022-02-09
 */
@RestController
@RequestMapping("/sys/menu")
public class SysMenuController extends BaseController {

    /**
     * 获取当前用户的菜单和权限信息
     * @param principal
     * @return
     */

    @GetMapping("/nav")
    public Result nav(Principal principal) {
        SysUser sysUser = sysUserService.getByUsername(principal.getName());

        //获取用户权限信息
        String userAuthorityInfo = sysUserService.getUserAuthorityInfo(sysUser.getId());
        String[] authorityInfoArray = StringUtils.tokenizeToStringArray(userAuthorityInfo, ",");

        //获取导航栏信息
        List<SysMenuDto> navs = sysMenuService.getCurrentUserNav();


        return Result.succ(MapUtil.builder()
                .put("authority", authorityInfoArray)
                .put("nav", navs)
                .map()
        );
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result list() {
        List<SysMenu> menus = sysMenuService.tree();

        return Result.succ(menus);
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('sys:menu:save')")
    public Result save(@RequestBody @Validated SysMenu sysMenu) {

        sysMenu.setCreated(LocalDateTime.now());
        sysMenuService.save(sysMenu);

        return Result.succ(sysMenu);
    }

    // 点击编辑，表单回显数据
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result info(@PathVariable(name = "id") Long id) {
        return Result.succ(sysMenuService.getById(id));
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('sys:menu:update')")
    public Result update(@RequestBody @Validated SysMenu sysMenu) {

        sysMenu.setUpdated(LocalDateTime.now());

        sysMenuService.updateById(sysMenu);

        sysUserService.clearUserAuthorityInfoByMenuId(sysMenu.getId());

        return Result.succ(sysMenu);
    }


    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    public Result delete(@PathVariable(name = "id") Long id) {

        int count = sysMenuService.count(new QueryWrapper<SysMenu>().eq("parent_id", id));

        if(count > 0) {
            return Result.fail("请先删除子菜单");
        }

        sysMenuService.removeById(id);

        //同步删除中间关联表对应数据
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("role_id", id));

        //清除所有与该菜单相关的缓存
        sysUserService.clearUserAuthorityInfoByMenuId(id);

        return Result.succ(null);
    }
}
