package com.fys;

import com.fys.cmd.clientToServer.WantManagerCmd;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * hcy 2020/2/18
 */
public class ServerManager {

    private static Logger log = LoggerFactory.getLogger(ServerManager.class);
    //这些promise在等待连接的到来
    private static Map<Short, Promise<Channel>> waitConnections = new ConcurrentHashMap<>();


    /**
     * 开启新的服务，用户数据转发
     */
    public static Promise<Server> startNewServer(WantManagerCmd msg, Channel managerChannel) {
        log.info("准备创建新server:{}", msg);
        Server server = new Server(msg, managerChannel);
        return server.start();
    }

    /*
     * 为指定serverId的服务添加链接
     * */
    public static void addConnection(short serverPort, Channel channel) {
        Promise<Channel> promise = waitConnections.remove(serverPort);
        if (promise == null) {
            log.debug("ServerManager.addConnection无法找到Promise，可能promise已被超时取消");
            channel.close();
            return;
        }
        promise.setSuccess(channel);
    }

    public static Promise<Channel> getConnection(EventLoop eventLoop, Server server) {
        Promise<Channel> promise = new DefaultPromise<>(eventLoop);
        NeedCreateNewConnectionCmd needNewConn = new NeedCreateNewConnectionCmd(server.getServerPort(), server.getLocalHost(), server.getLocalPort());

        server.write(needNewConn).addListener(future -> {
            if (future.isSuccess()) {
                waitConnections.put(server.getServerPort(), promise);
                //设置定时任务，超时则设置promise为failure
                ScheduledFuture<?> schedule = eventLoop.schedule(() -> promise.setFailure(new TimeoutException("Promise超时无法获取连接")), Config.timeOut, TimeUnit.SECONDS);
                //如果能成功获取到连接，则Promise被设为成功，若超时Promise被设为失败
                //若为成功时，则取消定时任务
                //设为失败时，则从map中移除promise （成功时不用移除，因为addConnection()内已经移除过了）
                promise.addListener(f -> {
                    if (f.isSuccess()) {
                        schedule.cancel(true);
                    } else {
                        waitConnections.remove(server.getServerPort());
                    }
                });
            } else {
                promise.setFailure(future.cause());
                log.debug("向客户端发送创建连接指令失败");
            }
        });
        return promise;
    }

}
