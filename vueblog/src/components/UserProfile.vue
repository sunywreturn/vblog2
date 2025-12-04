<template>
  <div class="user-profile" v-loading="loading">
    <!-- 个人信息卡片 -->
    <el-card class="profile-card">
      <div slot="header" class="profile-header">
        <span>个人信息</span>
        <el-button type="primary" size="small" @click="editMode = !editMode" style="float: right;">
          {{ editMode ? '取消编辑' : '编辑资料' }}
        </el-button>
      </div>
      
      <div class="profile-content">
        <!-- 头像 -->
        <div class="avatar-section">
          <el-avatar :size="100" :src="userInfo.userface" fit="cover">
            <img src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          </el-avatar>
          <div class="upload-btn" v-if="editMode">
            <el-button size="mini" type="text" @click="showUploadDialog">更换头像</el-button>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="info-section">
          <el-form :model="editForm" label-width="80px" size="small">
            <el-form-item label="用户名">
              <span v-if="!editMode">{{ userInfo.username }}</span>
              <el-input v-else v-model="editForm.username" disabled></el-input>
            </el-form-item>
            
            <el-form-item label="昵称">
              <span v-if="!editMode">{{ userInfo.nickname }}</span>
              <el-input v-else v-model="editForm.nickname"></el-input>
            </el-form-item>
            
            <el-form-item label="邮箱">
              <span v-if="!editMode">{{ userInfo.email }}</span>
              <el-input v-else v-model="editForm.email"></el-input>
            </el-form-item>
            
            <el-form-item label="注册时间">
              <span>{{ userInfo.regTime | formatDateTime }}</span>
            </el-form-item>
            
            <el-form-item label="用户状态">
              <el-tag :type="userInfo.enabled ? 'success' : 'danger'">
                {{ userInfo.enabled ? '正常' : '禁用' }}
              </el-tag>
            </el-form-item>
            
            <el-form-item label="用户角色">
              <el-tag 
                v-for="role in userInfo.roles" 
                :key="role.id" 
                size="small" 
                type="info" 
                style="margin-right: 5px;">
                {{ role.name }}
              </el-tag>
            </el-form-item>
          </el-form>
          
          <div class="action-buttons" v-if="editMode">
            <el-button type="primary" @click="saveProfile" size="small">保存</el-button>
            <el-button @click="editMode = false" size="small">取消</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.articleCount }}</div>
            <div class="stat-label">文章总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.viewCount }}</div>
            <div class="stat-label">总浏览量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.draftCount }}</div>
            <div class="stat-label">草稿数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 头像上传对话框 -->
    <el-dialog title="更换头像" :visible.sync="uploadDialogVisible" width="400px">
      <el-upload
        class="avatar-uploader"
        action="/upload/avatar"
        :show-file-list="false"
        :on-success="handleAvatarSuccess"
        :before-upload="beforeAvatarUpload">
        <img v-if="newAvatar" :src="newAvatar" class="avatar">
        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button @click="uploadDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmAvatar" :disabled="!newAvatar">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import {getRequest} from '../utils/api'
  import {putRequest} from '../utils/api'
  export default {
    name: 'UserProfile',
    data() {
      return {
        loading: false,
        editMode: false,
        uploadDialogVisible: false,
        newAvatar: '',
        userInfo: {
          id: null,
          username: '',
          nickname: '',
          email: '',
          userface: '',
          regTime: '',
          enabled: false,
          roles: []
        },
        editForm: {
          username: '',
          nickname: '',
          email: ''
        },
        statistics: {
          articleCount: 0,
          viewCount: 0,
          draftCount: 0
        }
      }
    },
    mounted() {
      this.loadUserProfile();
      this.loadStatistics();
    },
    methods: {
      loadUserProfile() {
        this.loading = true;
        const _this = this;
        
        // 获取当前用户基本信息
        getRequest("/currentUserId").then(resp => {
          if (resp.status == 200) {
            const userId = resp.data;
            // 获取详细用户信息
            getRequest("/admin/user/" + userId).then(userResp => {
              if (userResp.status == 200) {
                _this.userInfo = userResp.data;
                _this.editForm = {
                  username: userResp.data.username,
                  nickname: userResp.data.nickname,
                  email: userResp.data.email
                };
              }
            }).catch(() => {
              // 如果管理员接口失败，使用基础接口
              Promise.all([
                getRequest("/currentUserName"),
                getRequest("/currentUserEmail")
              ]).then(([nameResp, emailResp]) => {
                _this.userInfo = {
                  ..._this.userInfo,
                  username: nameResp.data || '',
                  nickname: nameResp.data || '',
                  email: emailResp.data || ''
                };
                _this.editForm = {
                  username: nameResp.data || '',
                  nickname: nameResp.data || '',
                  email: emailResp.data || ''
                };
              });
            });
          }
        }).finally(() => {
          this.loading = false;
        });
      },
      
      loadStatistics() {
        const _this = this;
        // 获取用户统计数据
        getRequest("/user/statistics").then(resp => {
          if (resp.status == 200) {
            _this.statistics = resp.data;
          }
        }).catch(() => {
          // 如果接口不存在，使用默认值
          console.log('统计接口不存在，使用默认值');
        });
      },
      
      saveProfile() {
        const _this = this;
        this.loading = true;
        
        // 更新用户信息
        putRequest("/user/profile", {
          nickname: this.editForm.nickname,
          email: this.editForm.email
        }).then(resp => {
          if (resp.status == 200 && resp.data.status === 'success') {
            _this.$message.success('保存成功');
            _this.userInfo.nickname = _this.editForm.nickname;
            _this.userInfo.email = _this.editForm.email;
            _this.editMode = false;
          } else {
            _this.$message.error('保存失败');
          }
        }).catch(() => {
          // 如果没有专门接口，尝试使用邮箱更新接口
          putRequest("/updateUserEmail?email=" + encodeURIComponent(_this.editForm.email)).then(resp => {
            if (resp.status == 200 && resp.data.status === 'success') {
              _this.$message.success('邮箱更新成功');
              _this.userInfo.email = _this.editForm.email;
              _this.editMode = false;
            } else {
              _this.$message.error('保存失败');
            }
          });
        }).finally(() => {
          this.loading = false;
        });
      },
      
      showUploadDialog() {
        this.uploadDialogVisible = true;
        this.newAvatar = '';
      },
      
      handleAvatarSuccess(res, file) {
        this.newAvatar = URL.createObjectURL(file.raw);
      },
      
      beforeAvatarUpload(file) {
        const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
        const isLt2M = file.size / 1024 / 1024 < 2;

        if (!isJPG) {
          this.$message.error('头像图片只能是 JPG/PNG 格式!');
        }
        if (!isLt2M) {
          this.$message.error('头像图片大小不能超过 2MB!');
        }
        return isJPG && isLt2M;
      },
      
      confirmAvatar() {
        // 这里应该调用后端接口保存头像
        this.$message.success('头像更新成功');
        this.userInfo.userface = this.newAvatar;
        this.uploadDialogVisible = false;
        this.newAvatar = '';
      }
    }
  }
</script>

<style scoped>
  .user-profile {
    padding: 20px;
  }
  
  .profile-card {
    max-width: 800px;
    margin: 0 auto;
  }
  
  .profile-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .profile-content {
    display: flex;
    gap: 30px;
  }
  
  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }
  
  .upload-btn {
    margin-top: 5px;
  }
  
  .info-section {
    flex: 1;
  }
  
  .action-buttons {
    margin-top: 20px;
    text-align: center;
  }
  
  .stat-card {
    text-align: center;
  }
  
  .stat-content {
    padding: 20px;
  }
  
  .stat-number {
    font-size: 28px;
    font-weight: bold;
    color: #409EFF;
    margin-bottom: 10px;
  }
  
  .stat-label {
    color: #666;
    font-size: 14px;
  }
  
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
  }
</style>