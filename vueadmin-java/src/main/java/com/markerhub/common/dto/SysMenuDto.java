package com.markerhub.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/*
    name: 'SysUser',
    title: '用户管理',
    icon: 'el-icon-s-custom',
    component: 'sys/User',
    path: '/sys/users',
    children: []
 */
@Data
public class SysMenuDto implements Serializable {


    private Long id;
    private String name;
    private String title;
    private String icon;
    private String component;
    private String path;
    private List<SysMenuDto> children = new ArrayList<>();

}
