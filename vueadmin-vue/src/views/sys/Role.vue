<template>
    <div>
        <el-form :inline="true">
            <el-form-item>
                <el-input
                        v-model="searchForm.name"
                        placeholder="名称"
                        clearable
                >
                </el-input>
            </el-form-item>

            <el-form-item>
                <el-button @click="getRoleList">搜索</el-button>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="dialogVisible = true">新增</el-button>
            </el-form-item>

            <el-popconfirm title="这是一段内容确定删除吗？" @confirm="delHandle(null)">
                <!--disabled为true默认无法点击-->
                <el-button type="danger" slot="reference" :disabled="delBtStatus">批量删除</el-button>
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
                    prop="name"
                    label="名称"
                    width="120">
            </el-table-column>
            <el-table-column
                    prop="code"
                    label="唯一编码"
                    show-overflow-tooltip>
            </el-table-column>
            <el-table-column
                    prop="remark"
                    label="描述"
                    show-overflow-tooltip>
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
                    prop="operate"
                    label="操作">
                <template slot-scope="scope">
                    <el-button type="text" @click="permHandle(scope.row.id)">分配权限</el-button>
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
                title="角色信息"
                :visible.sync="dialogVisible"
                width="600px"
                :before-close="handleClose">

            <el-form :model="editForm" :rules="editFormRules" ref="editForm" label-width="100px"
                     class="demo-editForm">

                <el-form-item label="角色名称" prop="name" label-width="100px">
                    <el-input v-model="editForm.name" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item label="唯一编码" prop="code" label-width="100px">
                    <el-input v-model="editForm.code" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item label="描述" prop="remark" label-width="100px">
                    <el-input v-model="editForm.remark" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item label="状态" prop="status" label-width="100px">
                    <el-radio-group v-model="editForm.status">
                        <el-radio :label=0>禁用</el-radio>
                        <el-radio :label=1>正常</el-radio>
                    </el-radio-group>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submitForm('editForm')">立即创建</el-button>
                    <el-button @click="resetForm('editForm')">重置</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog
                title="分配权限"
                :visible.sync="permDialogVisible"
                width="600px">

            <el-form :model="permForm">
                <el-tree
                        :data="permTreeData"
                        show-checkbox
                        default-expand-all
                        node-key="id"
                        ref="permTree"
                        :props="defaultProps">
                    <!--:check-strictly=true 添加该属性，树形结构强制不关联，即勾选一级选项时二级选项不会被勾选-->
                </el-tree>
            </el-form>

            <span slot="footer" class="dialog-footer">
                <el-button @click="permDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="submitPermFormHandle('permForm')">确 定</el-button>
            </span>
        </el-dialog>

    </div>
</template>

<script>
    export default {
        name: "Role",
        data() {
            return {
                searchForm: {},
                delBtStatus: true,

                currentPage: 1,
                size: 10,
                total: 0,

                dialogVisible: false,
                permDialogVisible: false,

                multipleSelection: [],

                editForm: {},
                permForm: {},

                permTreeData: [],

                tableData: [],

                editFormRules: {
                    name: [
                        {required: true, message: '请输入角色名称', trigger: 'blur'}
                    ],
                    code: [
                        {required: true, message: '请输入唯一编码', trigger: 'blur'}
                    ],
                    status: [
                        {required: true, message: '请选择状态', trigger: 'blur'}
                    ]
                },

                defaultProps: {
                    children: 'children',
                    label: 'name'
                }
            }
        },

        methods: {
            toggleSelection(rows) {
                if (rows) {
                    rows.forEach(row => {
                        this.$refs.multipleTable.toggleRowSelection(row);
                    });
                } else {
                    this.$refs.multipleTable.clearSelection();
                }
            },
            handleSelectionChange(val) {
                this.multipleSelection = val;
                this.delBtStatus = val.length == 0
            },
            handleSizeChange(val) {
                this.size = val
                this.getRoleList()
                // console.log(`每页 ${val} 条`);
            },
            handleCurrentChange(val) {
                this.currentPage = val
                this.getRoleList()
                // console.log(`当前页: ${val}`);
            },
            getRoleList() {
                this.$axios.get("/sys/role/list", {
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
            submitForm(formName) {
                this.$refs[formName].validate((valid) => {
                    if (valid) {
                        this.$axios.post('/sys/role/' + (this.editForm.id ? 'update' : 'save'), this.editForm)
                            .then(res => {
                                this.$message({
                                    showClose: true,
                                    message: '恭喜你，操作成功',
                                    type: 'success',
                                    onClose: () => {

                                    }
                                });
                                this.getRoleList()
                                this.resetForm('editForm')
                                this.dialogVisible = false
                            })
                    } else {
                        console.log('error submit!!');
                        return false;
                    }
                });
            },
            editHandle(id) {
                this.$axios.get("/sys/role/info/" + id).then(res => {
                    this.editForm = res.data.data
                    this.dialogVisible = true
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
            delHandle(id) {
                var ids = []
                if (id) {
                    ids.push(id)
                } else {
                    this.multipleSelection.forEach(row => {
                        ids.push(row.id)
                    })
                }

                this.$axios.post("/sys/role/delete", ids).then(res => {
                    this.$message({
                        showClose: true,
                        message: '恭喜你，操作成功',
                        type: 'success',
                        onClose: () => {
                            this.getRoleList()
                        }
                    });
                })
            },
            permHandle(id) {
                this.permDialogVisible = true

                this.$axios.get("/sys/role/info/" + id).then(res => {
                    this.$refs.permTree.setCheckedKeys(res.data.data.menuIds);
                    this.permForm = res.data.data
                })
            },

            submitPermFormHandle(formName) {
                var menuIds = this.$refs.permTree.getCheckedKeys()
                this.$axios.post('/sys/role/perm/'+ this.permForm.id, menuIds).then(res => {
                    this.$message({
                        showClose: true,
                        message: '恭喜你，操作成功',
                        type: 'success',
                        onClose: () => {
                            this.getRoleList()
                            this.resetForm('editForm')
                        }
                    });
                    this.permDialogVisible = false
                })
            }
        },


        created() {
            this.getRoleList()

            this.$axios.get('/sys/menu/list').then(res => {
                this.permTreeData = res.data.data
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