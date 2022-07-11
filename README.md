## WorkTool

本安卓应用是一个依附于企业微信来运行的无人值守群管理机器人，非hook侵入式函数修改，使用安卓系统原生支持的无障碍服务能力，手机无需Root，应用兼容99%的手机，长时间运行稳定且因为没有修改系统和软件所以理论上不存在被外挂监测和封号风险。
机器人支持查看问答记录，创建群、群管理，群发等功能，还支持接入自己的问答接口来全面接管机器人。

## 演示

**发送消息**

<img src="https://github.com/gallonyin/worktool/blob/master/images/send_message.gif"  height="500" width="280">

注：动图为机器人自动运行

更多演示看这里
https://www.apifox.cn/apidoc/project-1035094/doc-840833

## 兼容版本（重要）

经过测试验证的版本：企业微信 4.0.2 至 4.0.6

目前 4.0.8 经验证存在部分功能未兼容，需要后续迭代兼容

## 快速使用

[快速入门文档](https://www.apifox.cn/apidoc/project-1035094/doc-850007)

1. 准备一台无人使用的可联网安卓手机（本APP兼容99%安卓机型）
2. 手机登录企业微信（账号需要提前实名认证，不然很多功能无法正常使用）
3. 建议提前给该企业微信账号开通"工作台"-"客户群"权限（如无需外部群创建和管理可不开启）
4. 自助申请一个[机器人链接号(点击这里)](https://www.apifox.cn/apidoc/project-1035094/api-21488840)，目前申请机器人完全免费，您也可以加入QQ群向管理员免费申请。
5. 在这台手机上安装[WorkTool APP安装包(点击下载)](https://cdn.asrtts.cn/uploads/worktool/apk/worktool-1.1.apk)
6. 打开WorkTool APP，按照APP提示保存链接号，开启主功能，并打开到企业微信界面，不要关闭屏幕即可。

如果您想使用自己开发的QA回调接口接收机器人收到的所有消息并定制回答，请参考[第三方QA回调接口规范(点击这里)](https://www.apifox.cn/apidoc/project-1035094/doc-861677)开发接口，并在[机器人第三方QA配置(点击这里)](https://www.apifox.cn/apidoc/project-1035094/api-22587884)提交您的机器人id和回调接口地址

## 文档

这里有所有详细的API文档和调用示例！！！

这里有所有详细的API文档和调用示例！！！

这里有所有详细的API文档和调用示例！！！

https://www.apifox.cn/web/project/1035094

## 混淆

绝大部分代码均可以混淆，但由于使用的类库如okhttp、umeng不能混淆等情况，已经列在proguard-rules.pro当中，可以直接使用

#  Copyright

Apache License, Version 2.0

#  联系方式

- Email: gallonyin@163.com
- QQ群：764377820（链接号申请/问题反馈）
- 如果遇到问题欢迎在群里提问，个人能力也有限，希望一起学习一起进步。

# 版本更新

tag 1.2 2022-07-11 内部群已读数过滤；避免群名重复创建；可回调获取群二维码；其他稳定性优化

tag 1.1 2022-06-20 大幅度提高系统稳定性和响应速度

tag 1.0 2022-05-27 首次可用版本更新
