<template>
    <el-container>

        <SideMenu></SideMenu>

        <el-container>

            <el-header>
                <strong>VueAdmin后台管理系统</strong>
                <div class="header-avatar">
                    <el-avatar :size="35" :src="userInfo.avatar"></el-avatar>

                    <el-dropdown>
                        <span class="el-dropdown-link">
                            {{userInfo.username}}<i class="el-icon-arrow-down el-icon--right"></i>
                        </span>
                        <el-dropdown-menu slot="dropdown">
                            <router-link to="/userCenter">
                                <el-dropdown-item @click="selectUserCenterMenu({name: 'UserCenter', title: '个人中心'})">
                                    个人中心
                                </el-dropdown-item>
                            </router-link>
                            <!--                            <el-dropdown-item @click="selectUserCenterMenu({name: 'UserCenter', title: '个人中心'})">-->
                            <!--                                <router-link to="/userCenter">个人中心</router-link>-->
                            <!--                            </el-dropdown-item>-->
                            <el-dropdown-item @click.native="logout">退出</el-dropdown-item>
                        </el-dropdown-menu>
                    </el-dropdown>

                    <el-link href="https://github.com/CurryHao0630" target="_blank">github</el-link>
                </div>
            </el-header>

            <el-main>
                <Tabs></Tabs>
                <div style="margin: 0 15px;">
                    <router-view/>
                </div>
            </el-main>

        </el-container>
    </el-container>
</template>

<script>
    import SideMenu from "./inc/SideMenu";
    import Tabs from "./inc/Tabs";

    export default {
        name: "Home",
        components: {
            SideMenu,
            Tabs
        },
        data() {
            return {
                userInfo: {
                    id: '',
                    username: '',
                    avatar: ''
                }
            }
        },
        methods: {
            getUserInfo() {
                this.$axios.get("/sys/userInfo").then(res => {
                    this.userInfo = res.data.data
                })

                // this.userInfo = this.$store.getters.getUser

            },
            logout() {
                this.$axios.post("/logout").then(res => {
                    localStorage.clear()
                    sessionStorage.clear()

                    this.$store.commit("RESET_STATE")
                    this.$store.commit("resetMenuState")

                    this.$router.push("/login")
                })
            },
            selectUserCenterMenu(item) {
                console.log("userCenter")
                this.$store.commit("addTab", item)
                console.log("item")
                console.log(item)
            }
        },
        created() {
            this.getUserInfo()
        }
    }
</script>

<style scoped>
    .el-header {
        background-color: #17B3A3;
        color: #333;
        text-align: center;
        line-height: 60px;
    }

    .el-main {
        color: #333;
        padding: 0;
    }

    .el-container {
        padding: 0;
        margin: 0;
        height: 100%;
    }

    .header-avatar {
        float: right;
        width: 210px;
        display: flex; /*配合下面两个使用*/
        justify-content: space-around; /*间距*/
        align-items: center;
    }

    .el-dropdown-link {
        cursor: pointer;
        /*color: #409EFF;*/
    }

    .el-icon-arrow-down {
        font-size: 12px;
    }

    /*去除链接下划线*/
    a {
        text-decoration: none;
    }

</style>