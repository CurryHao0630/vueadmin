<template>
    <div id="app">
        <router-view/>
    </div>
</template>

<!--用于解决刷新网页时，标签Tab丢失的问题-->
<script>
    export default {
        name: "App",
        watch: {
            $route(to, from) {
                if (to.path != "/login") {
                    let ojb = {
                        name: to.name,
                        title: to.meta.title
                    }

                    this.$store.commit("addTab", ojb)
                }
            }
        }
    }
</script>
<!--用于解决刷新网页时，标签Tab丢失的问题-->


<style>
    #app {
        font-family: Avenir, Helvetica, Arial, sans-serif;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        /*text-align: center;*/
        color: #2c3e50;
    }

    #nav {
        padding: 30px;
    }

    #nav a {
        font-weight: bold;
        color: #2c3e50;
    }

    #nav a.router-link-exact-active {
        color: #42b983;
    }

    /*html, body, #app {*/
    /*    height: 100%;*/
    /*}*/

    html, body, #app {
        font-family: 'Helvetica Neue', 'Hiragino Sans GB', 'WenQuanYi Micro Hei', 'Microsoft Yahei', sans-serif;
        height: 100%;
        padding: 0;
        margin: 0;
        font-size: 15px;
    }
</style>
