#!/bin/bash

# 沏刻茶叶电商平台 - 本地一键停止脚本

cd "$(dirname "$0")"

PID_DIR="/tmp/brewnow-pids"

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

stop_process() {
    local name="$1"
    local port="$2"
    local pid_file="$PID_DIR/$3.pid"

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null
            echo -e "${YELLOW}[STOP]${NC} $name (PID: $pid)"
            rm -f "$pid_file"
            return 0
        fi
        rm -f "$pid_file"
    fi

    # PID 文件不存在，按端口查找
    if [ -n "$port" ] && lsof -i :"$port" -sTCP:LISTEN > /dev/null 2>&1; then
        local pid=$(lsof -t -i :"$port" -sTCP:LISTEN 2>/dev/null)
        if [ -n "$pid" ]; then
            kill "$pid" 2>/dev/null
            echo -e "${YELLOW}[STOP]${NC} $name (PID: $pid)"
        fi
    fi
}

echo -e "${YELLOW}停止沏刻茶叶电商平台所有服务...${NC}"
echo ""

# 停止后端 (Spring Boot)
stop_process "后端服务" "8080" "backend"

# 停止前端 (Vite)
stop_process "前端服务" "5173" "frontend"

# 停止 MinIO
stop_process "MinIO" "9000" "minio"

# 等待进程退出
sleep 2

# 再次检查，强制杀掉残留进程
for port in 8080 5173 9000; do
    local_pid=$(lsof -t -i :"$port" -sTCP:LISTEN 2>/dev/null)
    if [ -n "$local_pid" ]; then
        kill -9 "$local_pid" 2>/dev/null
        echo -e "${RED}[KILL]${NC} 端口 $port 残留进程已强制终止"
    fi
done

# 清理 PID 目录
rm -rf "$PID_DIR"

echo ""
echo -e "${GREEN}所有服务已停止${NC}"
echo -e "${YELLOW}MySQL 和 Redis 保持运行（通过 brew services 管理）${NC}"
echo -e "如需停止: brew services stop mysql && brew services stop redis"
