# yuechenPicture

#### 介绍
月辰图库项目：http://39.104.69.233
用户名&密码都是：admin001

#### 软件架构
https://gitee.com/du-yuelang/yuechen-picture/blob/dev/%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1%E6%B5%81%E7%A8%8B.md


#### 使用说明&用户名密码

https://gitee.com/du-yuelang/yuechen-picture/blob/dev/%E9%A1%B9%E7%9B%AE%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E%E6%96%87%E6%A1%A3.md

#### 关键技术方案实现思路
https://gitee.com/du-yuelang/yuechen-picture/wikis/Home

#### 迭代开发清单
- [x] 用户注册优化：通过邮箱验证码进行注册
- [x] 特殊接口限流处理，例如AI编辑功能，以及AI扩图功能
- [ ] 引入对血腥暴力涉黄图片进行分析检测功能参考：https://juejin.cn/post/7408759813850791976
- [ ] 允许用户搜索公开的团队空间，并且添加申请加入的请求，团队空间管理员需要可以进行审批允许加入
- [ ] 允许团队成员自由退出团队空间
- [ ] 在多人协同编辑的时候不允许删除这个图片：
  - [ ] 可以通过Redis来实现，类似分布式锁，触发时机和事件监听机制一样，进入和退出的时候分别去加锁释放锁
  - [ ] 是否还可以使用select ... for updata来对数据行进行上锁呢？
- [ ] 添加收藏功能，用户可以将公有图库的图片收藏到自己的私有空间
- [ ] 为了方便面试官查看，体验完整的功能，提供类Admin账号，或者创建一个介于User和Admin之间的角色提供出去

