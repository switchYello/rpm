package com.fys;

import com.fys.cmd.listener.ErrorLogListener;
import com.fys.cmd.message.DataConnectionCmd;
import com.fys.conf.ServerWorker;
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
    private static Map<DataConnectionCmd, Promise<Channel>> waitConnections = new HashMap<>();

    /*
     * 当客户端登录成功后，遍历映射表，开启所有服务，并绑定到managerChannel的生命周期上
     * 开启关闭Server均使用单个eventLoop执行保证按顺序执行
     * */
    public static Promise<Server> startServers(ServerWorker sw, Channel managerChannel) {
        Promise<Server> promise = managerEventLoop.newPromise();
        execute(() -> {
            if (managerChannel.isActive()) {
                Server server = new Server(sw, managerChannel);
                server.start(promise);
                managerChannel.closeFuture().addListener(future -> execute(server::stop));
            } else {
                promise.setFailure(new RuntimeException("尚未创建server，但已经断连了"));
            }
        });
        return promise;
    }

    /*
     * 接收到客户端连接，为指定serverId的服务添加链接
     * */
    public static void addConnection(DataConnectionCmd cmd, Channel channel) {
        execute(() -> {
            Promise<Channel> promise = waitConnections.remove(cmd);
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
        ServerWorker sw = server.getServerWorker();
        DataConnectionCmd message = new DataConnectionCmd(sw.getServerPort(), sw.getLocalHost(), sw.getLocalPort(), System.nanoTime());
        server.getManagerChannel().writeAndFlush(message)
                .addListener(ErrorLogListener.INSTANCE)
                .addListener(future -> {
                    if (future.isSuccess()) {
                        execute(() -> {
                            waitConnections.put(message, promise);
                            //设置定时任务，超时则设置promise为failure
                            schedule(() -> {
                                waitConnections.remove(message);
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

    private static void execute(Runnable runnable) {
        if (managerEventLoop.inEventLoop()) {
            runnable.run();
        } else {
            managerEventLoop.execute(runnable);
        }
    }

    private static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return managerEventLoop.schedule(command, delay, unit);
    }


}
