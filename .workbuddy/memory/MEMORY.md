# 沏刻茶叶电商平台 - 长期记忆

## 项目技术栈
- **后端**: Spring Boot 2.7 + MyBatis + MySQL 8.0，端口 8080，context-path /api
- **前端**: Vue 3 + Vite + Element Plus + TypeScript，端口 5173（dev 模式）
- **数据库**: MySQL，库名 `brew-now`，用户 root / 密码 200306
- **对象存储**: MinIO，端口 9000（API）/ 9001（控制台），凭据 minioadmin/minioadmin，bucket: brew-now
- **日志**: 后端 /tmp/backend.log，MinIO /tmp/minio.log，前端 /tmp/frontend.log

## 启动方式（本地开发）
1. MySQL 通过 brew services 自动启动（已配置）
2. MinIO: `nohup minio server /path/to/minio-data --address :9000 --console-address :9001 &`
3. 后端: `cd backend && nohup mvn spring-boot:run &` （需等约 20 秒启动）
4. 前端: `cd frontend && nohup npm run dev &`

## 数据库表
address, addresses, admins, cart_items, carts, merchants, order_items, orders, product_reviews, products, shopping_carts, user_behavior_logs, user_favorites, user_ratings, users

## 上次启动时间
2026-04-01 00:01 — 所有服务正常（本地运行）
**前端端口**: 5173 (Vite dev server)
**后端端口**: 8080 (/api context-path)
**MinIO控制台**: 9001 (用户名/密码: minioadmin)
**MySQL服务**: 3306 (brew services)
**Redis服务**: 6379 (brew services)

## 本地开发环境 (2026-04-01)
已切换为本地运行模式（不再使用Docker）

### 一键启停脚本
- **start.sh** - 本地一键启动（MySQL/Redis/MinIO/Backend/Frontend），启动后自动显示各角色登录账号
- **stop.sh** - 停止后端/前端/MinIO（MySQL和Redis通过brew services管理保持运行）

### 服务架构
- **MySQL**: brew services 管理，端口 3306，库 brew-now
- **Redis**: brew services 管理，端口 6379
- **MinIO**: nohup 启动，数据目录 minio-data（非 .minio-data），端口 9000/9001
- **后端**: mvn spring-boot:run，日志 /tmp/backend.log
- **前端**: npm run dev，日志 /tmp/frontend.log

### 前端构建配置
vite.config.ts 使用函数式 manualChunks：
- element-plus 单独拆包
- 其余 node_modules 统一打入 vendor chunk
（解决 Vue/Element Plus 循环依赖初始化问题）

### 启动后显示的示例账号
- **消费者**: user001 / 123456 (张三)
- **商家**: BREW001 / 123456 (商家用户001)

### 注意事项
- 数据库表结构以 brew-now.sql 为准，init.sql 是旧版本不要用
- Docker 相关文件保留但不再使用

## 团队技术提升计划（2026-03-31）
已为团队制定完整的技术提升计划，包括以下文档：
1. **团队技术提升计划.md** - 总体规划和实施路线图
2. **后端开发最佳实践.md** - Spring Boot + Java开发规范
3. **前端开发最佳实践.md** - Vue 3 + TypeScript开发规范
4. **持续集成与自动化测试体系.md** - CI/CD流水线和测试策略
5. **团队技术成长路径与考核机制.md** - 职业发展和考核体系

## Git开发环境配置（2026-03-31）
已配置完整的Git开发环境，包括：
1. **Git_开发环境配置指南.md** - 完整的Git使用指南和最佳实践
2. **scripts/git-checks.sh** - 自动化Git规范检查脚本
3. **.git-hooks/pre-commit-template.sh** - 预提交钩子模板
4. **.gitignore** - 项目忽略文件配置

### Git配置要点：
- **别名配置**: st=status, co=checkout, br=branch, lg=log --oneline --graph
- **提交规范**: Conventional Commits (feat:, fix:, docs:, style:, etc.)
- **分支策略**: feature/, bugfix/, hotfix/, release/ 前缀规范
- **安全操作**: 优先使用 revert，谨慎使用 reset --hard

## 代码审查技能
已安装并配置代码审查技能：`code-review-quality`
- 优先级标准：🔴 Blocker → 🟡 Major → 🟢 Minor → 💡 Suggestion
- 审查流程：PR提交 → 自动检查 → 人工审查 → 问题修复 → 重新审查 → 合并
- 审查工具：SonarQube、Checkstyle、PMD、ESLint

## 代码质量目标
- 单元测试覆盖率：≥ 70%
- 集成测试覆盖率：≥ 50%
- 代码重复率：≤ 5%
- 技术债务指数：≤ 10%
- 编译构建时间：≤ 3分钟

## 团队发展路径
建立双轨制发展路径：
1. **技术专家路径**：初级开发 → 中级开发 → 高级开发 → 资深开发 → 技术专家 → 首席科学家
2. **管理路径**：初级开发 → 中级开发 → 技术主管 → 技术经理 → 技术总监 → CTO

## 培训体系
- 每周技术分享会（周四下午）
- 新员工培训计划（1-3个月）
- 导师制度（6个月周期）
- 月度/季度/年度技术大会
