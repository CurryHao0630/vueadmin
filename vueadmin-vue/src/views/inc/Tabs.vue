<template>
    <el-tabs v-model="editableTabsValue" type="card" closable @tab-remove="removeTab" @tab-click="clickTab">
        <el-tab-pane
                v-for="(item, index) in editableTabs"
                :key="item.name"
                :label="item.title"
                :name="item.name"
        >

        </el-tab-pane>
    </el-tabs>
</template>

<script>
    export default {
        name: "Tabs",
        data() {
            return {}
        },
        computed: {
            editableTabsValue: {
                get() {
                    return this.$store.state.menus.editableTabsValue
                },
                set(val) {
                    this.$store.state.menus.editableTabsValue = val
                }
            },
            editableTabs: {
                get() {
                    return this.$store.state.menus.editableTabs
                },
                set(val) {
                    this.$store.state.menus.editableTabs = val
                }
            }
        },
        methods: {
            removeTab(targetName) {
                let tabs = this.editableTabs;
                let activeName = this.editableTabsValue;

                if(targetName === "Index"){
                    return
                }

                if (activeName === targetName) {
                    tabs.forEach((tab, index) => {
                        if (tab.name === targetName) {
                            let nextTab = tabs[index + 1] || tabs[index - 1];
                            if (nextTab) {
                                activeName = nextTab.name;
                            }
                        }
                    });
                }

                this.editableTabsValue = activeName;
                this.editableTabs = tabs.filter(tab => tab.name !== targetName);

                this.$router.push({name: this.editableTabsValue})

                console.log("removeTabs")
                console.log(this.$store.state.menus.editableTabs)
                console.log(this.editableTabs)
            },
            clickTab(target) {
                this.$router.push({name: target.name})
            }
        }
    }
</script>

<style scoped>

</style>