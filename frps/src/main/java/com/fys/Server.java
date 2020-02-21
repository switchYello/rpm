package com.fys;

import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.handler.TransactionHandler;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 客户端要求监听某一个端口后创建并启动此类
 * 接收外接连接传送到客户端
 * 因此此类不会autoRead
 * hcy 2020/2/17
 */
public class Server {

    private enum Status {
        start, stopIng, stop;
    }

    private static Logger log = LoggerFactory.getLogger(Server.class);
    private EventLoopGroup boss = App.boss;
    private EventLoopGroup work = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);

    private volatile Status status = Status.start;
    private final String id;
    private int port;
    private String clientName;
    private Channel managerChannel;
    private ChannelFuture bind;
    //这些promise在等待连接的到来
    private Map<Long, Promise<Channel>> waitConnections = new ConcurrentHashMap<>();

    public Server(int port, Channel managerChannel, String clientName) {
        this.port = port;
        this.clientName = clientName;
        this.managerChannel = managerChannel;
        this.id = "Server_" + port + "_" + clientName;
        //服务启动成功，为管理添加关闭事件，关闭连接时同时关闭Server
        managerChannel.closeFuture().addListener((ChannelFutureListener) managerFuture -> {
            this.stop();
        });
    }

    //开启服务监听客户端要求的端口，等待用户连接但不自动读数据
    public Promise<Server> start() {
        Promise<Server> promise = new DefaultPromise<>(work.next());
        ServerBootstrap sb = new ServerBootstrap();
        bind = sb.group(boss, work)
                .channel(App.serverClass)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 120));
                        ch.pipeline().addLast(new ServerHandler(Server.this));
                    }
                }).bind(Config.bindHost, port);
        //添加服务开启成功失败事件
        bind.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("Server在端口:{}启动成功", port);
                promise.setSuccess(this);
            } else {
                log.error("Server在端口:" + port + "启动失败", future.cause());
                promise.setFailure(future.cause());
            }
        });
        return promise;
    }

    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public String getClientName() {
        return clientName;
    }

    //添加客户端连接到池中，如果有等待需要的promise则优先给promise
    public void addConnection(long token, Channel dataChanel) {
        Promise<Channel> promise = waitConnections.remove(token);
        if (promise == null) {
            log.debug("Server.addConnection无法找到Promise，可能promise已被超时取消");
            dataChanel.close();
            return;
        }
        promise.setSuccess(dataChanel);
    }

    public Promise<Channel> getConnection(EventLoop eventLoop) {
        Promise<Channel> promise = new DefaultPromise<>(eventLoop);
        long token = System.nanoTime();
        waitConnections.put(token, promise);
        ScheduledFuture<?> schedule = eventLoop.schedule(() -> promise.setFailure(new TimeoutException("Promise超时无法获取连接ClientName:" + clientName)), Config.timeOut, TimeUnit.SECONDS);
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
        managerChannel.writeAndFlush(new NeedCreateNewConnectionCmd(token))
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        log.debug("向客户端发送创建连接指令失败");
                    }
                });
        return promise;
    }

    /*
     * 停止Server，即直接关闭manager连接，其他行为都在连接的回掉函数里处理
     * */
    public void stop() {
        //防止多次关闭
        if (status != Status.start) {
            return;
        }
        status = Status.stopIng;
        log.info("准备关闭Server，端口:{},客户端:{}", port, clientName);
        if (managerChannel != null && managerChannel.isActive()) {
            managerChannel.close();
        }
        ServerManager.unRegister(this);
        if (bind != null && bind.channel().isActive()) {
            bind.channel().close();
        }
        work.shutdownGracefully();
        status = Status.stop;
    }

    //当外网用户连接到监听的端口后，将打开与客户端的连接，并传输数据
    private static class ServerHandler extends ChannelInboundHandlerAdapter {

        private Server server;

        ServerHandler(Server server) {
            this.server = server;
        }

        @Override
        public void channelActive(ChannelHandlerContext userConnection) {
            Promise<Channel> promise = server.getConnection(userConnection.channel().eventLoop());
            promise.addListener((GenericFutureListener<Future<Channel>>) future -> {
                if (future.isSuccess()) {
                    Channel clientChannel = future.getNow();
                    clientChannel.pipeline().addLast("linkClient", new TransactionHandler(userConnection.channel(), true));
                    userConnection.pipeline().addLast("linkUser", new TransactionHandler(clientChannel, false));
                    userConnection.read();
                } else {
                    //promise失败可能是1：超时没结果被定时任务取消的
                    log.error("服务端获取对客户端的连接失败,关闭userConnection", future.cause());
                    userConnection.close();
                }
            });
        }
    }
}
