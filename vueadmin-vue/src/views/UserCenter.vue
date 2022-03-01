<template>
    <div>
        <h2>您好！{{passForm.username}} 同学</h2>
        <el-form :model="passForm" :rules="rules" ref="passForm" label-width="100px" class="demo-passForm">
            <el-form-item label="旧密码" prop="currentPass">
                <el-input v-model="passForm.currentPass"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="password">
                <el-input type="password" v-model="passForm.password"></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="checkPass">
                <el-input type="password" v-model="passForm.checkPass"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="submitForm('passForm')">提交</el-button>
                <el-button @click="resetForm('passForm')">重置</el-button>
            </el-form-item>
        </el-form>
    </div>

</template>

<script>
    export default {
        name: "UserCenter",
        data () {
            //自定义校验规则
            var validatePass = (rule, value, callback) => {
                if(value === ''){
                    callback(new Error('请输入密码'));
                }else if(value === this.passForm.currentPass){
                    callback(new Error('新密码不能与原密码相同！'));
                } else {
                    callback();
                }
            };
            //自定义校验规则
            var validateCheckPass = (rule, value, callback) => {
                if(value === ''){
                    callback(new Error('请再次输入密码'));
                }else if(value !== this.passForm.password){
                    callback(new Error('两次输入密码不一致！'));
                } else {
                    callback();
                }
            };
            return {
                passForm: {
                    username: '',
                    currentPass: '',
                    password: '',
                    checkPass: ''
                },
                rules: {
                    currentPass: [
                        { required: true, message: '请输入旧密码', trigger: 'blur' }
                    ],
                    password: [
                        { required: true, message: '请输入新密码', trigger: 'blur', validator: validatePass},
                        { min: 6, max: 12, message: '长度为6到12个字符', trigger: 'blur'}
                    ],
                    checkPass: [
                        { required: true, validator: validateCheckPass, trigger: 'blur' } //自定义校验规则
                    ]
                },
            }
        },
        methods: {
            getUsername() {
                this.passForm.username = this.$store.getters.getUser.username
            },
            submitForm(formName) {
                this.$refs[formName].validate((valid) => {
                    if(valid) {
                        const _this = this
                        this.$axios.post("/sys/user/updatePass", this.passForm).then(res => {
                            _this.$alert(res.data.msg, '提示', {
                                confirmButtonText: '确定',
                                callback: action => {
                                    this.$refs[formName].resetFields();
                                }
                            });
                        })
                    } else {
                        console.log('error submit!!');
                        return false;
                    }
                });
            },
            resetForm(formName) {
                this.$refs[formName].resetFields();
            }
        },
        created() {
            this.getUsername()
        }
    }
</script>

<style scoped>
    .demo-passForm {
        max-width: 399px;
        margin: 50px auto;
    }
</style>