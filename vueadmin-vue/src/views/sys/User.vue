<template>
    <div>
        <el-form :inline="true">
            <el-form-item>
                <el-input
                        v-model="searchForm.name"
                        placeholder="用户名"
                        clearable
                >
                </el-input>
            </el-form-item>

            <el-form-item>
                <el-button @click="getUserList">搜索</el-button>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="dialogVisible = true" >新增</el-button>
<!--                v-if="hasAuth('sys:user:save')"-->
            </el-form-item>

            <el-popconfirm title="这是一段内容确定删除吗？" @confirm="delHandle(null)">
                <!--disabled为true默认无法点击-->
                <el-button type="danger" slot="reference" :disabled="delBtStatus">批量删除</el-button>
<!--                v-if="hasAuth('sys:user:delete')"-->
            </el-popconfirm>
        </el-form>

        <el-table
                ref="multipleTable"
                :data="tableData"
                tooltip-effect="dark"
                style="width: 100%"
                border
                stripe
                @selection-change="handleSelectionChange">

            <el-table-column
                    type="selection"
                    width="55">
            </el-table-column>
            <el-table-column
                    label="头像"
                    width="50">
                <template slot-scope="scope">
                    <el-avatar size="small" :src="scope.row.avatar"></el-avatar>
                </template>
            </el-table-column>
            <el-table-column
                    prop="username"
                    label="用户名"
                    width="120">
            </el-table-column>
            <el-table-column
                    prop="roles"
                    label="角色名称">
                <template slot-scope="scope">
                    <el-tag size="small" type="info" v-for="item in scope.row.sysRoles">{{item.name}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column
                    prop="email"
                    label="邮箱">
            </el-table-column>
            <el-table-column
                    prop="phone"
                    label="手机号">
            </el-table-column>
            <el-table-column
                    prop="status"
                    label="状态">
                <template slot-scope="scope">
                    <el-tag size="small" v-if="scope.row.status === 1" type="success">正常</el-tag>
                    <el-tag size="small" v-else-if="scope.row.status === 0" type="danger">禁用</el-tag>
                </template>
            </el-table-column>
            <el-table-column
                    prop="created"
                    width="200"
                    label="创建时间"
            >
            </el-table-column>
            <el-table-column
                    prop="operate"
                    label="操作">
                <template slot-scope="scope">
                    <el-button type="text" @click="roleHandle(scope.row.id)">分配角色</el-button>
                    <el-divider direction="vertical"></el-divider>

                    <el-button type="text" @click="repassHandle(scope.row.id, scope.row.username)">重置密码</el-button>
                    <el-divider direction="vertical"></el-divider>

                    <el-button type="text" @click="editHandle(scope.row.id)">编辑</el-button>
                    <el-divider direction="vertical"></el-divider>

                    <!--气泡-->
                    <el-popconfirm title="这是一段内容确定删除吗？" @confirm="delHandle(scope.row.id)">
                        <el-button type="text" slot="reference">删除</el-button>
                    </el-popconfirm>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                layout="total, sizes, prev, pager, next, jumper"
                :page-sizes="[10, 20, 50, 100]"
                :current-page="currentPage"
                :page-size="size"
                :total="total">
        </el-pagination>

        <!--新增对话框-->
        <el-dialog
                title="用户信息"
                :visible.sync="dialogVisible"
                width="600px"
                :before-close="handleClose">

            <el-form :model="editForm" :rules="editFormRules" ref="editForm" label-width="100px"
                     class="demo-editForm">

                <el-form-item label="用户名" prop="username" label-width="100px">
                    <el-input v-model="editForm.username" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item label="邮箱" prop="email" label-width="100px">
                    <el-input v-model="editForm.email" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item label="手机号" prop="phone" label-width="100px">
                    <el-input v-model="editForm.phone" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item label="状态" prop="status" label-width="100px">
                    <el-radio-group v-model="editForm.status">
                        <el-radio :label=0>禁用</el-radio>
                        <el-radio :label=1>正常</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>

            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="submitForm('editForm')">确 定</el-button>
            </span>
        </el-dialog>

        <el-dialog
                title="分配角色"
                :visible.sync="roleDialogVisible"
                width="600px">

            <el-form :model="roleForm">
                <el-tree
                        :data="roleTreeData"
                        show-checkbox
                        default-expand-all
                        node-key="id"
                        ref="roleTree"
                        :props="defaultProps">
                    <!--:check-strictly=true 添加该属性，树形结构强制不关联，即勾选一级选项时二级选项不会被勾选-->
                </el-tree>
            </el-form>

            <span slot="footer" class="dialog-footer">
                <el-button @click="roleDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="submitRoleFormHandle('roleForm')">确 定</el-button>
            </span>
        </el-dialog>

    </div>
</template>

<script>
    export default {
        name: "User",
        data() {
            return {
                searchForm: {},
                dialogVisible: false,
                roleDialogVisible: false,
                delBtStatus: true,
                roleForm: {},
                roleTreeData: [],
                tableData: [],
                multipleSelection: [],
                currentPage: 1,
                size: 10,
                total: 0,
                editForm: {},
                editFormRules: {
                    username: [
                        {required: true, message: '请输入用户名', trigger: 'blur'}
                    ],
                    email: [
                        {required: true, message: '请输入邮箱', trigger: 'blur'}
                    ],
                    status: [
                        {required: true, message: '请选择状态', trigger: 'blur'}
                    ]
                },
                defaultProps: {
                    label: 'name'
                }
            }
        },
        methods: {
            getUserList() {
                this.$axios.get("/sys/user/list", {
                    params: {
                        name: this.searchForm.name,
                        currentPage: this.currentPage,
                        size: this.size
                    }
                }).then(res => {
                    this.tableData = res.data.data.records
                    this.total = res.data.data.total
                })
            },
            handleSelectionChange(val) {
                this.multipleSelection = val;
                this.delBtStatus = val.length == 0
            },
            handleSizeChange(val) {
                this.size = val
                this.getUserList()
                // console.log(`每页 ${val} 条`);
            },
            handleCurrentChange(val) {
                this.currentPage = val
                this.getUserList()
                // console.log(`当前页: ${val}`);
            },
            editHandle(id) {
                this.dialogVisible = true
                this.$axios.get('/sys/user/info/'+id).then(res => {
                    this.editForm = res.data.data
                })
            },
            delHandle(id) {
                var ids = []
                if (id) {
                    ids.push(id)
                } else {
                    this.multipleSelection.forEach(row => {
                        ids.push(row.id)
                    })
                }

                this.$axios.post("/sys/user/delete", ids).then(res => {
                    this.$message({
                        showClose: true,
                        message: '恭喜你，操作成功',
                        type: 'success',
                        onClose: () => {

                        }
                    });
                    this.getUserList ()
                })
            },
            resetForm(formName) {
                this.$refs[formName].resetFields();
                this.editForm = {};
            },
            handleClose() {
                this.resetForm("editForm");
                this.dialogVisible = false;
            },
            submitForm(formName) {
                this.$refs[formName].validate((valid) => {
                    if (valid) {
                        this.$axios.post('/sys/user/' + (this.editForm.id ? 'update' : 'save'), this.editForm)
                            .then(res => {
                                this.$message({
                                    showClose: true,
                                    message: '恭喜你，操作成功',
                                    type: 'success',
                                    onClose: () => {

                                    }
                                });
                                this.getUserList ()
                                this.dialogVisible = false
                            })
                    } else {
                        console.log('error submit!!');
                        return false;
                    }
                });
            },
            submitRoleFormHandle(formName) {
                var roles = this.$refs.roleTree.getCheckedKeys()
                this.$axios.post('/sys/user/role/'+this.roleForm.id, roles).then(res => {
                    this.$message({
                        showClose: true,
                        message: '恭喜你，操作成功',
                        type: 'success',
                        onClose: () => {

                        }
                    });
                    this.getUserList ()
                    this.roleDialogVisible = false
                })
            },
            roleHandle(id) {
                this.roleDialogVisible = true

                this.$axios.get("/sys/user/info/" + id).then(res => {
                    let roleIds = []
                    res.data.data.sysRoles.forEach(r => {
                        roleIds.push(r.id)
                    })
                    this.$refs.roleTree.setCheckedKeys(roleIds);
                    this.roleForm = res.data.data
                })
            },
            repassHandle(id, username) {
                this.$confirm('将重置用户【' + username + '】的密码, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.$axios.post("/sys/user/repass", id).then(res => {
                        this.$message({
                            showClose: true,
                            message: '恭喜你，操作成功',
                            type: 'success',
                            onClose: () => {
                            }
                        });
                    })
                }).catch(() => {});//捕获$confirm的取消操作
            }
        },
        created() {
            this.getUserList()

            this.$axios.get('/sys/role/list').then(res => {
                this.roleTreeData = res.data.data.records
            })
        }
    }
</script>

<style scoped>
    .el-pagination {
        float: right;
        margin-top: 22px;
    }
</style>