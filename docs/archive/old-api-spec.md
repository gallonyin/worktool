# API 规范文档

## API 概述

WorkTool提供了一套完整的API用于控制和管理企业微信的自动化操作。所有API都通过WebSocket进行调用，采用JSON格式进行数据传输。

## 通信协议

### WebSocket连接
```
ws://{host}/webserver/wework/{robotId}
```

### 消息格式
```json
{
    "messageId": "唯一消息ID",
    "type": "消息类型",
    "data": {
        // 具体参数
    }
}
```

## API 列表

### 1. 消息管理

#### 1.1 发送消息
```json
{
    "type": "SEND_MESSAGE",
    "titleList": ["接收人/群名称"],
    "receivedContent": "消息内容",
    "at": "要@的人名称",
    "atList": ["要@的多个人名称"]
}
```

#### 1.2 回复消息
```json
{
    "type": "REPLY_MESSAGE",
    "titleList": ["群名称"],
    "receivedName": "原消息发送者",
    "originalContent": "原始消息内容",
    "textType": "消息类型",
    "receivedContent": "回复内容"
}
```

#### 1.3 转发消息
```json
{
    "type": "RELAY_MESSAGE",
    "titleList": ["原始群名"],
    "nameList": ["目标接收者列表"],
    "receivedName": "原始发送者",
    "originalContent": "原始内容",
    "textType": "消息类型",
    "extraText": "附加说明"
}
```

### 2. 群管理

#### 2.1 创建/更新群聊
```json
{
    "type": "UPDATE_GROUP",
    "groupName": "群名称",
    "newGroupName": "新群名称",
    "newGroupAnnouncement": "群公告",
    "groupRemark": "群备注",
    "groupTemplate": "群模板",
    "selectList": ["要添加的成员"],
    "removeList": ["要移除的成员"],
    "showMessageHistory": true
}
```

#### 2.2 解散群聊
```json
{
    "type": "DISMISS_GROUP",
    "groupName": "要解散的群名称"
}
```

### 3. 联系人管理

#### 3.1 添加好友
```json
{
    "type": "ADD_FRIEND_BY_PHONE",
    "friend": {
        "phone": "手机号",
        "remark": "备注名",
        "message": "验证消息"
    }
}
```

#### 3.2 获取好友信息
```json
{
    "type": "GET_FRIEND_INFO",
    "selectList": ["好友名称列表"]
}
```

### 4. 文件操作

#### 4.1 发送文件
```json
{
    "type": "PUSH_FILE",
    "titleList": ["接收者列表"],
    "objectName": "文件名",
    "fileUrl": "文件URL",
    "fileType": "文件类型",
    "extraText": "附加说明"
}
```

## 返回值格式

### 成功响应
```json
{
    "messageId": "对应请求的消息ID",
    "success": true,
    "data": {
        // 返回数据
    }
}
```

### 错误响应
```json
{
    "messageId": "对应请求的消息ID",
    "success": false,
    "errorCode": "错误码",
    "errorReason": "错误原因"
}
```

## 错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 1001 | 参数错误 | 检查请求参数 |
| 1002 | 操作超时 | 重试操作 |
| 1003 | 权限不足 | 检查权限配置 |
| 2001 | 目标不存在 | 确认操作对象 |
| 2002 | 操作失败 | 查看详细错误信息 |

## 最佳实践

### 1. 错误处理
- 实现错误重试机制
- 做好错误日志记录
- 实现优雅降级策略

### 2. 性能优化
- 批量处理请求
- 合理控制并发
- 避免频繁请求

### 3. 安全建议
- 进行请求签名
- 实现请求频率限制
- 做好数据加密

## WebSocket心跳

为保持连接稳定，客户端需要定期发送心跳包：

```json
{
    "type": "HEARTBEAT",
    "timestamp": 1679215432000
}
```

服务端会回复：

```json
{
    "type": "HEARTBEAT_ACK",
    "timestamp": 1679215432000
}
```

建议的心跳间隔为30秒。

## 示例代码

### Kotlin示例
```kotlin
// 发送消息
val message = WeworkMessageBean().apply {
    type = "SEND_MESSAGE"
    titleList = listOf("测试群")
    receivedContent = "测试消息"
}
webSocketManager.send(message)
```

### JavaScript示例
```javascript
// 发送消息
const message = {
    type: "SEND_MESSAGE",
    titleList: ["测试群"],
    receivedContent: "测试消息"
};
ws.send(JSON.stringify(message));
```

## 版本历史

| 版本 | 更新内容 | 发布日期 |
|------|----------|----------|
| 2.8.1 | 优化执行队列去重算法 | 2023-11-19 |
| 2.8.0 | 兼容企微4.1.10 | 2023-10-18 |
| 2.7.4 | 优化消息发送 | 2023-10-15 |

后续版本更新请关注版本发布说明。