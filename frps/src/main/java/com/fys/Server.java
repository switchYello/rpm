package com.fys;

import com.fys.cmd.handler.TransactionHandler;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
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

    private static Logger log = LoggerFactory.getLogger(Server.class);
    private EventLoopGroup boss = App.boss;
    private EventLoopGroup work = App.work;

    //监听的地址和端口
    private String id = UUID.randomUUID().toString();
    private String clientName;
    private int port;
    private Channel managerChanel;
    private ChannelFuture bind;
    //这些promise在等待连接的到来
    private Map<Long, Promise<Channel>> waitConnections = new ConcurrentHashMap<>();


    public Server(int port, Channel managerChanel, String clientName) {
        this.port = port;
        this.clientName = clientName;
        this.managerChanel = managerChanel;
        //为管理添加关闭事件，连接关闭时同时关闭Server
        managerChanel.closeFuture().addListener((ChannelFutureListener) future -> {
            log.info("服务的ManagerChannel被关闭了，关闭server，端口:{},客户端:{}", port, clientName);
            close();
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
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new ServerHandler(Server.this));
                    }
                })
                .bind(Config.bindHost, port)
                //添加服务开启成功失败事件
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        promise.setSuccess(this);
                        log.info("服务端for:{}在端口:{}启动成功", clientName, port);
                    } else {
                        promise.setFailure(future.cause());
                        log.error("服务端for:{}在端口:{}启动失败", clientName, port, future.cause());
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
        Promise<Channel> promise = waitConnections.get(token);
        if (promise == null) {
            log.info("addConnection 但无法找到promise，可能promise已被取消");
            dataChanel.close();
            return;
        }
        promise.setSuccess(dataChanel);
    }

    //如果池中存在连接，则优先使用池中的，否则创建promise存入等待队列中
    public Promise<Channel> getConnection() {
        Promise<Channel> promise = getTimeOutPromise(Config.timeOut);
        long token = System.nanoTime();
        waitConnections.put(token, promise);
        promise.addListener(future -> waitConnections.remove(token));

        managerChanel.writeAndFlush(new NeedCreateNewConnectionCmd(token))
                .addListener(future -> {
                    if (future.isSuccess()) {
                    } else {
                        log.info("向客户端发送创建连接指令失败");
                    }
                });
        return promise;
    }


    /*
     * 创建一个promise
     * 这个promise会在指定时间后无结果则取消，有结果（无论成功失败）不会取消
     * 只有在定时任务里这一个地方会取消promise，其他地方都不会给该promise设置失败
     * */
    private Promise<Channel> getTimeOutPromise(int timeOut) {
        Promise<Channel> promise = new DefaultPromise<>(work.next());
        ScheduledFuture<?> schedule = work.next().schedule(() -> promise.setFailure(new TimeoutException("promise超时无法获取连接")), timeOut, TimeUnit.SECONDS);
        promise.addListener(future -> {
            if (future.isSuccess()) {
                schedule.cancel(true);
            }
        });
        return promise;
    }

    /*
     * 停止Server，即直接关闭manager连接，其他行为都在连接的回掉函数里处理
     * */
    public void stop() {
        if (managerChanel != null && managerChanel.isActive()) {
            managerChanel.close();
        }
    }

    /*
     * 真实处理stop server，清理资源
     * */
    private void close() {
        if (bind != null && bind.channel().isActive()) {
            bind.channel().close();
        }
        if (managerChanel != null && managerChanel.isActive()) {
            managerChanel.close();
        }
    }

    //当外网用户连接到监听的端口后，将打开与客户端的连接，并传输数据
    private static class ServerHandler extends ChannelInboundHandlerAdapter {

        private Server server;

        ServerHandler(Server server) {
            this.server = server;
        }

        @Override
        public void channelActive(ChannelHandlerContext webConnection) {
            Promise<Channel> promise = server.getConnection();
            promise.addListener((GenericFutureListener<Future<Channel>>) future -> {
                if (future.isSuccess()) {
                    Channel clientChannel = future.getNow();
                    clientChannel.pipeline().addLast("linkClient", new TransactionHandler(webConnection.channel(), true));
                    webConnection.pipeline().addLast("linkUser", new TransactionHandler(clientChannel, false));
                    webConnection.read();
                } else {
                    //promise失败可能是1：超时没结果被定时任务取消的
                    log.error("服务端获取对客户端的连接失败", future.cause());
                    webConnection.close();
                }
            });
        }
    }
}
