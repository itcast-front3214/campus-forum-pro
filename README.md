# Campus Forum 校园论坛后端

面向校园交流的Java后端学习项目，提供注册登录、发帖检索、评论回复、点赞、内容审核及举报处理接口。仓库包含后端源码，不包含前端。

## 技术栈

Java 17、Spring Boot 3.2.3、Spring Security、JWT、BCrypt、MyBatis-Plus 3.5.6、MySQL 8、Redis 7、Maven、SpringDoc OpenAPI / Swagger UI、Docker Compose。

## 功能

- 用户注册登录、个人资料、账号封禁。
- 帖子分页检索、评论回复、点赞和热门帖子列表。
- 待审核、审核中、审核完成的内容审核流程，以及举报处理。
- JWT认证结合数据库当前角色与账号状态校验，帖子操作权限检查。
- MyBatis参数绑定与排序字段白名单；Redis热榜缓存、浏览量IP时间窗口去重。
- Controller / Service / Mapper分层，统一异常处理、响应封装与参数校验。

## 本地启动

准备JDK 17、Maven 3.8+和Docker Compose，在项目根目录执行。

1. 复制 `.env.example` 为 `.env`，自行设置 `DB_PASSWORD`。
2. 执行 `docker compose up -d` 启动MySQL与Redis，使用 `docker compose ps` 查看状态。
3. 在运行后端的PowerShell中设置环境变量（Maven不会自动读取 `.env`）：

```powershell
$env:DB_PASSWORD = '替换为.env中的密码'
$keyBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($keyBytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($keyBytes)
mvn spring-boot:run
```

通过IDEA启动时，在运行配置设置同样的环境变量。JWT密钥为至少32字节随机数据的Base64编码，更换密钥会使旧Token失效。

4. 打开 `http://localhost:8080/swagger-ui.html`，注册测试账号、登录并使用Token调用需要授权的接口。

### 管理员设置

不提供默认管理员密码。注册自己的测试账号后，使用 `docker compose exec mysql mysql -u root -p campus_forum` 进入本地数据库，执行：

```sql
UPDATE user SET role = 'ADMIN' WHERE username = '替换为自己的测试用户名';
```

然后重新登录。初始化SQL仅在全新数据卷首次启动时执行，不要为普通配置问题删除已有数据卷。

## 配置

`DB_PASSWORD`与`JWT_SECRET`必填；可通过`DB_URL`、`DB_USERNAME`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`、`CORS_ALLOWED_ORIGINS`覆盖本地默认配置。默认数据库在localhost:3307，Redis在localhost:6379，允许的前端来源为http://localhost:5173。

## 接口分组

- `/api/users`：用户模块。
- `/api/posts`：帖子模块。
- `/api/posts/{postId}/comments`：评论模块。
- `/api/moderation/posts`：内容审核。
- `/api/reports`：举报模块。

具体请求方式、参数和权限以源码与Swagger文档为准。

## 验证与边界

这是本地学习和调试项目，未进行性能压测、生产部署或全面安全审计。当前源码无自动化业务测试，编译通过不等于功能与安全测试通过。

项目没有Token自动续签功能；热榜使用TTL缓存，更新存在延迟；浏览量去重是查询后写入方式，不保证并发下的严格一次计数。没有对性能收益作量化承诺。

Docker Compose仅提供本机开发环境。生产部署需要另外配置最小权限账号、HTTPS、监控、备份与密钥管理。不要提交`.env`、真实账号、个人数据库导出或IDE配置。
