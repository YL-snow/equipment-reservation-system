# equipment-reservation-system 后续待办事项

---

## 1. 前端构建验证

```bash
cd E:\project\equipment-reservation-system\frontend
pnpm install
pnpm run build
```

如果构建成功，`frontend/dist/` 会生成打包文件。

---

## 2. 本地启动运行（需要 MySQL）

```bash
# ① 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ers DEFAULT CHARACTER SET utf8mb4;"

# ② 导入表结构和种子数据
mysql -u root -p ers < database\schema.sql
mysql -u root -p ers < database\seed.sql

# ③ 启动后端
cd backend
mvn spring-boot:run

# ④ 新终端启动前端
cd frontend
pnpm install
pnpm run dev
```

默认账号：admin / password（管理员），user001~005 / 123456Ab（学生）

---

## 3. 推送修改到 GitHub

```bash
cd E:\project\equipment-reservation-system
git add -A
git commit -m "chore: 完成代码验证"
git push
```

---

## 4. 最终检查清单

- [ ] 后端编译无报错（`mvn compile`）
- [ ] 前端构建成功（`pnpm run build`）
- [ ] 数据库初始化成功
- [ ] 后端启动正常
- [ ] 前端页面正常访问
- [ ] 默认账号可正常登录
- [ ] 预约创建/审批/归还流程正常
- [ ] GitHub 仓库内容为最新
