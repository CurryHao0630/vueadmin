import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default {
    state: {
        menuList: [],
        permList: [],
        hasRoutes: false,
        editableTabsValue: 'Index',
        editableTabs: [{
            title: '首页',
            name: 'Index',
            // content: 'Tab 1 content'
        }]
    },
    mutations: {
        SET_MENULIST: (state, menuList) => {
            state.menuList = menuList
        },
        SET_PERMLIST: (state, perms) => {
            state.permList = perms
        },
        changeRouteStatus(state, hasRoutes) {
            state.hasRoutes = hasRoutes
        },
        addTab(state, tab) {
            let index = state.editableTabs.findIndex(e => e.name === tab.name)

            if (index === -1) {
                state.editableTabs.push({
                    title: tab.title,
                    name: tab.name
                });
            }

            state.editableTabsValue = tab.name;

        },
        //resetMenuState方法 用于解决退出重新登陆后标签Tab都还在的问题
        resetMenuState: (state) => {
            state.menuList = []
            state.permList = []
            state.hasRoutes = false
            state.editableTabsValue = 'Index'
            state.editableTabs =  [{
                title: '首页',
                name: 'Index',
            }]
        }
    },
    getters: {},
    actions: {}

}
