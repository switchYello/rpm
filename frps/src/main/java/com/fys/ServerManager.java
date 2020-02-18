package com.fys;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * hcy 2020/2/18
 */
public class ServerManager {

    private static Logger log = LoggerFactory.getLogger(ServerManager.class);
    private static Map<String, Server> map = new HashMap<>();

    public static void startNewServer(int port, Channel managerChannel) {
        Server server = new Server("127.0.0.1", port, managerChannel);
        map.put(server.getId(), server);
        server.start();
        log.info("创建新server，id:{},端口:{}", server.getId(), port);
    }

    public static void addConnection(String serverId, Channel channel) {
        log.info("添加连接到server，id:{}", serverId);
        Server server = map.get(serverId);
        if (server != null) {
            server.addConnection(channel);
        }
    }


}
