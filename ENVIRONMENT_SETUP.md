# 环境变量配置指南 (Environment Variables Setup Guide)

## 概述 (Overview)

本项目需要配置以下环境变量才能正常运行：

- `DB_USERNAME`: 数据库用户名（可选，默认: root）
- `DB_PASSWORD`: 数据库密码（可选，默认: 123456）
- `OSS_ACCESS_KEY_ID`: 阿里云OSS访问密钥ID（必需）
- `OSS_ACCESS_KEY_SECRET`: 阿里云OSS访问密钥Secret（必需）

## 问题说明 (Problem Description)

如果您遇到以下错误：

```
com.aliyun.oss.OSSException: You have no right to access this object because of bucket acl.
[ErrorCode]: AccessDenied
```

这通常意味着：
1. 环境变量设置不正确
2. 阿里云OSS凭证无效或缺失
3. OSS Bucket的ACL权限配置不正确

## Windows 系统配置方法

### 方法1: PowerShell (推荐)

在 PowerShell 中，使用 `$env:` 语法：

```powershell
# 设置数据库密码
$env:DB_PASSWORD = "123456"

# 设置阿里云OSS凭证
$env:OSS_ACCESS_KEY_ID = "你的AccessKeyID"
$env:OSS_ACCESS_KEY_SECRET = "你的AccessKeySecret"

# 验证设置是否成功
echo $env:OSS_ACCESS_KEY_ID
echo $env:OSS_ACCESS_KEY_SECRET
```

**注意**: 这种方式只在当前 PowerShell 会话中有效。关闭窗口后需要重新设置。

### 方法2: 命令提示符 (CMD)

在 CMD 中，使用 `set` 命令：

```cmd
set DB_PASSWORD=123456
set OSS_ACCESS_KEY_ID=你的AccessKeyID
set OSS_ACCESS_KEY_SECRET=你的AccessKeySecret

# 验证设置
echo %OSS_ACCESS_KEY_ID%
echo %OSS_ACCESS_KEY_SECRET%
```

**注意**: 这种方式只在当前 CMD 会话中有效。

### 方法3: 系统环境变量 (永久有效)

1. 右键点击"此电脑"或"我的电脑" → 属性
2. 点击"高级系统设置"
3. 点击"环境变量"
4. 在"用户变量"或"系统变量"中点击"新建"
5. 添加以下变量：
   - 变量名: `OSS_ACCESS_KEY_ID`，变量值: `你的AccessKeyID`
   - 变量名: `OSS_ACCESS_KEY_SECRET`，变量值: `你的AccessKeySecret`
   - 变量名: `DB_PASSWORD`，变量值: `123456`
6. 点击"确定"保存
7. **重启IDE或命令行窗口**使环境变量生效

### 方法4: 在IDE中配置 (IntelliJ IDEA / Eclipse)

#### IntelliJ IDEA:

1. 打开 Run → Edit Configurations
2. 选择您的运行配置 (通常是 SkyApplication)
3. 找到 "Environment variables" 字段
4. 点击文件夹图标，添加环境变量：
   ```
   DB_PASSWORD=123456
   OSS_ACCESS_KEY_ID=你的AccessKeyID
   OSS_ACCESS_KEY_SECRET=你的AccessKeySecret
   ```
5. 点击 OK 保存

## Linux / macOS 系统配置方法

### 方法1: 临时设置 (当前终端会话)

```bash
export DB_PASSWORD="123456"
export OSS_ACCESS_KEY_ID="你的AccessKeyID"
export OSS_ACCESS_KEY_SECRET="你的AccessKeySecret"

# 验证设置
echo $OSS_ACCESS_KEY_ID
echo $OSS_ACCESS_KEY_SECRET
```

### 方法2: 永久设置

编辑 `~/.bashrc` (Bash) 或 `~/.zshrc` (Zsh):

```bash
# 使用文本编辑器打开
nano ~/.bashrc  # 或 nano ~/.zshrc

# 在文件末尾添加
export DB_PASSWORD="123456"
export OSS_ACCESS_KEY_ID="你的AccessKeyID"
export OSS_ACCESS_KEY_SECRET="你的AccessKeySecret"

# 保存并退出，然后重新加载配置
source ~/.bashrc  # 或 source ~/.zshrc
```

## Docker 环境配置

如果您使用 Docker 运行项目，可以通过以下方式配置：

### docker run 命令:

```bash
docker run -e DB_PASSWORD=123456 \
           -e OSS_ACCESS_KEY_ID=你的AccessKeyID \
           -e OSS_ACCESS_KEY_SECRET=你的AccessKeySecret \
           your-image-name
```

### docker-compose.yml:

```yaml
version: '3'
services:
  sky-takeout:
    image: your-image-name
    environment:
      - DB_PASSWORD=123456
      - OSS_ACCESS_KEY_ID=你的AccessKeyID
      - OSS_ACCESS_KEY_SECRET=你的AccessKeySecret
```

## 配置文件方式 (application-dev.yml)

您也可以直接在配置文件中设置（不推荐用于生产环境）：

编辑 `sky-server/src/main/resources/application-dev.yml`:

```yaml
sky:
  datasource:
    username: root
    password: 123456  # 直接设置，不使用环境变量
  alioss:
    access-key: 你的AccessKeyID  # 直接设置
    secret-key: 你的AccessKeySecret  # 直接设置
```

**警告**: 不要将真实的密钥提交到Git仓库！建议使用环境变量方式。

## 获取阿里云OSS凭证

1. 登录阿里云控制台: https://www.aliyun.com
2. 进入"访问控制 (RAM)" → "用户" → "创建用户"
3. 勾选"OpenAPI调用访问"
4. 保存生成的 AccessKey ID 和 AccessKey Secret
5. 为用户授予 OSS 相关权限

## 常见问题排查

### 1. 环境变量没有生效

- Windows: 确保重启了 IDE 或命令行窗口
- 检查变量名是否拼写正确（大小写敏感）
- 验证变量值是否正确设置（没有多余的空格或引号）

### 2. 仍然报 AccessDenied 错误

- 确认 OSS Bucket 存在且名称正确
- 检查 Bucket 的 ACL 权限设置
- 验证 AccessKey 是否有效且具有 OSS 操作权限
- 确认 endpoint 和 region 配置正确

### 3. 数据库连接失败

- 检查 MySQL 是否已启动
- 验证用户名和密码是否正确
- 确认数据库 `sky_take_out` 已创建

## 验证配置

启动应用后，查看日志中是否有以下内容：

```
INFO com.sky.SkyApplication : Started SkyApplication in X.XXX seconds
INFO com.sky.SkyApplication : server started
```

如果看到以上信息，说明应用启动成功。您可以尝试上传文件来验证 OSS 配置是否正确。

## 安全建议

1. ✅ 使用环境变量存储敏感信息
2. ✅ 不要将密钥提交到版本控制系统
3. ✅ 定期轮换 AccessKey
4. ✅ 为不同环境使用不同的凭证
5. ✅ 使用 RAM 用户而不是主账号 AccessKey
6. ✅ 遵循最小权限原则，只授予必要的权限
