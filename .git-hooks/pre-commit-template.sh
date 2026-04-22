#!/bin/bash

# 预提交钩子模板
# 放在 .git/hooks/pre-commit 使用
# 或通过 git config core.hooksPath .git-hooks 配置

echo "🚀 Git 预提交检查启动..."
echo "==========================="

# 1. 检查提交信息是否为空
COMMIT_MSG_FILE=$1
COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")

if [[ -z "$COMMIT_MSG" || "$COMMIT_MSG" =~ ^[[:space:]]*$ ]]; then
    echo "❌ 提交信息不能为空！"
    exit 1
fi

# 2. 检查提交信息是否符合规范
if [[ ! "$COMMIT_MSG" =~ ^(feat:|fix:|docs:|style:|refactor:|perf:|test:|chore:|build:|ci:)(\ .+) ]]; then
    echo "❌ 提交信息不符合规范！"
    echo "   应遵循 Conventional Commits 格式，例如:"
    echo "   feat: 添加新功能"
    echo "   fix: 修复bug"
    echo "   docs: 更新文档"
    echo "   style: 代码格式调整"
    echo "   refactor: 重构代码"
    echo "   perf: 性能优化"
    echo "   test: 测试相关"
    echo "   chore: 构建/工具更新"
    echo "   build: 构建系统"
    echo "   ci: CI配置"
    exit 1
fi

# 3. 检查提交信息长度
if [[ ${#COMMIT_MSG} -lt 10 ]]; then
    echo "⚠️  提交信息可能过于简单，建议提供更详细的描述"
fi

# 4. 运行代码检查（如果有配置）
if [[ -f "package.json" ]] && command -v npm &> /dev/null; then
    echo "📦 运行前端代码检查..."
    # 检查是否有 lint 脚本
    if grep -q "\"lint\"" package.json; then
        if npm run lint 2>&1 | grep -E "(error|ERROR|fail|FAIL)"; then
            echo "❌ 前端代码检查失败，请先修复"
            exit 1
        fi
    fi
fi

# 5. 运行测试（如果有配置）
if [[ -f "pom.xml" ]] && command -v mvn &> /dev/null; then
    echo "☕ 运行后端测试..."
    # 快速测试，不打包
    if mvn test -DskipTests=false 2>&1 | grep -E "(BUILD FAILURE|Tests failed)"; then
        echo "❌ 后端测试失败，请先修复"
        exit 1
    fi
fi

# 6. 检查是否有调试代码
echo "🔍 检查调试代码..."
FILES_TO_COMMIT=$(git diff --cached --name-only)

for file in $FILES_TO_COMMIT; do
    # 检查常见调试代码
    if [[ -f "$file" ]]; then
        # Java 调试代码
        if [[ "$file" =~ \.java$ ]] && grep -q "System\.out\.print\|System\.err\.print\|TODO\|FIXME\|HACK" "$file"; then
            echo "⚠️  文件 $file 中可能包含调试代码或TODO"
        fi
        
        # JavaScript/TypeScript 调试代码
        if [[ "$file" =~ \.(js|ts|tsx)$ ]] && grep -q "console\.log\|debugger\|alert\|TODO\|FIXME" "$file"; then
            echo "⚠️  文件 $file 中可能包含调试代码或TODO"
        fi
        
        # Python 调试代码
        if [[ "$file" =~ \.py$ ]] && grep -q "print(\|TODO\|FIXME" "$file"; then
            echo "⚠️  文件 $file 中可能包含调试代码或TODO"
        fi
    fi
done

# 7. 检查文件编码
echo "🔠 检查文件编码..."
for file in $FILES_TO_COMMIT; do
    if [[ -f "$file" ]]; then
        # 检查是否包含非UTF-8编码
        if file "$file" | grep -q "Non-ISO"; then
            echo "⚠️  文件 $file 可能不是UTF-8编码"
        fi
    fi
done

echo ""
echo "✅ 所有预提交检查通过！"
echo "📝 提交信息: $COMMIT_MSG"
echo ""
echo "💡 提交即将完成..."
exit 0