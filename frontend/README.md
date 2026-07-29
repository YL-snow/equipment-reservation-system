# 前端项目

## 说明

前端基于 **React ^19.2** + **TypeScript** + **Vite ^8.1**，使用 **Ant Design ^6.5** 作为 UI 组件库。

## 启动方式

```bash
cd frontend
npm install
npm run dev
```

默认启动在 `http://localhost:5173`

## 构建

```bash
npm run build
```

## 页面清单

| 页面路径 | 功能说明 | 访问权限 |
|---|---|---|
| `/login` | 登录页 | 公开 |
| `/register` | 注册页 | 公开 |
| `/` | 设备列表页（搜索、分类筛选、设备卡片展示） | 登录 |
| `/reservations` | 预约记录列表（按状态筛选、查看详情） | 登录 |
| `/reservations/new` | 新建预约（选择设备、时间段、冲突检测） | 登录 |
| `/approval` | 审批管理（审批/驳回/归还设备） | 管理员 |
| `/admin/users` | 用户管理（查看用户、重置密码、修改角色） | 管理员 |

## 接口请求

所有 API 请求通过 axios 实例发送，默认通过 Vite proxy 代理到 `http://localhost:8080`。请求自动携带 JWT Token。

## 项目结构

```
frontend/src/
├── api/
│   └── api.ts               # axios 实例配置
├── components/
│   └── AuthGuard.tsx         # 路由权限守卫
├── context/
│   └── AuthContext.tsx       # 认证上下文
├── pages/
│   ├── Login/index.tsx       # 登录
│   ├── Register/index.tsx    # 注册
│   ├── EquipmentList/index.tsx       # 设备列表
│   ├── ReservationCreate/index.tsx   # 新建预约
│   ├── ReservationList/index.tsx     # 预约记录
│   ├── Approval/index.tsx            # 审批管理
│   └── UserManagement/index.tsx      # 用户管理
├── types/
│   └── index.ts              # 类型定义
├── App.tsx                   # 路由配置
└── main.tsx                  # 入口
```
