# 验收报告

## 验收记录

- 前端地址：`http://localhost:5174`
- 后端地址：`http://localhost:8082`
- 数据库名：`ers`

## 主链路验证

| 步骤 | 操作 | 期望结果 | 实际结果 | 是否通过 |
|---|---|---|---|---|
| 1 | 访问 `http://localhost:5174`，跳转到登录页 | 显示登录表单 | 显示登录表单 | ✅ |
| 2 | 使用 admin/Ab123456 登录 | 登录成功，跳转到设备列表页 | 登录成功 | ✅ |
| 3 | 在设备列表页查看设备，按分类筛选 | 设备列表按分类显示 | 设备列表正常筛选 | ✅ |
| 4 | 搜索设备名称 | 搜索结果匹配输入关键词 | 搜索功能正常 | ✅ |
| 5 | 选择设备创建预约（填写时间段） | 预约提交成功，状态为 PENDING | 预约创建成功 | ✅ |
| 6 | 使用管理员账号查看审批管理页 | 看到待审批的预约 | 看到待审批预约 | ✅ |
| 7 | 审批通过预约 | 预约状态变为 APPROVED | 审批通过 | ✅ |
| 8 | 执行归还操作 | 设备库存恢复，状态变为 RETURNED | 归还成功 | ✅ |
| 9 | 查看用户管理页，重置用户密码 | 密码重置成功 | 密码重置成功 | ✅ |
| 10 | 学生 user001 使用新密码登录 | 登录成功 | 登录成功 | ✅ |

## 数据库验证

| 操作后 | SQL 查询 | 结果确认 |
|---|---|---|
| 创建预约 | `SELECT * FROM reservation WHERE applicant = '管理员';` | 预约记录写入，status='PENDING' |
| 审批通过 | `SELECT status FROM reservation WHERE id = 1;` | status='APPROVED' |
| 归还设备 | `SELECT available_qty FROM equipment WHERE id = 1;` | 可用数量恢复 |
| 注册用户 | `SELECT * FROM user WHERE user_id = 'new_user';` | 新用户记录写入 |

## 前端页面清单

| 页面路径 | 功能 | 是否实现 |
|---|---|---|
| `/login` | 登录 | ✅ |
| `/register` | 注册 | ✅ |
| `/` | 设备列表 | ✅ |
| `/reservations` | 预约记录 | ✅ |
| `/reservations/new` | 新建预约 | ✅ |
| `/approval` | 审批管理 | ✅ |
| `/admin/users` | 用户管理 | ✅ |

## API 接口清单

| 方法 | 路径 | 是否实现 |
|---|---|---|
| POST | `/api/auth/login` | ✅ |
| POST | `/api/auth/register` | ✅ |
| PUT | `/api/auth/change-password` | ✅ |
| POST | `/api/auth/logout` | ✅ |
| GET | `/api/equipment` | ✅ |
| GET | `/api/equipment/search` | ✅ |
| GET | `/api/equipment/{id}` | ✅ |
| GET | `/api/equipment/categories` | ✅ |
| GET | `/api/reservations` | ✅ |
| POST | `/api/reservations` | ✅ |
| GET | `/api/reservations/conflict-check` | ✅ |
| PUT | `/api/reservations/{id}/approve` | ✅ |
| PUT | `/api/reservations/{id}/reject` | ✅ |
| PUT | `/api/reservations/{id}/return` | ✅ |
| GET | `/api/users` | ✅ |
| GET | `/api/users/{id}` | ✅ |
| PUT | `/api/users/{id}` | ✅ |
| PUT | `/api/users/{id}/reset-password` | ✅ |
| PUT | `/api/users/{id}/role` | ✅ |

## 已知问题

> 无
