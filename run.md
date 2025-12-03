# V部落博客系统本地运行文档

## 项目概述

V部落是一个基于Vue + SpringBoot开发的多用户博客管理平台，包含前端Vue项目和后端SpringBoot项目。

## 项目结构

```
VBlog/
├── blogserver/          # SpringBoot后端项目
├── vueblog/            # Vue前端项目
├── doc/                # 项目文档
├── README.md           # 项目说明
└── run.md             # 本地运行文档（本文件）
```

## 环境要求

### 基础环境
- **操作系统**: Windows 10/11
- **Java**: JDK 1.8+
- **Node.js**: 4.0.0+
- **npm**: 3.0.0+
- **MySQL**: 5.7+

### 开发工具（可选）
- **后端**: IntelliJ IDEA
- **前端**: WebStorm 或 VSCode
- **数据库**: MySQL Workbench 或 Navicat

## 运行步骤

### 第一步：准备工作

1. **确保MySQL已安装并启动**
2. **确保Java 8+已安装**
3. **确保Node.js和npm已安装**

验证环境：
```powershell
# 检查Java版本
java -version

# 检查Node.js版本
node -v

# 检查npm版本
npm -v
```

### 第二步：数据库配置

1. **创建数据库**
   ```sql
   CREATE DATABASE vueblog2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **导入数据库脚本**
   - 找到 `blogserver/src/main/resources/vueblog.sql` 文件
   - 在MySQL中执行该脚本，创建表结构和初始数据

3. **验证数据库**
   ```sql
   USE vueblog2;
   SHOW TABLES;
   ```

### 第三步：后端配置

1. **修改数据库连接配置**
   
   编辑 `blogserver/src/main/resources/application.properties` 文件：

   ```properties
   spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
   spring.datasource.url=jdbc:mysql://localhost:3306/vueblog2?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
   spring.datasource.username=root        # 修改为你的MySQL用户名
   spring.datasource.password=123         # 修改为你的MySQL密码
   
   mybatis.config-location=classpath:/mybatis-config.xml
   server.port=8081
   logging.level.org.springframework.security=info
   ```

2. **启动后端服务**
   
   使用IntelliJ IDEA打开 `blogserver` 项目，找到主启动类并运行，或者使用命令行：

   ```powershell
   cd blogserver
   mvn spring-boot:run
   ```

3. **验证后端启动**
   
   访问 `http://localhost:8081`，应该能看到响应信息。

### 第四步：前端配置

1. **安装前端依赖**
   
   ```powershell
   cd vueblog
   npm install
   ```

2. **启动前端开发服务器**
   
   ```powershell
   npm run dev
   ```

3. **验证前端启动**
   
   访问 `http://localhost:8080`，应该能看到V部落登录页面。

### 第五步：访问系统

- **前端开发环境**: http://localhost:8080
- **后端API服务**: http://localhost:8081

## 项目启动说明

### 开发模式运行

**方式一：分别启动前后端**

1. 启动后端：
   ```powershell
   cd blogserver
   mvn spring-boot:run
   ```

2. 启动前端：
   ```powershell
   cd vueblog
   npm run dev
   ```

3. 访问：http://localhost:8080

**方式二：仅启动后端（静态文件方式）**

如果已经构建过前端，可以只启动后端：

1. 启动后端：
   ```powershell
   cd blogserver
   mvn spring-boot:run
   ```

2. 访问：http://localhost:8081/index.html

## 生产部署

### 构建前端项目

```powershell
cd vueblog
npm run build
```

构建完成后，会在 `vueblog/dist` 目录下生成静态文件。

### 部署到SpringBoot

1. 将 `vueblog/dist` 目录下的所有文件复制到 `blogserver/src/main/resources/static/` 目录下
2. 重新启动SpringBoot应用
3. 访问：http://localhost:8081

## 常见问题解决

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库名称、用户名、密码配置正确
- 检查防火墙设置

### 2. 前端安装依赖失败
- 清除npm缓存：`npm cache clean --force`
- 使用淘宝镜像：`npm install --registry=https://registry.npm.taobao.org`

### 3. 端口冲突
- 后端默认端口：8081（可在application.properties中修改）
- 前端默认端口：8080（可在vueblog/config/index.js中修改）

### 4. 跨域问题
- 前端已配置代理转发到后端，一般不会出现跨域问题
- 如果仍有问题，检查 `vueblog/config/index.js` 中的proxyTable配置

## 默认账号信息

根据数据库初始化脚本，默认管理员账号：
- 用户名：admin
- 密码：123

## 技术栈

### 后端技术
- SpringBoot 2.2.7
- Spring Security
- MyBatis
- MySQL
- Druid连接池

### 前端技术
- Vue 2.5.2
- Element UI 2.0.8
- Axios
- Vue Router
- Vue ECharts
- Mavon Editor

## 开发建议

1. **后端开发**：使用IntelliJ IDEA，可以直接运行和调试
2. **前端开发**：使用WebStorm或VSCode，配合Vue DevTools进行调试
3. **数据库管理**：使用MySQL Workbench或Navicat管理数据库
4. **版本控制**：使用Git进行代码版本管理

## 项目结构说明

### 后端项目结构（blogserver）
```
src/
├── main/
│   ├── java/
│   │   └── org/sang/          # Java源码
│   └── resources/
│       ├── application.properties    # 配置文件
│       ├── vueblog.sql             # 数据库脚本
│       ├── static/                 # 静态资源
│       └── templates/              # 模板文件
└── test/                    # 测试代码
```

### 前端项目结构（vueblog）
```
src/
├── assets/                  # 静态资源
├── components/              # Vue组件
├── router/                  # 路由配置
├── App.vue                  # 根组件
└── main.js                  # 入口文件

config/
├── index.js                 # 项目配置
└── ...

build/                      # 构建配置
```

---

**注意：** 本文档基于项目当前版本编写，如果项目有更新，请以实际情况为准。