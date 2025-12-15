<template>
  <div v-loading="loading">
    <div style="margin-top: 10px;display: flex;justify-content: center">
      <el-input
        placeholder="默认展示部分用户，可以通过用户名搜索用户..."
        prefix-icon="el-icon-search"
        v-model="keywords" style="width: 400px" size="small">
      </el-input>
      <el-button type="primary" icon="el-icon-search" size="small" style="margin-left: 3px" @click="searchClick">搜索
      </el-button>
    </div>
    <div style="display: flex;justify-content: space-around;flex-wrap: wrap">
      <el-card style="width:330px;margin-top: 10px;" v-for="(user,index) in users" :key="index"
               v-loading="cardloading[index]">
        <div slot="header" style="text-align: left">
          <span>{{user.nickname}}</span>
          <el-button style="float: right; padding: 3px 0;color: #ff0509" type="text" icon="el-icon-delete"
                     @click="deleteUser(user.id)">删除
          </el-button>
        </div>
        <div>
          <div style="position: relative; display: inline-block;">
            <img :src="user.userface" :alt="user.nickname" style="width: 70px;height: 70px; border-radius: 50%; object-fit: cover;">
            <el-button
              v-if="isAdmin"
              size="mini"
              type="text"
              icon="el-icon-camera"
              style="position: absolute; bottom: 0; right: 0; background: white; border-radius: 50%; padding: 2px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);"
              @click="showAvatarUpload(user)">
            </el-button>
          </div>
          <div style="text-align: left;color:#20a0ff;font-size: 12px;margin-top: 13px">
            <span>用户名:</span><span>{{user.username}}</span>
          </div>
          <div style="text-align: left;color:#20a0ff;font-size: 12px;margin-top: 13px">
            <span>电子邮箱:</span><span>{{user.email}}</span>
          </div>
          <div style="text-align: left;color:#20a0ff;font-size: 12px;margin-top: 13px">
            <span>注册时间:</span><span>{{user.regTime | formatDateTime}}</span>
          </div>
          <div
            style="text-align: left;color:#20a0ff;font-size: 12px;margin-top: 13px;display: flex;align-items: center">
            <span>用户状态:</span>
            <el-switch
              v-model="user.enabled"
              active-text="启用"
              active-color="#13ce66"
              @change="enabledChange(user.enabled,user.id,index)"
              inactive-text="禁用" style="font-size: 12px">
            </el-switch>
          </div>
          <div style="text-align: left;color:#20a0ff;font-size: 12px;margin-top: 13px">
            <span>用户角色:</span>
            <el-tag
              v-for="role in user.roles"
              :key="role.id"
              size="mini"
              style="margin-right: 8px"
              type="success">
              {{role.name}}
            </el-tag>
            <el-popover
              placement="right"
              title="角色列表"
              width="200"
              :key="index+''+user.id"
              @hide="saveRoles(user.id,index)"
              trigger="click" v-loading="eploading[index]">
              <el-select v-model="roles" :key="user.id" multiple placeholder="请选择" size="mini">
                <el-option
                  v-for="(item,index) in allRoles"
                  :key="user.id+'-'+item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
              <el-button type="text" icon="el-icon-more" style="padding-top: 0px" slot="reference"
                         @click="showRole(user.roles,user.id,index)"></el-button>
            </el-popover>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog
      title="更换用户头像"
      :visible.sync="avatarUploadDialogVisible"
      width="400px"
      :before-close="closeAvatarDialog">
      <div style="text-align: center; margin-bottom: 20px;">
        <span>正在为用户 <strong>{{ currentUserForAvatar?.nickname }}</strong> 更换头像</span>
      </div>
      <el-upload
        class="avatar-uploader"
        action="/upload/avatar"
        :headers="uploadHeaders"
        :show-file-list="false"
        :on-success="handleAvatarUploadSuccess"
        :on-error="handleAvatarUploadError"
        :before-upload="beforeAvatarUpload">
        <img v-if="tempAvatar" :src="tempAvatar" class="avatar">
        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeAvatarDialog">取 消</el-button>
        <el-button type="primary" @click="confirmAvatarUpdate" :disabled="!tempAvatar || avatarUploading">
          {{ avatarUploading ? '上传中...' : '确 定' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
  import {getRequest} from '../utils/api'
  import {putRequest} from '../utils/api'
  import {deleteRequest} from '../utils/api'
  export default{
    mounted: function () {
      this.loading = true;
      this.loadUserList();
      this.cardloading = Array.apply(null, Array(20)).map(function (item, i) {
        return false;
      });
      this.eploading = Array.apply(null, Array(20)).map(function (item, i) {
        return false;
      });
      this.setUploadHeaders();
      this.checkAdmin();
    },
    methods: {
      saveRoles(id, index){
        var selRoles = this.roles;
        if (this.cpRoles.length == selRoles.length) {
          for (var i = 0; i < this.cpRoles.length; i++) {
            for (var j = 0; j < selRoles.length; j++) {
              if (this.cpRoles[i].id == selRoles[j]) {
                selRoles.splice(j, 1);
                break;
              }
            }
          }
          if (selRoles.length == 0) {
            return;
          }
        }
        var _this = this;
        _this.cardloading.splice(index, 1, true)
        putRequest("/admin/user/role", {rids: this.roles, id: id}).then(resp=> {
          if (resp.status == 200 && resp.data.status == 'success') {
            _this.$message({type: resp.data.status, message: resp.data.msg});
            _this.loadOneUserById(id, index);
          } else {
            _this.cardloading.splice(index, 1, false)
            _this.$message({type: 'error', message: '更新失败!'});
          }
        }, resp=> {
          _this.cardloading.splice(index, 1, false)
          if (resp.response.status == 403) {
            var data = resp.response.data;
            _this.$message({type: 'error', message: data});
          }
        });
      },
      showRole(aRoles, id, index){
        this.cpRoles = aRoles;
        this.roles = [];
        this.loadRoles(index);
        for (var i = 0; i < aRoles.length; i++) {
          this.roles.push(aRoles[i].id);
        }
      },
      deleteUser(id){
        var _this = this;
        this.$confirm('删除该用户, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          _this.loading = true;
          deleteRequest("/admin/user/" + id).then(resp=> {
            if (resp.status == 200 && resp.data.status == 'success') {
              _this.$message({type: 'success', message: '删除成功!'})
              _this.loadUserList();
              return;
            }
            _this.loading = false;
            _this.$message({type: 'error', message: '删除失败!'})
          }, resp=> {
            _this.loading = false;
            _this.$message({type: 'error', message: '删除失败!'})
          });
        }).catch(() => {
          _this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
      },
      enabledChange(enabled, id, index){
        var _this = this;
        _this.cardloading.splice(index, 1, true)
        putRequest("/admin/user/enabled", {enabled: enabled, uid: id}).then(resp=> {
          if (resp.status != 200) {
            _this.$message({type: 'error', message: '更新失败!'})
            _this.loadOneUserById(id, index);
            return;
          }
          _this.cardloading.splice(index, 1, false)
          _this.$message({type: 'success', message: '更新成功!'})
        }, resp=> {
          _this.$message({type: 'error', message: '更新失败!'})
          _this.loadOneUserById(id, index);
        });
      },
      loadRoles(index){
        var _this = this;
        _this.eploading.splice(index, 1, true)
        getRequest("/admin/roles").then(resp=> {
          _this.eploading.splice(index, 1, false)
          if (resp.status == 200) {
            _this.allRoles = resp.data;
          } else {
            _this.$message({type: 'error', message: '数据加载失败!'});
          }
        }, resp=> {
          _this.eploading.splice(index, 1, false)
          if (resp.response.status == 403) {
            var data = resp.response.data;
            _this.$message({type: 'error', message: data});
          }
        });
      },
      loadOneUserById(id, index){
        var _this = this;
        getRequest("/admin/user/" + id).then(resp=> {
          _this.cardloading.splice(index, 1, false)
          if (resp.status == 200) {
            _this.users.splice(index, 1, resp.data);
          } else {
            _this.$message({type: 'error', message: '数据加载失败!'});
          }
        }, resp=> {
          _this.cardloading.splice(index, 1, false)
          if (resp.response.status == 403) {
            var data = resp.response.data;
            _this.$message({type: 'error', message: data});
          }
        });
      },
      loadUserList(){
        var _this = this;
        getRequest("/admin/user?nickname="+this.keywords).then(resp=> {
          _this.loading = false;
          if (resp.status == 200) {
            _this.users = resp.data;
          } else {
            _this.$message({type: 'error', message: '数据加载失败!'});
          }
        }, resp=> {
          _this.loading = false;
          if (resp.response.status == 403) {
            var data = resp.response.data;
            _this.$message({type: 'error', message: data});
          }
        });
      },
      searchClick(){
        this.loading = true;
        this.loadUserList();
      },

      // 头像上传相关方法
      setUploadHeaders() {
        const token = localStorage.getItem('token');
        if (token) {
          this.uploadHeaders = {
            'Authorization': 'Bearer ' + token
          };
        }
      },

      checkAdmin() {
        const _this = this;
        getRequest("/isAdmin").then(resp=> {
          if (resp.status == 200) {
            _this.isAdmin = resp.data;
          }
        });
      },

      showAvatarUpload(user) {
        this.currentUserForAvatar = user;
        this.tempAvatar = '';
        this.avatarUploadDialogVisible = true;
      },

      closeAvatarDialog() {
        this.avatarUploadDialogVisible = false;
        this.currentUserForAvatar = null;
        this.tempAvatar = '';
        this.avatarUploading = false;
      },

      handleAvatarUploadSuccess(res, file) {
        this.avatarUploading = false;
        if (res.status === 'success') {
          this.tempAvatar = res.data.url;
          this.$message.success('头像上传成功');
        } else {
          this.$message.error(res.msg || '头像上传失败');
        }
      },

      handleAvatarUploadError(err, file) {
        this.avatarUploading = false;
        this.$message.error('头像上传失败，请重试');
        console.error('上传错误:', err);
      },

      beforeAvatarUpload(file) {
        this.avatarUploading = true;
        const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
        const isLt2M = file.size / 1024 / 1024 < 2;

        if (!isJPG) {
          this.$message.error('头像图片只能是 JPG/PNG 格式!');
          this.avatarUploading = false;
        }
        if (!isLt2M) {
          this.$message.error('头像图片大小不能超过 2MB!');
          this.avatarUploading = false;
        }
        return isJPG && isLt2M;
      },

      confirmAvatarUpdate() {
        if (!this.tempAvatar || !this.currentUserForAvatar) {
          return;
        }

        const _this = this;
        // 找到用户在数组中的索引
        const userIndex = _this.users.findIndex(u => u.id === _this.currentUserForAvatar.id);

        if (userIndex !== -1) {
          // 更新头像
          _this.users[userIndex].userface = _this.tempAvatar;
          _this.$message.success(`用户 ${_this.currentUserForAvatar.nickname} 的头像更新成功`);
        }

        _this.closeAvatarDialog();
      }
    },
    data(){
      return {
        loading: false,
        eploading: [],
        cardloading: [],
        keywords: '',
        users: [],
        allRoles: [],
        roles: [],
        cpRoles: [],
        // 头像上传相关
        avatarUploadDialogVisible: false,
        currentUserForAvatar: null,
        tempAvatar: '',
        avatarUploading: false,
        uploadHeaders: {},
        isAdmin: false
      }
    }
  }
}
</script>

<style scoped>
  .avatar-uploader {
    text-align: center;
  }

  .avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }

  .avatar-uploader .el-upload:hover {
    border-color: #409EFF;
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }

  .avatar {
    width: 178px;
    height: 178px;
    display: block;
    object-fit: cover;
  }
</style>
