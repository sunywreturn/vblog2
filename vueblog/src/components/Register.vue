<template>
  <el-form :model="registerForm" :rules="rules" class="register-container" label-position="left"
           label-width="0px" v-loading="loading" ref="registerForm">
    <h3 class="register_title">用户注册</h3>
    <el-form-item prop="username">
      <el-input type="text" v-model="registerForm.username" auto-complete="off" placeholder="用户名"></el-input>
    </el-form-item>
    <el-form-item prop="nickname">
      <el-input type="text" v-model="registerForm.nickname" auto-complete="off" placeholder="昵称"></el-input>
    </el-form-item>
    <el-form-item prop="email">
      <el-input type="email" v-model="registerForm.email" auto-complete="off" placeholder="邮箱"></el-input>
    </el-form-item>
    <el-form-item prop="password">
      <el-input type="password" v-model="registerForm.password" auto-complete="off" placeholder="密码" show-password></el-input>
    </el-form-item>
    <el-form-item prop="confirmPassword">
      <el-input type="password" v-model="registerForm.confirmPassword" auto-complete="off" placeholder="确认密码" show-password></el-input>
    </el-form-item>
    <el-form-item style="width: 100%">
      <el-button type="primary" @click.native.prevent="submitRegister" style="width: 48%">注册</el-button>
      <el-button @click="goToLogin" style="width: 48%">返回登录</el-button>
    </el-form-item>
  </el-form>
</template>
<script>
  import {postRequest} from '../utils/api'

  export default{
    data(){
      // 验证确认密码
      var validateConfirmPassword = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'))
        } else if (value !== this.registerForm.password) {
          callback(new Error('两次输入密码不一致!'))
        } else {
          callback()
        }
      }

      // 验证邮箱格式
      var validateEmail = (rule, value, callback) => {
        const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
        if (value && !emailReg.test(value)) {
          callback(new Error('请输入正确的邮箱格式'))
        } else {
          callback()
        }
      }

      return {
        rules: {
          username: [
            {required: true, message: '请输入用户名', trigger: 'blur'},
            {min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur'}
          ],
          nickname: [
            {required: true, message: '请输入昵称', trigger: 'blur'},
            {min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur'}
          ],
          email: [
            {required: true, message: '请输入邮箱', trigger: 'blur'},
            {validator: validateEmail, trigger: 'blur'}
          ],
          password: [
            {required: true, message: '请输入密码', trigger: 'blur'},
            {min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur'}
          ],
          confirmPassword: [
            {required: true, message: '请确认密码', trigger: 'blur'},
            {validator: validateConfirmPassword, trigger: 'blur'}
          ]
        },
        registerForm: {
          username: '',
          nickname: '',
          email: '',
          password: '',
          confirmPassword: '',
          phone: ''
        },
        loading: false
      }
    },
    methods: {
      submitRegister: function () {
        var _this = this;
        this.$refs.registerForm.validate((valid) => {
          if (valid) {
            _this.loading = true;
            postRequest('/register', {
              username: _this.registerForm.username,
              nickname: _this.registerForm.nickname,
              email: _this.registerForm.email,
              password: _this.registerForm.password,
              phone: _this.registerForm.phone
            }).then(resp=> {
              _this.loading = false;
              if (resp.status == 200) {
                var json = resp.data;
                if (json.status == 'success') {
                  _this.$message.success('注册成功！即将跳转到登录页面');
                  setTimeout(() => {
                    _this.$router.replace({path: '/'});
                  }, 1500);
                } else {
                  _this.$message.error(json.msg || '注册失败，请重试');
                }
              } else {
                _this.$message.error('注册失败，请重试');
              }
            }, resp=> {
              _this.loading = false;
              _this.$message.error('连接服务器失败，请检查网络连接');
            });
          } else {
            _this.$message.warning('请正确填写注册信息');
            return false;
          }
        });
      },
      goToLogin: function () {
        this.$router.push('/');
      }
    }
  }
</script>
<style>
  .register-container {
    border-radius: 15px;
    background-clip: padding-box;
    margin: 100px auto;
    width: 400px;
    padding: 35px 35px 15px 35px;
    background: #fff;
    border: 1px solid #eaeaea;
    box-shadow: 0 0 25px #cac6c6;
  }

  .register_title {
    margin: 0px auto 40px auto;
    text-align: center;
    color: #505458;
    font-size: 24px;
    font-weight: bold;
  }
</style>
