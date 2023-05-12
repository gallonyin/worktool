package org.yameida.worktool.model;


import java.util.List;
import java.util.Objects;

public class WeworkMessageBean {

    /**
     * type
     * <p>
     *消息类型10
     * 心跳 HEART_BEAT
     * <p>
     * 消息类型 100
     * 上传消息列表 TYPE_RECEIVE_MESSAGE_LIST
     * 交互通知 TYPE_CONSOLE_TOAST
     * <p>
     * 全局操作类型 200
     * 停止所有任务并返回首页待命 STOP_AND_GO_HOME
     * 回到首页等待接收新消息 LOOP_RECEIVE_NEW_MESSAGE
     * 在房间内发送消息 SEND_MESSAGE
     * 在房间内指定回复消息 REPLY_MESSAGE
     * 在房间内转发消息 RELAY_MESSAGE
     * 创建群 INIT_GROUP
     * 进入群聊并修改群配置 INTO_GROUP_AND_CONFIG
     * 推送微盘图片 PUSH_MICRO_DISK_IMAGE
     * 推送微盘文件 PUSH_MICRO_DISK_FILE
     * 推送小程序 PUSH_MICROPROGRAM
     * 推送腾讯文档 PUSH_OFFICE
     * 通过当前所有好友请求 PASS_ALL_FRIEND_REQUEST
     * 按手机号添加好友 ADD_FRIEND_BY_PHONE
     * 展示群信息 SHOW_GROUP_INFO
     * 推送文件(网络图片视频和文件等) PUSH_FILE
     * 解散群聊 DISMISS_GROUP
     * 从外部群添加好友 ADD_FRIEND_BY_GROUP
     * 添加待办 ADD_NEED_DEAL
     * 打卡 CLOCK_IN
     * 切换企业 SWITCH_CORP
     * 推送链接 PUSH_LINK
     * 修改群成员信息 MODIFY_GROUP_MEMBER_INFO
     * 撤回消息 RECALL_MESSAGE
     * 批量转发 RELAY_MULTI_MESSAGE
     * 合并转发 RELAY_MERGE_MESSAGE
     * 批量发送 SEND_MULTI_MESSAGE
     * 合并发送 SEND_MERGE_MESSAGE
     * <p>
     * 非操作类型 300
     * 机器人普通日志记录 ROBOT_LOG
     * 机器人异常日志记录 ROBOT_ERROR_LOG
     * 机器人接口测试 ROBOT_CONTROLLER_TEST
     * <p>
     * 获取数据类型 500
     * 获取群信息 GET_GROUP_INFO
     * 获取好友信息 GET_FRIEND_INFO
     * 获取我的信息 GET_MY_INFO
     * 获取最近聊天列表 GET_RECENT_LIST
     * 获取企业列表 GET_CORP_LIST
     * 获取全部好友信息 GET_ALL_FRIEND_INFO
     * 获取全部群信息 GET_ALL_GROUP_INFO
     */
    public static final int HEART_BEAT = 11;
    public static final int TYPE_RECEIVE_MESSAGE_LIST = 101;
    public static final int TYPE_CONSOLE_TOAST = 102;

    public static final int STOP_AND_GO_HOME = 201;
    public static final int LOOP_RECEIVE_NEW_MESSAGE = 202;
    public static final int SEND_MESSAGE = 203;
    public static final int REPLY_MESSAGE = 204;
    public static final int RELAY_MESSAGE = 205;
    public static final int INIT_GROUP = 206;
    public static final int INTO_GROUP_AND_CONFIG = 207;
    public static final int PUSH_MICRO_DISK_IMAGE = 208;
    public static final int PUSH_MICRO_DISK_FILE = 209;
    public static final int PUSH_MICROPROGRAM = 210;
    public static final int PUSH_OFFICE = 211;
    public static final int PASS_ALL_FRIEND_REQUEST = 212;
    public static final int ADD_FRIEND_BY_PHONE = 213;
    public static final int SHOW_GROUP_INFO = 214;
    public static final int INIT_ANTI_HARASSMENT_RULE = 215;
    public static final int UPDATE_ANTI_HARASSMENT_RULE = 216;
    public static final int DELETED_ANTI_HARASSMENT_RULE = 217;
    public static final int PUSH_FILE = 218;
    public static final int DISMISS_GROUP = 219;
    public static final int ADD_FRIEND_BY_GROUP = 220;
    public static final int ADD_NEED_DEAL = 221;
    public static final int CLOCK_IN = 222;
    public static final int SWITCH_CORP = 223;
    public static final int PUSH_LINK = 224;
    public static final int MODIFY_GROUP_MEMBER_INFO = 225;
    public static final int RECALL_MESSAGE = 226;
    public static final int RELAY_MULTI_MESSAGE = 227;
    public static final int RELAY_MERGE_MESSAGE = 228;
    public static final int SEND_MULTI_MESSAGE = 229;
    public static final int SEND_MERGE_MESSAGE = 230;

    public static final int ROBOT_LOG = 301;
    public static final int ROBOT_ERROR_LOG = 302;
    public static final int ROBOT_CONTROLLER_TEST = 303;

    public static final int GET_GROUP_INFO = 501;
    public static final int GET_FRIEND_INFO = 502;
    public static final int GET_MY_INFO = 503;
    public static final int GET_GROUP_QRCODE = 504;
    public static final int GET_RECENT_LIST = 505;
    public static final int GET_CORP_LIST = 506;
    public static final int GET_ALL_FRIEND_INFO = 507;
    public static final int GET_ALL_GROUP_INFO = 508;

    /**
     * roomType
     * <p>
     * 外部群 ROOM_TYPE_EXTERNAL_GROUP
     * 外部联系人 ROOM_TYPE_EXTERNAL_CONTACT
     * 内部群 ROOM_TYPE_INTERNAL_GROUP
     * 内部联系人 ROOM_TYPE_INTERNAL_CONTACT
     */
    public static final int ROOM_TYPE = 0;
    public static final int ROOM_TYPE_UNKNOWN = 0;
    public static final int ROOM_TYPE_EXTERNAL_GROUP = 1;
    public static final int ROOM_TYPE_EXTERNAL_CONTACT = 2;
    public static final int ROOM_TYPE_INTERNAL_GROUP = 3;
    public static final int ROOM_TYPE_INTERNAL_CONTACT = 4;

    /**
     * textType
     * <p>
     * 文本类型 TEXT_TYPE_PLAIN
     * 表情类型 同文本类型
     * 群公告类型 同文本类型
     * 图片类型 TEXT_TYPE_IMAGE
     * 语音类型 TEXT_TYPE_VOICE
     * 名片类型 TEXT_TYPE_CARD
     * 视频类型 TEXT_TYPE_VIDEO
     * 定位类型 TEXT_TYPE_LOCATION
     * 小程序类型 TEXT_TYPE_MICROPROGRAM
     * 链接类型 TEXT_TYPE_LINK
     * 群通知类型 同链接类型
     * 文件类型 TEXT_TYPE_FILE
     * 警告类型 TEXT_TYPE_WARNING
     * 腾讯文档类型 TEXT_TYPE_OFFICE
     * 群接龙类型 TEXT_TYPE_SOLITAIRE
     * 合并聊天记录类型 TEXT_TYPE_CHAT_RECORD
     * 群收集表类型 TEXT_TYPE_COLLECTION
     * 接收带回复引用文本类型 TEXT_TYPE_REPLY
     */
    public static final int TEXT_TYPE = 0;
    public static final int TEXT_TYPE_UNKNOWN = 0;
    public static final int TEXT_TYPE_PLAIN = 1;
    public static final int TEXT_TYPE_IMAGE = 2;
    public static final int TEXT_TYPE_VOICE = 3;
    public static final int TEXT_TYPE_CARD = 4;
    public static final int TEXT_TYPE_VIDEO = 5;
    public static final int TEXT_TYPE_LOCATION = 6;
    public static final int TEXT_TYPE_MICROPROGRAM = 7;
    public static final int TEXT_TYPE_LINK = 8;
    public static final int TEXT_TYPE_FILE = 9;
    public static final int TEXT_TYPE_WARNING = 10;
    public static final int TEXT_TYPE_OFFICE = 11;
    public static final int TEXT_TYPE_SOLITAIRE = 12;
    public static final int TEXT_TYPE_CHAT_RECORD = 13;
    public static final int TEXT_TYPE_COLLECTION = 14;
    public static final int TEXT_TYPE_REPLY = 15;

    //消息id(解析指令时同步)
    public String messageId;
    //api类型(解析指令时同步) 0=后台消息 1=API指令调用
    public Integer apiSend;

    //标题 通常是群名或联系人
    public List<String> titleList;
    //上传聊天列表
    public List<SubMessageBean> messageList;
    //上传日志内容
    public String log;
    //外部群1 外部联系人2 内部群3 内部联系人4
    public Integer roomType;

    //接收人名称
    public String receivedName;
    //内容移除了@me
    public String receivedContent;
    //想要at的昵称
    public String at;
    //想要at的昵称列表
    public List<String> atList;
    //原始内容text
    public String originalContent;
    //多选(转发等)
    public List<String> nameList;
    //转发附加留言
    public String extraText;
    //接收消息类型
    public Integer textType;
    //最大尝试次数
    public Integer maxRetryCount;

    //群名
    public String groupName;
    //群主名称
    public String groupOwner;
    //成员名单
    public List<String> selectList;
    //成员数
    public Integer groupNumber;
    //群公告
    public String groupAnnouncement;
    //群备注
    public String groupRemark;
    //群模板
    public String groupTemplate;
    //新群名
    public String newGroupName;
    //新群公告
    public String newGroupAnnouncement;
    //踢人列表
    public List<String> removeList;
    //拉人是否附带历史记录
    public Boolean showMessageHistory;
    //我的信息
    public MyInfo myInfo;
    //对象名称(图片、文件、小程序等)
    public String objectName;
    //二维码转码
    public String qrcode;
    //批量发送
    public List<WeworkMessageBean> weworkMessageList;

    //添加好友
    public Friend friend;

    //网络文件
    public String fileBase64;
    public String fileUrl;
    public String fileType;

    public WeworkMessageBean() {}

    public WeworkMessageBean(String receivedName, String receivedContent, Integer type, Integer roomType, List<String> titleList, List<SubMessageBean> messageList, String log) {
        this.type = type;
        this.roomType = roomType;
        this.titleList = titleList;
        this.messageList = messageList;
        this.log = log;
        this.receivedContent = receivedContent;
        this.receivedName = receivedName;
    }

    //消息类型
    public Integer type = 0;

    //消息列表的每条消息
    public static class SubMessageBean {
        //0其他人 1机器人自己 2unknown(如系统消息)
        public Integer sender;
        //消息类型判断 仅针对sender=0
        public Integer textType;
        public List<ItemMessageBean> itemMessageList;
        public List<String> nameList;
        public Boolean imageRepeat;
        public Integer imageSize;
        public byte[] image;

        public SubMessageBean(Integer sender, Integer textType, List<ItemMessageBean> itemMessageList, List<String> nameList) {
            this.sender = sender;
            this.textType = textType;
            this.itemMessageList = itemMessageList;
            this.nameList = nameList;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubMessageBean that = (SubMessageBean) o;
            return Objects.equals(sender, that.sender) && Objects.equals(textType, that.textType) && Objects.equals(itemMessageList, that.itemMessageList) && Objects.equals(nameList, that.nameList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sender, textType, itemMessageList, nameList);
        }

        @Override
        public String toString() {
            return "SubMessageBean{" +
                    "sender=" + sender +
                    ", textType=" + textType +
                    ", itemMessageList=" + itemMessageList +
                    ", nameList=" + nameList +
                    ", imageRepeat=" + imageRepeat +
                    ", imageSize=" + imageSize +
                    '}';
        }
    }

    //消息列表每条消息的text推断
    public static class ItemMessageBean {
        //0消息主体上方信息 如日期等 系统消息(拉人/撤回/外部群等居中的提示语)
        //2消息内容
        public Integer feature;
        public String text;

        public ItemMessageBean(Integer feature, String text) {
            this.feature = feature;
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemMessageBean that = (ItemMessageBean) o;
            return Objects.equals(feature, that.feature) && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(feature, text);
        }

        @Override
        public String toString() {
            return "ItemMessageBean{" +
                    "feature=" + feature +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    //我的信息
    public static class MyInfo {
        public String name;
        public String alias;
        public String gender;
        public String showName;
        public String workSign;
        public String corporation;
        public String phone;
        public String job;
        public String sumInfo;
    }

    //添加好友
    public static class Friend {
        //按手机号搜索
        public String phone;
        //好友姓名
        public String name;
        //备注名
        public String markName;
        //备注企业
        public String markCorp;
        //备注更多描述
        public String markExtra;
        //备注标签(推荐)
        public List<String> tagList;
        //是否是新好友
        public Boolean newFriend;
        //留言
        public String leavingMsg;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Friend friend = (Friend) o;
            return Objects.equals(phone, friend.phone) && Objects.equals(name, friend.name) && Objects.equals(markName, friend.markName) && Objects.equals(markCorp, friend.markCorp) && Objects.equals(markExtra, friend.markExtra) && Objects.equals(tagList, friend.tagList) && Objects.equals(newFriend, friend.newFriend) && Objects.equals(leavingMsg, friend.leavingMsg);
        }

        @Override
        public int hashCode() {
            return Objects.hash(phone, name, markName, markCorp, markExtra, tagList, newFriend, leavingMsg);
        }

        @Override
        public String toString() {
            return "Friend{" +
                    "phone='" + phone + '\'' +
                    ", name='" + name + '\'' +
                    ", markName='" + markName + '\'' +
                    ", markCorp='" + markCorp + '\'' +
                    ", markExtra='" + markExtra + '\'' +
                    ", tagList=" + tagList +
                    ", newFriend=" + newFriend +
                    ", leavingMsg='" + leavingMsg + '\'' +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeworkMessageBean that = (WeworkMessageBean) o;
        return Objects.equals(titleList, that.titleList) && Objects.equals(messageList, that.messageList) && Objects.equals(log, that.log) && Objects.equals(roomType, that.roomType) && Objects.equals(receivedName, that.receivedName) && Objects.equals(receivedContent, that.receivedContent) && Objects.equals(at, that.at) && Objects.equals(atList, that.atList) && Objects.equals(originalContent, that.originalContent) && Objects.equals(nameList, that.nameList) && Objects.equals(extraText, that.extraText) && Objects.equals(textType, that.textType) && Objects.equals(groupName, that.groupName) && Objects.equals(groupOwner, that.groupOwner) && Objects.equals(selectList, that.selectList) && Objects.equals(groupNumber, that.groupNumber) && Objects.equals(groupAnnouncement, that.groupAnnouncement) && Objects.equals(groupRemark, that.groupRemark) && Objects.equals(groupTemplate, that.groupTemplate) && Objects.equals(newGroupName, that.newGroupName) && Objects.equals(newGroupAnnouncement, that.newGroupAnnouncement) && Objects.equals(removeList, that.removeList) && Objects.equals(showMessageHistory, that.showMessageHistory) && Objects.equals(myInfo, that.myInfo) && Objects.equals(objectName, that.objectName) && Objects.equals(qrcode, that.qrcode) && Objects.equals(friend, that.friend) && Objects.equals(fileBase64, that.fileBase64) && Objects.equals(fileUrl, that.fileUrl) && Objects.equals(fileType, that.fileType) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titleList, messageList, log, roomType, receivedName, receivedContent, at, atList, originalContent, nameList, extraText, textType, groupName, groupOwner, selectList, groupNumber, groupAnnouncement, groupRemark, groupTemplate, newGroupName, newGroupAnnouncement, removeList, showMessageHistory, myInfo, objectName, qrcode, friend, fileBase64, fileUrl, fileType, type);
    }

    @Override
    public String toString() {
        return "WeworkMessageBean{" +
                "messageId='" + messageId + '\'' +
                ", titleList=" + titleList +
                ", messageList=" + messageList +
                ", log='" + log + '\'' +
                ", roomType=" + roomType +
                ", receivedName='" + receivedName + '\'' +
                ", receivedContent='" + receivedContent + '\'' +
                ", at='" + at + '\'' +
                ", atList=" + atList +
                ", originalContent='" + originalContent + '\'' +
                ", nameList=" + nameList +
                ", extraText='" + extraText + '\'' +
                ", textType=" + textType +
                ", groupName='" + groupName + '\'' +
                ", groupOwner='" + groupOwner + '\'' +
                ", selectList=" + selectList +
                ", groupNumber=" + groupNumber +
                ", groupAnnouncement='" + groupAnnouncement + '\'' +
                ", groupRemark='" + groupRemark + '\'' +
                ", groupTemplate='" + groupTemplate + '\'' +
                ", newGroupName='" + newGroupName + '\'' +
                ", newGroupAnnouncement='" + newGroupAnnouncement + '\'' +
                ", removeList=" + removeList +
                ", showMessageHistory=" + showMessageHistory +
                ", myInfo=" + myInfo +
                ", objectName='" + objectName + '\'' +
                ", qrcode='" + qrcode + '\'' +
                ", friend=" + friend +
                ", fileBase64='" + fileBase64 + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileType='" + fileType + '\'' +
                ", type=" + type +
                '}';
    }
}
