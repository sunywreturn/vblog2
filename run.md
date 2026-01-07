# V部落博客系统 - 本地运行指南

## 项目概述

V部落是一个多用户博客管理平台，采用前后端分离架构：
- **后端**: Spring Boot + Spring Security + MyBatis + MySQL
- **前端**: Vue + ElementUI + axios + vue-router

## 环境要求

### 必需软件
1. **Java JDK** 1.8 或以上版本
2. **MySQL** 5.7 或以上版本
3. **Node.js** 14.x 或以上版本 
4. **Maven** 3.6 或以上版本（可选，项目自带Maven Wrapper）

### 开发工具（推荐）
- **后端**: IntelliJ IDEA
- **前端**: WebStorm 或 VS Code
- **数据库管理**: MySQL Workbench 或 phpMyAdmin

## 项目结构

```
VBlog/
├── blogserver/          # Spring Boot后端项目
│   ├── src/
│   ├── pom.xml
│   └── mvnw, mvnw.cmd   # Maven Wrapper
├── vueblog/             # Vue前端项目
│   ├── src/
│   ├── package.json
│   └── static/
└── doc/                 # 项目文档
```

## 快速启动指南

### 第一步：克隆项目

```bash
git clone https://github.com/lenve/VBlog.git
cd VBlog
```

### 第二步：数据库配置

1. **创建数据库**
   ```sql
   CREATE DATABASE vueblog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **导入数据**
   - 找到文件：`blogserver/src/main/resources/vueblog.sql`
   - 在MySQL中执行该SQL文件

3. **修改数据库连接配置**
   
   编辑文件：`blogserver/src/main/resources/application.properties`
   ```properties
   # 根据你的实际情况修改以下配置
   spring.datasource.url=jdbc:mysql://localhost:3306/vueblog?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
   spring.datasource.username=root
   spring.datasource.password=你的MySQL密码
   ```

### 第三步：启动后端服务

**方式一：使用IDE运行**
1. 在IntelliJ IDEA中打开 `blogserver` 项目
2. 找到主启动类（通常在 `org.sang` 包下）
3. 右键运行主启动类

**方式二：使用命令行运行**
```bash
cd blogserver

# Windows用户
.\mvnw.cmd spring-boot:run

# Linux/Mac用户  
./mvnw spring-boot:run
```

服务启动成功后，访问：http://localhost:8081

出现Spring Boot页面或无错误日志表示后端启动成功。

### 第四步：启动前端服务

1. **安装依赖**
   ```bash
   cd vueblog
   npm install
   ```

   如果安装遇到网络问题，可以使用淘宝镜像：
   ```bash
   npm config set registry https://registry.npmmirror.com
   npm install
   ```

2. **启动开发服务器**
   ```bash
   npm run dev
   ```

   前端服务启动后，访问：http://localhost:8080

### 第五步：访问系统

- **开发模式**: http://localhost:8080
  - 前端Vue开发服务器 + 后端Spring Boot服务
  - 适合开发调试，支持热重载

- **生产模式**: http://localhost:8081/index.html  
  - 仅后端Spring Boot服务
  - 适合测试验证

## 生产部署

### 构建前端项目

```bash
cd vueblog
npm run build
```

构建成功后，会在 `vueblog/dist` 目录下生成：
- `index.html`
- `static/` 文件夹

### 部署到Spring Boot

将 `dist` 目录下的所有文件复制到 `blogserver/src/main/resources/static/` 目录下：
```bash
cp -r vueblog/dist/* blogserver/src/main/resources/static/
```

然后重启Spring Boot服务，即可通过 http://localhost:8081 访问完整应用。

## 常见问题解决

### 1. 数据库连接失败
**问题**: `Could not create connection to database server`

**解决方案**:
- 检查MySQL服务是否启动
- 验证数据库URL、用户名、密码是否正确
- 确认数据库名称是否正确

### 2. 前端依赖安装失败
**问题**: `npm install` 出现网络错误

**解决方案**:
```bash
# 使用淘宝镜像
npm config set registry https://registry.npmmirror.com

# 清除缓存重新安装
npm cache clean --force
npm install
```

### 3. 端口冲突
**问题**: 端口8080或8081被占用

**解决方案**:
- 修改后端端口：编辑 `blogserver/src/main/resources/application.properties` 中的 `server.port`
- 修改前端端口：编辑 `vueblog/config/index.js` 中的 `port` 配置

### 4. Maven构建失败
**问题**: Maven依赖下载失败

**解决方案**:
- 使用项目自带的Maven Wrapper：`./mvnw` 或 `mvnw.cmd`
- 配置Maven镜像为国内源

### 5. 前端页面空白
**问题**: 访问前端页面出现空白

**解决方案**:
- 检查浏览器控制台是否有JavaScript错误
- 确认后端服务是否正常启动
- 检查API接口URL配置是否正确

## 开发指南

### 后端开发
- 主要代码目录：`blogserver/src/main/java/org/sang/`
- 配置文件：`blogserver/src/main/resources/`
- 使用MyBatis进行数据库操作
- Spring Security处理用户认证

### 前端开发
- 主要代码目录：`vueblog/src/`
- 使用Vue 2.x + ElementUI
- API调用通过axios
- 路由配置在 `vueblog/src/router/index.js`

### 调试技巧
1. **后端日志**: 在 `application.properties` 中设置 `logging.level.root=debug`
2. **前端调试**: 使用浏览器开发者工具 
3. **API调试**: 使用Postman或类似工具测试接口

## 系统功能模块

- 用户管理
- 文章管理
- 栏目管理  
- 评论管理
- 数据统计
- 权限控制

## 技术架构补充说明

- **前后端分离**: Vue前端 + Spring Boot后端
- **数据持久化**: MyBatis + MySQL
- **安全认证**: Spring Security + JWT
- **UI框架**: ElementUI
- **图表展示**: vue-echarts  
- **编辑器**: mavon-editor（Markdown编辑器）

## 获取帮助

如遇到其他问题，可以：
1. 查看项目GitHub Issues
2. 关注公众号"江南一点雨"
3. 微信群咨询（微信号：a_java_boy2，备注V部落）

---

**祝您使用愉快！** 🎉