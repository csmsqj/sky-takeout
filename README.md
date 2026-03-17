# 苍穹外卖 (Sky Takeout)

## 项目简介

苍穹外卖是一个基于 Spring Boot 的外卖管理系统，包含管理端和用户端功能。

## 快速开始

### 环境要求

- JDK 21+
- MySQL 8.0+
- Maven 3.9+
- Redis（可选）

### 配置步骤

#### 1. 配置数据库

创建数据库 `sky_take_out` 并导入 SQL 脚本（如果有）。

#### 2. 配置环境变量

**重要**：本项目使用阿里云 OSS 进行文件存储，需要配置环境变量。

**方法一：在 IDEA 中配置（推荐用于开发）**

1. 打开 Run/Debug Configurations
2. 在 Environment variables 中添加：
   ```
   OSS_ACCESS_KEY_ID=你的AccessKeyId;OSS_ACCESS_KEY_SECRET=你的AccessKeySecret
   ```

**方法二：使用 .env 文件**

1. 复制 `.env.example` 为 `.env`
2. 填入真实的阿里云 OSS 密钥
3. 安装 IDEA EnvFile 插件并配置

**详细配置说明请参考：[环境变量配置指南](docs/环境变量配置指南.md)**

#### 3. 启动项目

```bash
# 使用 Maven
mvn clean install
cd sky-server
mvn spring-boot:run

# 或者运行主类
com.sky.SkyApplication
```

启动成功后访问：http://localhost:8080

### API 文档

项目集成了 Knife4j，启动后访问：http://localhost:8080/doc.html

## 常见问题

### 1. 文件上传报错：AccessDenied

这是因为环境变量未配置或阿里云 OSS 权限不足。请参考 [环境变量配置指南](docs/环境变量配置指南.md) 进行配置。

### 2. 数据库连接失败

检查 `application-dev.yml` 中的数据库配置，可以通过环境变量 `DB_USERNAME` 和 `DB_PASSWORD` 自定义用户名和密码。

### 3. Redis 连接失败

如果不需要 Redis，可以在配置文件中禁用相关功能。

## 项目结构

```
sky-takeout/
├── sky-common/          # 公共模块
├── sky-pojo/            # 实体类模块
├── sky-server/          # 服务端模块
├── docs/                # 文档目录
│   └── 环境变量配置指南.md
├── .env.example         # 环境变量示例文件
└── pom.xml              # Maven 主配置
```

## 技术栈

- Spring Boot 2.7.3
- MyBatis
- MySQL 8.0
- Redis
- Aliyun OSS
- Knife4j (API 文档)
- JWT (身份认证)

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

请查看 LICENSE 文件。
