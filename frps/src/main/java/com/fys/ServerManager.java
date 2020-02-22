package com.fys;

import com.fys.cmd.clientToServer.WantManagerCmd;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * hcy 2020/2/18
 */
public class ServerManager {

    private static Logger log = LoggerFactory.getLogger(ServerManager.class);
    private static List<Server> register = new ArrayList<>();
    //这些promise在等待连接的到来
    private static Map<Long, Promise<Channel>> waitConnections = new ConcurrentHashMap<>();


    /**
     * 开启新的服务，用户数据转发
     */
    public static Promise<Server> startNewServer(WantManagerCmd msg, Channel managerChannel) {
        log.info("准备创建新Server:{}", msg);

        for (Server s : register) {
            if (Objects.equals(s.getServerPort(), msg.getServerPort())) {
                if (s.getStatus() == Server.Status.start) {
                    return new DefaultPromise<Server>(s.getEventLoop())
                            .setFailure(new IllegalStateException("Server:" + s.getId() + "正在使用端口:" + msg.getServerPort() + "，请更换其他端口"));
                }
            }
        }

        Server server = new Server(msg, managerChannel);
        return server.start().addListener((GenericFutureListener<? extends Future<Server>>) future -> {
            if (future.isSuccess()) {
                Server s = future.getNow();
                log.info("将Server:{} 注册到ServerManager,", s);
                register.add(s);
                log.debug("当前Server如下");
                for (Server currentServer : register) {
                    log.debug(currentServer.toString());
                }
                log.info("");
            }
        });
    }

    /*
     * 为指定serverId的服务添加链接
     * */
    public static void addConnection(long token, Channel channel) {
        Promise<Channel> promise = waitConnections.remove(token);
        if (promise == null) {
            log.debug("ServerManager.addConnection无法找到Promise，可能promise已被超时取消");
            channel.close();
            return;
        }
        promise.setSuccess(channel);
    }

    public static Promise<Channel> getConnection(EventLoop eventLoop, Server server) {
        Promise<Channel> promise = new DefaultPromise<>(eventLoop);
        long token = System.nanoTime();
        waitConnections.put(token, promise);
        ScheduledFuture<?> schedule = eventLoop.schedule(() -> promise.setFailure(new TimeoutException("Promise超时无法获取连接")), Config.timeOut, TimeUnit.SECONDS);
        //如果能成功获取到连接Promise被设为成功，若超时Promise被设为失败
        //设为成功时，则取消定时任务
        //设为失败时，则从map中移除promise （成功时不用移除，因为设置成功方法内已经移除过了）
        promise.addListener(future -> {
            if (future.isSuccess()) {
                schedule.cancel(true);
            } else {
                waitConnections.remove(token);
            }
        });
        NeedCreateNewConnectionCmd needNewConn = new NeedCreateNewConnectionCmd(server.getServerPort(), server.getLocalHost(), server.getLocalPort(), token);
        server.write(needNewConn)
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        log.debug("向客户端发送创建连接指令失败");
                    }
                });
        return promise;
    }

    public static void unRegister(Server server) {
        register.remove(server);
    }

}
