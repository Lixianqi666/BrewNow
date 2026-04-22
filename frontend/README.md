# 沏刻茶叶电商平台 - 前端项目

## 项目概述

沏刻茶叶电商平台是一个现代化的茶叶电商系统，采用Vue3 + Vite技术栈构建，为用户、商家和管理员提供完整的电商解决方案。

## 技术栈

- **Vue 3.5.13** - 渐进式JavaScript框架
- **Vite 6.2.4** - 下一代前端构建工具  
- **TypeScript** - 类型安全的JavaScript超集
- **Element Plus 2.10.1** - Vue 3组件库
- **Vue Router 4.5.0** - 官方路由管理器
- **Pinia 3.0.1** - Vue官方状态管理库
- **Axios 1.9.0** - HTTP客户端库

## 开发指南

### 安装依赖
```sh
npm install
```

### 开发模式
```sh
npm run dev
```
访问: http://localhost:5173

### 构建生产版本
```sh
npm run build
```

### 代码检查
```sh
npm run lint
```

## API接口

- 后端服务: http://localhost:8080
- API前缀: /api
- 用户接口: /api/user/*
- 商品接口: /api/product/*
- 系统接口: /api/system/*

## 功能特性

- 🎯 三端分离设计（用户端、商家端、管理端）
- 🔧 JWT认证授权
- 🎨 Element Plus组件库
- 📱 响应式设计
- 🚀 TypeScript类型安全
- ⚡ Vite快速构建
