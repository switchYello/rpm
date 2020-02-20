package com.fys;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * hcy 2020/2/18
 */
public class ServerManager {

    private static Logger log = LoggerFactory.getLogger(ServerManager.class);
    private static List<Server> list = new ArrayList<>();

    /**
     * 开启新的服务，用户数据转发
     */
    public static ChannelFuture startNewServer(int port, Channel managerChannel, String clientName) {
        log.info("准备创建新server，端口:{},客户端:{}", port, clientName);

        for (Iterator<Server> iterator = list.iterator(); iterator.hasNext(); ) {
            Server s = iterator.next();
            if (Objects.equals(s.getPort(), port)) {
                log.info("端口已被使用,关闭旧server，端口:{},客户端:{}", port, s.getClientName());
                iterator.remove();
                s.close();
            }
        }
        Server server = new Server(port, managerChannel, clientName);
        return server.start();
    }

    /*
     * 为指定serverId的服务添加链接
     * */
    public static void addConnection(String serverId, Channel channel) {
        for (Server server : list) {
            if (Objects.equals(serverId, server.getId())) {
                log.info("添加连接到服务:{}中", server.getClientName());
                server.addConnection(channel);
                return;
            }
        }
        //对于找不到Server的数据连接，直接关闭连接
        channel.close();
    }

    //关闭server，同时从list中移除
    public static void unRegister(Server server) {
        list.remove(server);
    }

    public static void register(Server server) {
        list.add(server);
    }
}
