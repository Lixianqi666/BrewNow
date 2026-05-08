# 沏刻茶叶电商平台

一个基于 Vue 3 + Spring Boot 的全栈茶叶电商系统，支持消费者、商家、管理员三端操作，内置基于用户行为的协同过滤推荐算法。

## 项目概览

本项目是一个完整的毕业设计项目，实现了茶叶电商的完整业务流程，包括商品展示、购物车、订单管理、评价系统、智能推荐等功能。

### 功能特性

- **消费者端**：商品浏览、搜索、购物车、下单支付、订单追踪、商品评价、收藏、个性化推荐
- **商家端**：商品管理、订单处理、商家资料管理、销售数据查看
- **管理端**：用户管理、商家审核、商品审核、订单管理、推荐数据统计
- **智能推荐**：基于用户行为的协同过滤推荐算法，支持实时推荐与推荐效果评估
- **文件存储**：MinIO 对象存储服务，支持商品图片、用户头像上传
- **缓存支持**：本地缓存 / Redis 缓存可切换

## 技术栈

### 前端
- Vue 3.5 + TypeScript
- Vite 6 构建工具
- Element Plus 2.10 组件库
- Vue Router 4 路由管理
- Pinia 3 状态管理
- Axios HTTP 客户端

### 后端
- Spring Boot 2.7
- MyBatis 2.3
- MySQL 8.0
- Druid 连接池
- JWT 认证
- MinIO 对象存储
- Redis 缓存（可选）

### 基础设施
- Nginx 反向代理
- Maven 构建管理
- npm / Node.js 前端构建

## 项目结构

```
沏刻茶叶电商平台/
├── backend/                 # Spring Boot 后端服务
│   ├── src/main/java/       # Java 源码
│   ├── src/main/resources/  # 配置文件与 MyBatis 映射
│   ├── src/test/java/       # 测试代码
│   ├── pom.xml              # Maven 配置
│   └── README.md            # 后端详细说明
├── frontend/                # Vue 3 前端项目
│   ├── src/                 # 前端源码
│   ├── package.json         # npm 依赖
│   └── README.md            # 前端详细说明
├── sql/                     # 数据库初始化脚本
│   ├── brew-now.sql         # 完整数据库导出（含测试数据）
│   └── init.sql             # 基础初始化脚本
├── nginx/                   # Nginx 配置文件
│   └── brew-now.conf        # 反向代理配置
├── .github/                 # GitHub 配置
│   ├── agents/              # GitHub Copilot Agent 指令
│   └── copilot-instructions.md
├── .env.example             # 环境变量模板
├── .gitignore               # Git 忽略规则
├── start.command            # 一键启动脚本（macOS）
└── stop.command             # 一键停止脚本（macOS）
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+ / npm 9+
- MySQL 8.0+
- Redis（可选）
- MinIO（可选，用于文件存储）

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE \`brew-now\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入数据库脚本
mysql -u root -p brew-now < sql/brew-now.sql
```

### 2. 启动后端服务

```bash
cd backend

# 方式一：使用 Maven 直接运行
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/brew-now-backend-1.0.0.jar
```

后端默认运行在 `http://localhost:8080/api`

### 3. 启动前端服务

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 4. 使用启动脚本（macOS）

```bash
# 启动所有服务
./start.command

# 停止所有服务
./stop.command
```

## 默认账号

### 管理员
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| manager | 123456 | 管理员 |
| operator | 123456 | 运营专员 |

### 商家
| 用户名 | 密码 | 商家 |
|--------|------|------|
| BREW001 | 123456 | 沏刻茶业有限公司 |
| BREW002 | 123456 | 云岭茶坊有限公司 |

### 消费者
| 用户名 | 密码 |
|--------|------|
| user001 | 123456 |
| chen_ming | 123456 |
| liu_fang | 123456 |

## 文档

- [用户手册](./docs/用户手册.md) - 各角色功能使用说明
- [部署指南](./docs/部署指南.md) - 生产环境部署说明
- [后端文档](./backend/README.md) - 后端架构与 API 说明
- [前端文档](./frontend/README.md) - 前端开发与构建说明

## 环境变量

复制 `.env.example` 为 `.env` 并修改配置：

```bash
cp .env.example .env
```

主要配置项：
- 数据库连接（MySQL）
- MinIO 对象存储
- JWT 密钥
- Redis 缓存（可选）

## API 文档

启动后端服务后，访问 Swagger UI：

```
http://localhost/api/swagger-ui.html
```

## 许可证

本项目仅供学习与毕业设计使用。
