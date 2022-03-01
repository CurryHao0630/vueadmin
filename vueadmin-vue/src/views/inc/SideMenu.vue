<template>
    <el-aside width="200px">
        <el-menu
                :default-active= this.$store.state.menus.editableTabsValue
                class="el-menu-vertical-demo"
                background-color="#545c64"
                text-color="#fff"
                active-text-color="#ffd04b">

            <router-link to="/index">
                <el-menu-item index="Index" @click="selectMenu({name: 'Index', title: '首页'})">
                    <template slot="title">
                        <i class="el-icon-s-home"></i>
                        <span slot="title">首页</span>
                    </template>
                </el-menu-item>
            </router-link>

            <el-submenu :index="menu.name" v-for="menu in menuList">
                <template slot="title">
                    <i :class="menu.icon"></i>
                    <span>{{menu.title}}</span>
                </template>

                <router-link :to="item.path" v-for="item in menu.children">
                    <el-menu-item :index="item.name" @click="selectMenu(item)">
                        <template slot="title">
                            <i :class="item.icon"></i>
                            <span slot="title">{{item.title}}</span>
                        </template>
                    </el-menu-item>
                </router-link>

            </el-submenu>
        </el-menu>
    </el-aside>
</template>

<script>
    export default {
        name: "SideMenu",
        data() {
            return {}
        },
        computed: {
            menuList: {
                get() {
                    return this.$store.state.menus.menuList
                }
            }
        },
        methods: {
            selectMenu(item) {
                // console.log("首页")
                this.$store.commit("addTab", item)
                // console.log("首页item")
                // console.log(item)
            }
        }
    }
</script>

<style scoped>
    .el-aside {
        background-color: #D3DCE6;
        color: #333;
        text-align: left;
        line-height: 200px;
    }

    .el-menu-vertical-demo {
        height: 100%;
    }

    /*去除链接下划线*/
    a {
        text-decoration: none;
    }

</style>