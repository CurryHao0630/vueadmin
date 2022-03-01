import Vue from 'vue'
import Vuex from 'vuex'

import menus from "./modules/menus";

Vue.use(Vuex)

export default new Vuex.Store({
    state: {
        token: '',
        userInfo: {},
    },
    //set
    mutations: {
        SET_TOKEN: (state, token) => {
            state.token = token
            localStorage.setItem("token", token) //将value存储到key字段
        },
        SET_USERINFO: (state, userInfo) => {
            state.userInfo = userInfo
            sessionStorage.setItem("userInfo", JSON.stringify(userInfo)) //把JSON对象转成字符串
        },
        RESET_STATE: (state) => {
            state.token = ''
            state.userInfo = {}
        }
    },
    getters: {
        //get
        getUser: state => {
            return state.userInfo
        }
    },
    actions: {},
    modules: {
        menus
    }
})
