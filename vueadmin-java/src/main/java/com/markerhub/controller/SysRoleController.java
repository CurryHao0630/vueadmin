package com.markerhub.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markerhub.common.lang.Const;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.SysMenu;
import com.markerhub.entity.SysRole;
import com.markerhub.entity.SysRoleMenu;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 我的github：CurryHao0630
 * @since 2022-02-09
 */
@RestController
@RequestMapping("/sys/role")
public class SysRoleController extends BaseController {

    @PreAuthorize("hasAuthority('sys:role:list')")
    @GetMapping("/info/{id}")
    public Result info(@PathVariable(name = "id") Long id) {

        SysRole sysRole = sysRoleService.getById(id);

        //获取角色相关联的菜单id
        List<SysRoleMenu> roleMenus = sysRoleMenuService.list(new QueryWrapper<SysRoleMenu>().eq("role_id", id));
        //通过流的形式获取menuIds
        List<Long> menuIds = roleMenus.stream().map(p -> p.getMenuId()).collect(Collectors.toList());

        sysRole.setMenuIds(menuIds);

        return Result.succ(sysRole);

    }

    @PreAuthorize("hasAuthority('sys:role:list')")
    @GetMapping("/list")
    public Result list(String name) {

        Page<SysRole> pageData = sysRoleService.page(getPage(),
                new QueryWrapper<SysRole>()
                        .like(StrUtil.isNotBlank(name), "name", name));//搜索框

        return Result.succ(pageData);
    }

    @PreAuthorize("hasAuthority('sys:role:save')")
    @PostMapping("/save")
    public Result save(@Validated @RequestBody SysRole sysRole) {

        sysRole.setCreated(LocalDateTime.now());
        sysRole.setStatus(Const.STATUS_ON);

        sysRoleService.save(sysRole);

        return Result.succ(sysRole);

    }

    @PreAuthorize("hasAuthority('sys:role:update')")
    @PostMapping("/update")
    public Result update(@Validated @RequestBody SysRole sysRole) {

        sysRole.setUpdated(LocalDateTime.now());
        sysRoleService.updateById(sysRole);

        //更新缓存
        sysUserService.clearUserAuthorityInfoByRoleId(sysRole.getId());

        return Result.succ(sysRole);

    }

    @PreAuthorize("hasAuthority('sys:role:delete')")
    @PostMapping("/delete")
    @Transactional //开启事务
    public Result delete(@RequestBody Long[] ids) {

        sysRoleService.removeByIds(Arrays.asList(ids));

        //删除中间表
        sysUserRoleService.removeByIds(Arrays.asList(ids));
        sysRoleMenuService.removeByIds(Arrays.asList(ids));

        //缓存同步删除
        Arrays.stream(ids).forEach(id -> {
            //更新缓存
            sysUserService.clearUserAuthorityInfoByRoleId(id);
        });

        return Result.succ(ids);
    }


    @PreAuthorize("hasAuthority('sys:role:perm')")
    @Transactional //开启事务
    @PostMapping("/perm/{roleId}")
    //分配权限
    public Result perm(@PathVariable(name = "roleId") Long roleId, @RequestBody Long[] menuIds) {

        List<SysRoleMenu> sysRoleMenus = new ArrayList<>();

        Arrays.stream(menuIds).forEach(menuId -> {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(roleId);
            sysRoleMenu.setMenuId(menuId);

            sysRoleMenus.add(sysRoleMenu);
        });

        //先删除关联表中原先的记录，再保存新的
        sysRoleMenuService.removeById(roleId);
        sysRoleMenuService.saveBatch(sysRoleMenus);

        //删除缓存
        sysUserService.clearUserAuthorityInfoByRoleId(roleId);

        return Result.succ(menuIds);
    }

}
