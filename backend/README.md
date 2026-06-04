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
```

用于确认后端服务已经启动。
