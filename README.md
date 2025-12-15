# DiaryWeb —— 日记 · 树洞 · 互动系统（高级程序设计课程大作业）

## 1. 项目简介

DiaryWeb 是一个基于 Java Spring Boot 的多用户日记与树洞互动系统，支持用户注册登录、日记管理、树洞问答、互动点赞评论、日记交换以及经验值与成就系统。本项目作为《高级程序设计》课程大作业，重点体现后端系统设计能力、工程规范以及可扩展性。

项目采用前后端分离思想，后端提供 RESTful API，使用 JWT 进行用户认证，支持分页、排序、统一返回格式和全局异常处理。

---

## 2. 技术栈

- **语言**：Java 17  
- **后端框架**：Spring Boot 3.5.x  
- **安全框架**：Spring Security 6 + JWT  
- **ORM**：Spring Data JPA（Hibernate）  
- **数据库**：MySQL 8.x  
- **构建工具**：Maven  
- **接口测试**：Postman  

---

## 3. 系统功能模块

### 3.1 用户与认证
- 用户注册 / 登录
- JWT 无状态认证
- 密码 BCrypt 加密
- 多用户并发使用

### 3.2 日记管理
- 创建 / 修改 / 删除日记
- 支持文字与图片
- 日记可见性控制：
  - PUBLIC（公开）
  - PRIVATE（私密）
  - SEMI_PRIVATE（交换）
- 标签系统（多标签）
- 分页 / 排序 / 搜索

### 3.3 日记互动
- 点赞 / 取消点赞
- 评论（支持匿名）
- 点赞去重（用户对同一日记只能点赞一次）

### 3.4 日记交换
- 半私密日记交换
- 交换状态机：
  - PENDING
  - ACCEPTED
  - REJECTED
- 查看交换历史

### 3.5 树洞问答
- 匿名提问
- 匿名回答
- 问题与回答关联展示

### 3.6 经验值与成就系统
- 用户行为获得经验值：
  - 写日记
  - 回答问题
- 自动升级等级
- 成就系统（经验阈值解锁）
- 查看用户成就列表

---

## 4. 系统架构设计

### 4.1 分层结构
controller —— 接口层（REST API）
service —— 业务逻辑层
repository —— 数据访问层（JPA）
entity —— 数据库实体
dto —— 数据传输对象（对外返回）
config —— 安全与系统配置
common —— 通用返回与异常

---


### 4.2 DTO 分层设计
- Controller 层只返回 DTO
- 避免 Entity 直接序列化
- 解决 JSON 循环引用
- 防止敏感字段（如 password）泄露

---

## 5. 数据库设计（核心表）

- users：用户信息
- diaries：日记内容与可见性
- tags / diary_tags：标签多对多关系
- comments：评论
- likes：点赞（唯一约束）
- diary_exchange：日记交换记录
- questions / answers：树洞问答
- user_experience：用户经验值与等级
- achievements / user_achievements：成就定义与解锁记录

---

## 6. 数据库初始化

### 6.1 创建数据库

```sql
CREATE DATABASE diary_management
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;
```

### 6.2 application.properties 示例
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/diary_management
spring.datasource.username=root
spring.datasource.password=你的数据库密码

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
app.jwt.secret=ChangeThisToAProperLongRandomSecretKey_AtLeast_32_Chars
app.jwt.expiration-ms=86400000
```

## 7. 接口认证说明（JWT）

### 7.1 登录获取 Token
POST /api/auth/login

---

### 7.2 返回示例：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```
### 7.3 后续请求在 Header 中携带：
```makefile
Authorization: Bearer <token>
```

## 8. 主要接口示例

### 8.1 认证
- POST /api/auth/register
- POST /api/auth/login

---

### 8.2 日记
- POST /api/diaries/create
- GET /api/diaries/mine
- GET /api/diaries/mine/page?page=0&size=10
- GET /api/diaries/public/search/page?keyword=xxx
