import Vue from "vue"

// 实现不同权限用户登录系统后展示不同的菜单按钮
Vue.mixin({
	methods: {
		hasAuth(perm) {
			var authority = this.$store.state.menus.permList

			return authority.indexOf(perm) > -1
		}
	}
})