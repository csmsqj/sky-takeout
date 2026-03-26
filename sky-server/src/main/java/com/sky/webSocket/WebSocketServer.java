package com.sky.webSocket;

import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务组件
 * 注解 @ServerEndpoint 声明了客户端连接该 WebSocket 的具体路径
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    // 存放所有成功建立的会话对象。键为客户端ID（如管理端），值为 Session 对象。
    // 使用 static 是因为每来一个客户端连接，底层都会创建一个新的 WebSocketServer 实例。静态变量确保所有实例共享同一个 Map。
    private static Map<String, Session> sessionMap = new HashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + " 建立连接");
        sessionMap.put(sid, session); // 将新接入的客户端加入集合
    }

    /**
     * 收到客户端消息后调用的方法 (如果是双向聊天才需要)
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + " 的信息: " + message);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开：" + sid);
        sessionMap.remove(sid); // 客户端断开后，从集合清理释放资源
    }

    /**
     * 自定义的方法：用于给所有已连接的客户端群发消息
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                // 服务器向客户端主动发送文本消息的核心方法
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
