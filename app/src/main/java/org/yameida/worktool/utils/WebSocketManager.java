package org.yameida.worktool.utils;

import android.util.Log;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.hjq.toast.ToastUtils;

import org.yameida.worktool.model.WeworkMessageBean;
import org.yameida.worktool.model.WeworkMessageListBean;
import org.yameida.worktool.service.WeworkController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {

    public static final String HEARTBEAT = "{\"type\":" + WeworkMessageBean.HEART_BEAT + "}";
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    public static Map<String, WebSocketManager> webSocketManager = new ConcurrentHashMap<>();
    private static final int reconnectInt = 5000;  //毫秒
    private static final long heartBeatRate = 10;  //秒
    private Map<String, Long> messageIdMap = new ConcurrentHashMap<>();
    private ScheduledFuture task;
    private WebSocket socket;
    private String url;
    private WebSocketListener listener;
    private boolean connecting = false;

    public WebSocketManager(String url, WebSocketListener listener) {
        Log.e(url, "新建链接");
        this.url = url;
        this.listener = listener;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        this.socket = client.newWebSocket(request, listener);
        webSocketManager.put(url, this);
        task = heartCheckStart();
    }

    public void send(WeworkMessageBean msg) {
        send(new WeworkMessageListBean(msg, WeworkMessageListBean.SOCKET_TYPE_MESSAGE_LIST, null));
    }

    /**
     * 确认消息
     * @param messageId 保存在map中30秒后清除
     * @return true继续消费事件 false重复事件不需消息
     */
    public synchronized boolean confirm(String messageId) {
        if (messageId == null || messageId.isEmpty()) return true;
        send(new WeworkMessageListBean(messageId, WeworkMessageListBean.SOCKET_TYPE_MESSAGE_CONFIRM));
        if (messageIdMap.containsKey(messageId)) return false;
        long currentTimeMillis = System.currentTimeMillis();
        messageIdMap.put(messageId, currentTimeMillis + 30 * 1000);
        for (Map.Entry<String, Long> entry : messageIdMap.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();
            if (currentTimeMillis > value) {
                messageIdMap.remove(key);
            }
        }
        return true;
    }

    public void send(WeworkMessageListBean msg) {
        send(msg, msg.getSocketType() == 2);
    }

    public void send(WeworkMessageListBean msg, boolean log) {
        String json = GsonUtils.toJson(msg);
        boolean success = socket.send(json);
        if (log && success)
            LogUtils.d(url, json, "通讯消息发送成功！");
        if (!success)
            LogUtils.e(url, json, "通讯消息发送失败！");
    }

    public void send(String msg) {
        boolean success = socket.send(msg);
        LogUtils.e(url, msg, (success ? "通讯消息发送成功！" : "通讯消息发送失败！"));
    }

    public void close(int code, String reason) {
        task.cancel(true);
        Log.e("url", "task 取消");
        this.socket.close(code, reason);
        Log.e(url, "链接关闭");
    }

    public static void closeManager() {
        Log.e("SocketManager", "关闭Manager:");
        for (Map.Entry<String, WebSocketManager> e : webSocketManager.entrySet()) {
            e.getValue().close(1000, "closeAll");
        }
        webSocketManager.clear();
        scheduledExecutorService.shutdown();
    }

    public void reConnect() {
        connecting = true;
        Log.e(url, "重连");
        boolean isConnect = false;
        int interval = reconnectInt;
        while (!isConnect) {
            try {
                isConnect = connect();
                Thread.sleep(interval);
                if (interval < 600000) {
                    interval *= 2;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        connecting = false;
    }

    private boolean connect() {
        WebSocket s = new OkHttpClient().newWebSocket(new Request.Builder().url(url).build(), listener);
        if (s.send(WebSocketManager.HEARTBEAT)) {
            this.socket = s;
            return true;
        }
        return false;
    }

    private ScheduledFuture heartCheckStart() {
        Runnable r = () -> {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            Log.e(url, "心跳检测" + df.format(new Date()));// new Date()为获取当前系统时间
            if (!connecting && (socket == null || !socket.send(HEARTBEAT))) {
                //断开链接后关闭新消息接收
                WeworkController.INSTANCE.setEnableLoopRunning(false);
                //断开链接后进入重连
                reConnect();
            }
//            ToastUtils.show("机器人运行中 请勿人工操作手机~");
        };

        //每heartBeatRate秒发一次心跳包
        return scheduledExecutorService.scheduleAtFixedRate(r, heartBeatRate, heartBeatRate, TimeUnit.SECONDS);
    }

    public static WebSocketManager getWebSocketManager(String id) {
        return webSocketManager.get(id);
    }
}
