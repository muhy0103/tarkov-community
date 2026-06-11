# Backend

逃离塔科夫玩家情报社区管理系统后端。

## 技术栈

- Spring Boot 3.5.x
- Maven
- Java 17 编译目标
- Spring Web
- Validation
- Actuator
- Lombok
- Spring Mail
- MyBatis-Plus
- MySQL Driver

## 本机 JDK

当前本机命令行未配置全局 `java`，但已发现可用 JDK：

```text
C:\Users\muhy\.jdks\openjdk-25.0.1
```

后端项目使用 Java 17 作为编译目标，可以用本机更新版本 JDK 运行 Maven Wrapper。

## 开发命令

```bash
./mvnw spring-boot:run
./mvnw test
```

Windows PowerShell 下如果未配置全局 JDK，可以临时指定：

```powershell
$env:JAVA_HOME="C:\Users\muhy\.jdks\openjdk-25.0.1"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd test
```

## 当前接口

```text
GET /api/health
POST /api/auth/register
POST /api/auth/login
GET /api/auth/verify-email?token=...
```

健康检查用于确认后端服务已经启动，认证接口负责注册、登录和邮箱确认。

## 数据库环境变量

后端默认读取本机 MySQL 的 `tarkov_community` 数据库。

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/tarkov_community?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的 MySQL 密码"
```

如果暂时没有配置密码，项目仍可启动，但真正访问数据库接口时需要正确的 MySQL 账号。

## 邮箱验证环境变量

注册接口会创建待验证账号，并向用户邮箱发送确认链接。邮件授权码只建议通过环境变量传入，不要写入配置文件或提交到仓库。

```powershell
$env:MAIL_HOST="smtp.sina.com"
$env:MAIL_PORT="465"
$env:MAIL_USERNAME="nzdyhbzx@sina.com"
$env:MAIL_PASSWORD="你的邮箱授权码"
$env:MAIL_FROM="nzdyhbzx@sina.com"
$env:APP_FRONTEND_URL="http://127.0.0.1:5173"
```

开发阶段如暂时不发送真实邮件，可以关闭发送并使用接口返回的本地验证链接：

```powershell
$env:MAIL_ENABLED="false"
$env:MAIL_DEV_LINK_ENABLED="true"
```
