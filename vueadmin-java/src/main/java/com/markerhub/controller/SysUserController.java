package com.markerhub.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markerhub.common.dto.PassDto;
import com.markerhub.common.lang.Const;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.SysRole;
import com.markerhub.entity.SysUser;
import com.markerhub.entity.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.security.Principal;
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
@RequestMapping("/sys/user")
public class SysUserController extends BaseController {

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long id) {

        SysUser sysUser = sysUserService.getById(id);
        Assert.notNull(sysUser, "找不到该管理员");

        List<SysRole> roles = sysRoleService.listRolesByUserId(id);

        sysUser.setSysRoles(roles);

        return Result.succ(sysUser);

    }

    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/list")
    public Result list(String username) {

        Page<SysUser> pageData = sysUserService.page(getPage(), new QueryWrapper<SysUser>()
                .like(StrUtil.isNotBlank(username), "username", username));//搜索框

        pageData.getRecords().forEach(u -> {

          u.setSysRoles(sysRoleService.listRolesByUserId(u.getId()));
        });

        return Result.succ(pageData);
    }

    @PreAuthorize("hasAuthority('sys:user:save')")
    @PostMapping("/save")
    public Result save(@Validated @RequestBody SysUser sysUser) {

        sysUser.setCreated(LocalDateTime.now());
        sysUser.setStatus(Const.STATUS_ON);

        //设置默认密码
        String password = passwordEncoder.encode(Const.DEFULT_PASSWORD);
        sysUser.setPassword(password);

        //设置默认头像
        sysUser.setAvatar(Const.DEFULT_AVATAR);

        sysUserService.save(sysUser);

        return Result.succ(sysUser);

    }

    @PreAuthorize("hasAuthority('sys:user:update')")
    @PostMapping("/update")
    public Result update(@Validated @RequestBody SysUser sysUser) {

        sysUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(sysUser);

        return Result.succ(sysUser);
    }

    @PreAuthorize("hasAuthority('sys:user:delete')")
    @PostMapping("/delete")
    @Transactional //开启事务
    public Result delete(@RequestBody Long[] ids) {

        sysUserService.removeByIds(Arrays.asList(ids));

        //删除中间表
        sysUserRoleService.removeByIds(Arrays.asList(ids));
        sysRoleMenuService.removeByIds(Arrays.asList(ids));

        return Result.succ(ids);
    }

    @PreAuthorize("hasAuthority('sys:user:role')")
    @PostMapping("/role/{userId}")
    public Result rolePerm(@PathVariable("userId") Long userId, @RequestBody Long[] roleIds) {

        List<SysUserRole> sysUserRoles = new ArrayList<>();

        Arrays.stream(roleIds).forEach(r -> {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setRoleId(r);
            sysUserRole.setUserId(userId);

            sysUserRoles.add(sysUserRole);
        });
        //先删除该用户原有角色数据
        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        //批量保存
        sysUserRoleService.saveBatch(sysUserRoles);

        //删除缓存
        SysUser sysUser = sysUserService.getById(userId);
        sysUserService.clearUserAuthorityInfo(sysUser.getUsername());

        return Result.succ(roleIds);

    }

    @PreAuthorize("hasAuthority('sys:user:repass')")
    @PostMapping("/repass")
    public Result repass(@RequestBody Long id) {

        SysUser sysUser = sysUserService.getById(id);
        sysUser.setPassword(passwordEncoder.encode(Const.DEFULT_PASSWORD));
        sysUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(sysUser);

        return Result.succ(id);
    }

    @PostMapping("/updatePass")
    public Result updatePass(@RequestBody @Validated PassDto passDto, Principal principal) {
        SysUser currentUser = sysUserService.getByUsername(principal.getName());
        //   passwordEncoder.matches(正常密码，加密后的字符串)，顺序不能错
        boolean matches = passwordEncoder.matches(passDto.getCurrentPass(), currentUser.getPassword());

        if(!matches) {
            return Result.fail("旧密码不正确！");
        }
        currentUser.setPassword(passwordEncoder.encode(passDto.getPassword()));
        currentUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(currentUser);

        return Result.succ(currentUser);
    }
}
