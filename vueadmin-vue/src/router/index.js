import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Index from "../views/Index.vue";
import User from "../views/sys/User";
import Role from "../views/sys/Role";
import Menu from "../views/sys/Menu";
import UserCenter from "../views/UserCenter";

import axios from "../axios";
import store from "../store"

Vue.use(VueRouter)

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home,
        children: [
            {
                path: '/index',
                name: 'Index',
                component: Index
            },
            {
                path: '/userCenter',
                name: 'UserCenter',
                component: UserCenter,
                meta: {
                    title: "个人中心"
                }
            }
            // {
            //   path: '/sys/users',
            //   name: 'SysUser',
            //   component: User
            // },
            // {
            //   path: '/sys/roles',
            //   name: 'SysRole',
            //   component: Role
            // },
            // {
            //   path: '/sys/menus',
            //   name: 'SysMenu',
            //   component: Menu
            // }
        ]
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    }
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

router.beforeEach((to, from, next) => {

    let hasRoute = store.state.menus.hasRoutes

    let token = localStorage.getItem("token")

    if (to.path == '/login') {
        next()
    } else if (!token) {
        next({path: '/login'})
    } else if (token && !hasRoute) {
        axios.get("/sys/menu/nav", {
            headers: {
                "Authorization": localStorage.getItem("token")
            }
        }).then(res => {

            // console.log(res.data.data)

            //拿到menuList，存入store中
            store.commit("SET_MENULIST", res.data.data.nav)


            //拿到用户权限，存入store中
            store.commit("SET_PERMLIST", res.data.data.authoritys)

            //动态绑定路由
            let newRoutes = router.options.routes

            res.data.data.nav.forEach(menu => {
                if (menu.children) {
                    menu.children.forEach(e => {

                        //菜单转成路由
                        let route = menuToRoute(e)

                        //把路由添加到路由管理中
                        if (route) {
                            newRoutes[0].children.push(route)
                        }
                    })
                }
            })
            // console.log("newRoutes")
            // console.log(newRoutes)
            router.addRoutes(newRoutes)

            hasRoute = true
            store.commit("changeRouteStatus", hasRoute)
        })
    }

    next()
})

const menuToRoute = (menu) => {
    if (!menu.component) {
        return null
    }
    let route = {
        path: menu.path,
        name: menu.name,
        component: () => import('../views/' + menu.component + '.vue'),
        meta: {
            icon: menu.icon,
            title: menu.title
        }
    }
    return route
}

//用于处理删除非高亮tab时或重复点击导航栏标签时报的异常
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
    return originalPush.call(this, location).catch(err => err)
}

export default router
