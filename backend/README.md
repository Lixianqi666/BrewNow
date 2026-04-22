# 沏刻茶叶电商平台 - 后端服务

## 项目简介

基于SpringBoot 2.7 + MyBatis + MySQL构建的沏刻茶叶电商平台后端服务，实现前后端分离架构，提供完整的电商功能支持。

## 技术架构

### 核心技术栈
- **框架**: Spring Boot 2.7.18
- **数据访问**: MyBatis 2.3.2
- **数据库**: MySQL 8.0+
- **连接池**: Druid 1.2.20
- **工具库**: Hutool 5.8.22
- **认证**: JWT 3.19.2
- **构建工具**: Maven
- **JDK版本**: JDK 17

### 项目结构
```
src/main/java/com/brewnow/
├── BrewNowApplication.java         # 启动类
├── common/                         # 通用响应与公共类型
├── config/                         # 配置类（CORS、异常处理、OpenAPI、缓存、MinIO）
├── controller/                     # 接口层（user/product/order/cart/admin/merchant 等）
├── dto/                            # 数据传输对象
├── entity/                         # 实体类
├── enums/                          # 枚举类
├── interceptor/                    # 拦截器（JWT、审计）
├── mapper/                         # MyBatis Mapper 接口
├── service/                        # 服务接口与实现
└── utils/                          # 工具类
  ├── JwtUtil.java
  └── PasswordUtil.java

src/main/resources/
├── application.yml                 # 应用配置
├── logback-spring.xml              # 日志配置
└── mapper/                         # MyBatis 映射文件（11 个 XML）
```

## 数据库设计

### 核心表结构

1. **users** - 消费者用户表
2. **admins** - 管理员表
3. **merchants** - 商家表
4. **products** - 商品表
5. **orders** - 订单表
6. **order_items** - 订单项表
7. **shopping_carts** - 购物车表
8. **cart_items** - 购物车项表
9. **addresses** - 地址表
10. **product_reviews** - 商品评价表
11. **user_favorites / user_behavior_logs** - 收藏与行为日志表

### 特色功能
- 完整的外键约束关系
- 智能触发器（用户注册自动创建购物车、订单金额自动计算、库存自动管理）
- 性能优化索引（25个高效索引覆盖常用查询）
- 软删除支持

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 运行步骤

1. **克隆项目**
```bash
git clone <project-url>
cd <project-root>/backend
```

2. **配置数据库**
- 创建数据库：`brew-now`
- 修改 `application.yml` 中的数据库连接信息

3. **启动应用**
```bash
mvn spring-boot:run
```

4. **访问验证**
- 应用地址：http://localhost:8080/api
- 健康检查：http://localhost:8080/api/system/health
- 数据库监控：http://localhost:8080/api/druid

### Redis 缓存模式

默认情况下：

- `REDIS_CACHE_ENABLED=false`
- 推荐缓存使用本地 `ConcurrentMapCacheManager`

如需启用 Redis 缓存：

1. 启动本地 Redis（与项目启动脚本一致）

```bash
brew services start redis
```

2. 使用环境变量启动后端

```bash
cd backend
REDIS_CACHE_ENABLED=true REDIS_HOST=127.0.0.1 REDIS_PORT=6379 mvn spring-boot:run
```

启动日志会打印当前缓存后端，便于功能演示与运维排查：

- `Cache backend: ConcurrentMapCacheManager`
- `Cache backend: RedisCacheManager`

## API接口

以下为核心接口示例，完整接口以 `controller` 包源码为准。

### 用户模块 (/api/user)
- `POST /register` - 用户注册
- `POST /login` - 用户登录
- `GET /{userId}` - 查询用户信息
- `GET /list` - 查询用户列表（分页）
- `PUT /update` - 更新用户信息
- `PUT /change-password` - 修改密码
- `DELETE /{userId}` - 删除用户

### 商品模块 (/api/product)
- `GET /list` - 查询商品列表（分页）
- `GET /{productId}` - 查询商品详情
- `GET /category/{category}` - 按分类查询商品
- `GET /search` - 搜索商品
- `POST /add` - 添加商品
- `PUT /update` - 更新商品
- `DELETE /{productId}` - 删除商品

### 系统模块 (/api/system)
- `GET /health` - 健康检查
- `GET /info` - 系统信息
- `GET /docs` - API文档

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/brew-now
    username: root
    password: 200306
```

### JWT配置
```yaml
jwt:
  secret: brew-now-secret-key-2026
  expire: 7200  # 2小时
```

### 文件上传配置
```yaml
file:
  upload:
    max-size: 10MB

minio:
  endpoint: http://127.0.0.1:9000
  bucket: brew-now
```

## 业务特色

### 1. 智能触发器
- 用户注册时自动创建购物车
- 订单项小计自动计算
- 订单总额实时更新
- 支付时自动库存管理

### 2. 完整权限体系
- JWT Token认证
- 多角色权限管理
- 接口访问控制

### 3. 数据完整性
- 外键约束保护
- 软删除机制
- 数据校验注解

### 4. 性能优化
- 连接池优化
- 索引策略完善
- 分页查询支持

## 开发规范

### 代码规范
- 统一使用Result包装响应结果
- 异常统一处理
- 日志规范记录
- 注释完整清晰

### 数据库规范
- 表名使用下划线命名
- 字段名对应实体类驼峰命名
- 主键统一使用自增ID
- 时间字段统一使用LocalDateTime