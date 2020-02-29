package com.fys;

import com.fys.cmd.message.DataConnectionCmd;
import com.fys.cmd.message.clientToServer.WantManagerCmd;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * hcy 2020/2/18
 */
public class ServerManager {

    private static Logger log = LoggerFactory.getLogger(ServerManager.class);

    private static EventLoop managerEventLoop = App.work.next();
    //这些promise在等待连接的到来
    private static Map<Long, Promise<Channel>> waitConnections = new HashMap<>();

    private static Map<Integer, Server> pauses = new HashMap<>();

    /**
     * 开启新的服务，用户数据转发
     */
    public static Promise<Server> startNewServer(WantManagerCmd msg, Channel managerChannel) {
        Promise<Server> promise = managerEventLoop.newPromise();
        execute(() -> {
            Server pause = pauses.remove(msg.getServerPort());
            if (pause != null && pause.getStatus() == Server.Status.pause) {
                log.info("找到已暂停的Server:{}，重新启用它", pause.getId());
                pause.reStart(msg, managerChannel, promise);
                return;
            }

            log.info("准备创建新server:{}", msg);
            Server server = new Server(msg, managerChannel);
            server.start(promise);
        });
        return promise;
    }


    /*
     * 为指定serverId的服务添加链接
     * */
    public static void addConnection(long token, Channel channel) {
        execute(() -> {
            Promise<Channel> promise = waitConnections.get(token);
            if (promise == null) {
                log.info("ServerManager.addConnection无法找到Promise，可能promise已被超时取消Channel:{}", channel);
                channel.close();
                return;
            }
            promise.setSuccess(channel);
        });
    }

    public static Promise<Channel> getConnection(Server server) {
        Promise<Channel> promise = managerEventLoop.newPromise();
        long token = System.nanoTime();
        DataConnectionCmd needNewConn = new DataConnectionCmd(server.getServerPort(), server.getLocalHost(), server.getLocalPort(), token);

        server.write(needNewConn).addListener(future -> {
            if (future.isSuccess()) {
                execute(() -> {
                    waitConnections.put(token, promise);
                    //设置定时任务，超时则设置promise为failure
                    schedule(() -> {
                        waitConnections.remove(token);
                        //说明result还没被设置值，说明还没成功
                        if (promise.isCancellable()) {
                            promise.setFailure(new TimeoutException("Promise超时无法获取连接"));
                        }
                    }, Config.timeOut, TimeUnit.SECONDS);
                });
            } else {
                promise.setFailure(future.cause());
            }
        });
        return promise;
    }

    /*暂停服务*/
    static void pauseServer(Server server) {
        execute(() -> {
            if (Server.Status.pause == server.getStatus()) {
                pauses.put(server.getServerPort(), server);
            }
        });
    }

    /*停止服务*/
    static void stopServer(Server server) {
        execute(() -> {
            if (server == pauses.get(server.getServerPort())) {
                pauses.remove(server.getServerPort());
            }
        });
    }

    static void execute(Runnable runnable) {
        if (managerEventLoop.inEventLoop()) {
            runnable.run();
        } else {
            managerEventLoop.execute(runnable);
        }
    }

    static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return managerEventLoop.schedule(command, delay, unit);
    }

    /*
     * 测试用例里用到，其他地方不使用这个方法
     * */
    static Map<Integer, Server> getPauseServer() {
        return pauses;
    }

}
