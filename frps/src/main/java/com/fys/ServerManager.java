package com.fys;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
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
    public static void startNewServer(int port, Channel channel, String clientName) {
        log.info("准备创建新server，端口:{},客户端:{}", port, clientName);
        
        for (Iterator<Server> iterator = list.iterator(); iterator.hasNext(); ) {
            Server s = iterator.next();
            if (Objects.equals(s.getPort(), port)) {
                log.info("端口已被使用,关闭旧server，端口:{},客户端:{}", port, s.getClientName());
                iterator.remove();
                s.close();
            }
        }
        Server server = new Server(port, channel, clientName);
        channel.closeFuture().addListener((ChannelFutureListener) future -> {
            log.info("服务的ManagerChannel被关闭了，关闭server，端口:{},客户端:{}", server.getPort(), server.getClientName());
            server.close();
            list.remove(server);
        });
        list.add(server);
        server.start();
    }

    /*
     * 为指定serverId的服务添加链接
     * */
    public static void addConnection(String serverId, Channel channel) {
        list.stream()
                .filter(s -> Objects.equals(serverId, s.getId()))
                .findFirst()
                .ifPresent(s -> {
                    log.info("添加连接到服务:{}中", s.getClientName());
                    s.addConnection(channel);
                });
    }


}
