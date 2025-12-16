<template>
  <div class="myhome-container">
    <el-card class="profile-card">
      <div slot="header" class="clearfix">
        <span>个人主页</span>
      </div>
      
      <div class="profile-content">
        <div class="avatar-section">
          <el-avatar :size="100" :src="userInfo.userface" class="user-avatar">
            {{ userInfo.nickname ? userInfo.nickname.charAt(0) : 'U' }}
          </el-avatar>
          <h2 class="username">{{ userInfo.nickname || '用户' }}</h2>
          <p class="user-id">ID: {{ userInfo.id }}</p>
        </div>
        
        <el-divider></el-divider>
        
        <div class="info-section">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <i class="el-icon-user"></i>
                <span class="label">用户名:</span>
                <span class="value">{{ userInfo.username || '未设置' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <i class="el-icon-message"></i>
                <span class="label">邮箱:</span>
                <span class="value">{{ userInfo.email || '未设置' }}</span>
              </div>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
              <div class="info-item">
                <i class="el-icon-date"></i>
                <span class="label">注册时间:</span>
                <span class="value">{{ formatDate(userInfo.regTime) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <i class="el-icon-s-check"></i>
                <span class="label">账户状态:</span>
                <span class="value">{{ userInfo.enabled ? '正常' : '禁用' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <el-divider></el-divider>
        
        <div class="stats-section">
          <h3>个人统计</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">{{ articleCount }}</div>
                <div class="stat-label">文章数量</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">0</div>
                <div class="stat-label">获赞数量</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-number">0</div>
                <div class="stat-label">评论数量</div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <el-divider></el-divider>
        
        <div class="action-section">
          <el-button type="primary" icon="el-icon-edit" @click="editProfile">编辑资料</el-button>
          <el-button icon="el-icon-setting" @click="goToSettings">账户设置</el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 编辑资料对话框 -->
    <el-dialog title="编辑资料" :visible.sync="editDialogVisible" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {getRequest, putRequest} from '../utils/api'

export default {
  name: 'MyHome',
  data() {
    return {
      userInfo: {},
      articleCount: 0,
      editDialogVisible: false,
      editForm: {
        nickname: '',
        email: ''
      }
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadArticleCount()
  },
  methods: {
    loadUserInfo() {
      getRequest("/currentUserInfo").then(response => {
        if (response.data) {
          this.userInfo = response.data
          this.editForm.nickname = response.data.nickname
          this.editForm.email = response.data.email
        }
      }).catch(error => {
        console.error('获取用户信息失败:', error)
        this.$message.error('获取用户信息失败')
      })
    },
    
    loadArticleCount() {
      getRequest("/article/countByUser").then(response => {
        if (response.data) {
          this.articleCount = response.data
        }
      }).catch(error => {
        console.error('获取文章数量失败:', error)
      })
    },
    
    formatDate(date) {
      if (!date) return '未知'
      return new Date(date).toLocaleString('zh-CN')
    },
    
    editProfile() {
      this.editDialogVisible = true
    },
    
    saveProfile() {
      putRequest("/updateUserInfo", this.editForm).then(response => {
        if (response.status === 'success') {
          this.$message.success('保存成功')
          this.editDialogVisible = false
          this.loadUserInfo()
        } else {
          this.$message.error('保存失败')
        }
      }).catch(error => {
        console.error('保存失败:', error)
        this.$message.error('保存失败')
      })
    },
    
    goToSettings() {
      this.$message.info('账户设置功能开发中...')
    }
  }
}
</script>

<style scoped>
.myhome-container {
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.profile-content {
  text-align: center;
}

.avatar-section {
  margin-bottom: 20px;
}

.user-avatar {
  margin-bottom: 10px;
  background-color: #409EFF;
  color: white;
  font-size: 40px;
  font-weight: bold;
}

.username {
  margin: 10px 0 5px 0;
  color: #303133;
}

.user-id {
  color: #909399;
  font-size: 14px;
}

.info-section {
  text-align: left;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-item i {
  margin-right: 10px;
  color: #409EFF;
  font-size: 16px;
}

.info-item .label {
  margin-right: 10px;
  color: #606266;
  font-weight: 500;
}

.info-item .value {
  color: #303133;
}

.stats-section h3 {
  margin-bottom: 20px;
  color: #303133;
}

.stat-item {
  text-align: center;
  padding: 20px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.action-section {
  margin-top: 20px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
</style>