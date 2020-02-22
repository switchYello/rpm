package com.fys;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * hcy 2020/2/18
 */
public class ServerManager {

    private static Logger log = LoggerFactory.getLogger(ServerManager.class);
    private static List<Server> register = new ArrayList<>();
    private static List<Server> unRegister = new ArrayList<>();

    /**
     * 开启新的服务，用户数据转发
     */
    public static Promise<Server> startNewServer(int port, Channel managerChannel, String clientName) {
        log.info("准备创建新Server，客户端:{},端口:{}", clientName, port);

        for (Server s : register) {
            if (Objects.equals(s.getPort(), port)) {
                log.info("端口已被使用,关闭旧Server，客户端:{},端口:{}", s.getClientName(), port);
                s.stop();
            }
        }
        register.removeAll(unRegister);
        unRegister.clear();

        Server server = new Server(port, managerChannel, clientName);
        return server.start().addListener((GenericFutureListener<? extends Future<Server>>) future -> {
            if (future.isSuccess()) {
                Server s = future.getNow();
                log.info("注册Server,ClientName:{},Port:{}", s.getClientName(), s.getPort());
                register.add(future.getNow());
                log.debug("当前Server情况");
                for (Server currentServer : register) {
                    log.debug("ServerId:{},ServerPort:{},ClientName:{}", currentServer.getId(), currentServer.getPort(), currentServer.getClientName());
                }
            }
        });
    }

    /*
     * 为指定serverId的服务添加链接
     * */
    public static void addConnection(String serverId, long token, Channel channel) {
        for (Server server : register) {
            if (Objects.equals(serverId, server.getId())) {
                log.debug("添加连接到服务:{}中", server.getId());
                server.addConnection(token, channel);
                return;
            }
        }
        log.info("找不到对应的Server无法添加连接,ServerId:{},Token:{}", serverId, token);
        //对于找不到Server的数据连接，直接关闭连接
        channel.close();
    }

    public static void unRegister(Server server) {
        unRegister.add(server);
    }

}
