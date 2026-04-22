#!/bin/bash

# 沏刻茶叶电商平台 - 本地一键启动脚本

cd "$(dirname "$0")"
ROOT_DIR="$(pwd)"
FRONTEND_DIST="$ROOT_DIR/frontend/dist"
NGINX_SITE_PATH="/opt/homebrew/etc/nginx/servers/brew-now.conf"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

# 存储进程 PID
PID_DIR="/tmp/brewnow-pids"
mkdir -p "$PID_DIR"

# ==========================================
# 1. 检查并启动 MySQL
# ==========================================
start_mysql() {
    log_info "检查 MySQL..."
    if mysqladmin ping -uroot -p200306 --silent 2>/dev/null; then
        log_success "MySQL 已运行"
    else
        log_info "启动 MySQL..."
        brew services start mysql
        for i in $(seq 1 15); do
            if mysqladmin ping -uroot -p200306 --silent 2>/dev/null; then
                log_success "MySQL 启动成功"
                return 0
            fi
            sleep 2
        done
        log_error "MySQL 启动超时，请检查配置"
        exit 1
    fi
}

# ==========================================
# 2. 检查并启动 Redis
# ==========================================
start_redis() {
    log_info "检查 Redis..."
    if redis-cli ping 2>/dev/null | grep -q PONG; then
        log_success "Redis 已运行"
    else
        log_info "启动 Redis..."
        brew services start redis
        for i in $(seq 1 10); do
            if redis-cli ping 2>/dev/null | grep -q PONG; then
                log_success "Redis 启动成功"
                return 0
            fi
            sleep 1
        done
        log_error "Redis 启动超时，请检查配置"
        exit 1
    fi
}

# ==========================================
# 3. 检查并启动 MinIO
# ==========================================
start_minio() {
    log_info "检查 MinIO..."
    if lsof -i :9000 -sTCP:LISTEN > /dev/null 2>&1; then
        log_success "MinIO 已运行"
    else
        log_info "启动 MinIO..."
        MINIO_DATA="$(pwd)/minio-data"
        mkdir -p "$MINIO_DATA"
        nohup minio server "$MINIO_DATA" --address :9000 --console-address :9001 > /tmp/minio.log 2>&1 &
        echo $! > "$PID_DIR/minio.pid"

        for i in $(seq 1 10); do
            if lsof -i :9000 -sTCP:LISTEN > /dev/null 2>&1; then
                log_success "MinIO 启动成功"
                # 自动创建 bucket
                sleep 2
                if command -v mc &> /dev/null; then
                    mc alias set local http://127.0.0.1:9000 minioadmin minioadmin > /dev/null 2>&1
                    mc mb local/brew-now --ignore-existing > /dev/null 2>&1
                    mc anonymous set download local/brew-now > /dev/null 2>&1
                    log_info "MinIO bucket 已就绪"
                fi
                return 0
            fi
            sleep 1
        done
        log_error "MinIO 启动超时，请检查 /tmp/minio.log"
        exit 1
    fi
}

# ==========================================
# 4. 构建并启动后端
# ==========================================
start_backend() {
    log_info "检查后端服务..."
    if lsof -i :8080 -sTCP:LISTEN > /dev/null 2>&1; then
        log_warn "端口 8080 已被占用，跳过后端启动"
        return 0
    fi

    log_info "编译后端项目..."
    cd backend
    if ! mvn spring-boot:run -DskipTests > /tmp/backend.log 2>&1 &
    then
        log_error "后端编译失败，请检查 /tmp/backend.log"
        cd ..
        exit 1
    fi
    cd ..
    echo $! > "$PID_DIR/backend.pid"

    log_info "等待后端服务启动（约 30 秒）..."
    for i in $(seq 1 30); do
        if curl -s http://localhost:8080/api/system/health > /dev/null 2>&1; then
            log_success "后端服务启动成功"
            return 0
        fi
        sleep 2
    done
    log_warn "后端健康检查超时，可能仍在启动中，请查看 /tmp/backend.log"
}

# ==========================================
# 5. 启动前端
# ==========================================
start_frontend() {
    log_info "检查前端服务..."
    if lsof -i :5173 -sTCP:LISTEN > /dev/null 2>&1; then
        log_warn "端口 5173 已被占用，跳过前端启动"
        return 0
    fi

    log_info "启动前端开发服务器..."
    cd frontend
    if [ ! -d "node_modules" ]; then
        log_info "安装前端依赖..."
        npm install
    fi
    nohup npm run dev > /tmp/frontend.log 2>&1 &
    cd ..
    echo $! > "$PID_DIR/frontend.pid"

    for i in $(seq 1 15); do
        if lsof -i :5173 -sTCP:LISTEN > /dev/null 2>&1; then
            log_success "前端服务启动成功"
            return 0
        fi
        sleep 2
    done
    log_warn "前端启动超时，请查看 /tmp/frontend.log"
}

# ==========================================
# 6. 检查 Nginx 反向代理
# ==========================================
check_nginx() {
    log_info "检查 Nginx 反向代理..."

    if ! command -v nginx > /dev/null 2>&1; then
        log_warn "未检测到 nginx，可继续使用前端开发地址 http://localhost:5173"
        return 0
    fi

    if ! lsof -i :80 -sTCP:LISTEN > /dev/null 2>&1; then
        log_warn "nginx 未监听 80 端口，可继续使用前端开发地址 http://localhost:5173"
        return 0
    fi

    if curl -s http://127.0.0.1/healthz > /dev/null 2>&1; then
        log_success "Nginx 反向代理已就绪"
    else
        log_warn "nginx 正在运行，但 /healthz 未返回成功，请检查站点配置"
    fi

    if [ -f "$NGINX_SITE_PATH" ]; then
        log_info "当前 nginx 站点配置: $NGINX_SITE_PATH"
    fi

    if [ -d "$FRONTEND_DIST" ]; then
        log_info "前端构建产物已存在: $FRONTEND_DIST"
    else
        log_warn "未找到前端构建产物目录，nginx 静态站点可能无法提供最新页面"
        log_warn "可执行: cd frontend && npm run build"
    fi
}

# ==========================================
# 显示状态
# ==========================================
show_status() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}     沏刻茶叶电商平台 - 服务状态        ${NC}"
    echo -e "${GREEN}========================================${NC}"

    # MySQL
    if mysqladmin ping -uroot -p200306 --silent 2>/dev/null; then
        echo -e "  MySQL      ${GREEN}● 运行中${NC}  localhost:3306"
    else
        echo -e "  MySQL      ${RED}○ 未运行${NC}"
    fi

    # Redis
    if redis-cli ping 2>/dev/null | grep -q PONG; then
        echo -e "  Redis      ${GREEN}● 运行中${NC}  localhost:6379"
    else
        echo -e "  Redis      ${RED}○ 未运行${NC}"
    fi

    # MinIO
    if lsof -i :9000 -sTCP:LISTEN > /dev/null 2>&1; then
        echo -e "  MinIO      ${GREEN}● 运行中${NC}  localhost:9000 / 控制台:9001"
    else
        echo -e "  MinIO      ${RED}○ 未运行${NC}"
    fi

    # Backend
    if lsof -i :8080 -sTCP:LISTEN > /dev/null 2>&1; then
        echo -e "  Backend    ${GREEN}● 运行中${NC}  http://localhost:8080/api"
    else
        echo -e "  Backend    ${RED}○ 未运行${NC}"
    fi

    # Frontend
    if lsof -i :5173 -sTCP:LISTEN > /dev/null 2>&1; then
        echo -e "  Frontend   ${GREEN}● 运行中${NC}  http://localhost:5173"
    else
        echo -e "  Frontend   ${RED}○ 未运行${NC}"
    fi

    # Nginx
    if lsof -i :80 -sTCP:LISTEN > /dev/null 2>&1 && curl -s http://127.0.0.1/healthz > /dev/null 2>&1; then
        echo -e "  Nginx      ${GREEN}● 运行中${NC}  http://localhost"
    elif lsof -i :80 -sTCP:LISTEN > /dev/null 2>&1; then
        echo -e "  Nginx      ${YELLOW}◐ 监听中${NC}  80 端口已开启，但 /healthz 未通过"
    else
        echo -e "  Nginx      ${RED}○ 未运行${NC}"
    fi

    echo ""
    echo -e "${BLUE}日志文件:${NC}"
    echo "  后端:  tail -f /tmp/backend.log"
    echo "  前端:  tail -f /tmp/frontend.log"
    echo "  MinIO: tail -f /tmp/minio.log"
    echo "  Nginx: nginx -T"
    echo ""
}

# ==========================================
# 显示登录信息
# ==========================================
show_login_info() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}        登录账号和密码                ${NC}"
    echo -e "${GREEN}========================================${NC}"

    if ! mysqladmin ping -uroot -p200306 --silent 2>/dev/null; then
        echo -e "${RED}无法连接数据库，无法获取登录信息${NC}"
        return
    fi

    mysql -uroot -p200306 brew-now -N -t -e "
SELECT 
    role AS 角色,
    account AS 账号,
    username AS 用户名,
    '123456' AS 密码
FROM users 
WHERE user_id IN (SELECT MIN(user_id) FROM users WHERE deleted_at IS NULL GROUP BY role)
" 2>/dev/null

    echo ""
    echo -e "${YELLOW}提示: 以上为各角色的示例账号，密码均为 123456${NC}"
    echo ""
}

# ==========================================
# 主函数
# ==========================================
main() {
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  沏刻茶叶电商平台 - 本地环境启动       ${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo -e "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""

    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        echo "用法: ./start.sh [选项]"
        echo "选项:"
        echo "  -h, --help    显示帮助"
        echo "  --status      仅显示服务状态"
        echo "  --skip-build  跳过后端编译，直接启动"
        exit 0
    fi

    if [ "$1" = "--status" ]; then
        show_status
        exit 0
    fi

    start_mysql
    start_redis
    start_minio
    start_backend
    start_frontend
    check_nginx

    echo ""
    show_status
    show_login_info
    echo -e "${GREEN}所有服务已启动完成！${NC}"
    if lsof -i :80 -sTCP:LISTEN > /dev/null 2>&1 && curl -s http://127.0.0.1/healthz > /dev/null 2>&1; then
        echo -e "${GREEN}访问 http://localhost 使用系统 ${NC}"
    else
        echo -e "${GREEN}请访问 http://localhost:5173 使用系统${NC}"
    fi
}

main "$@"
