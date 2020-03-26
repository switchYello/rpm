package com.fys;

import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.handler.TransactionHandler;
import com.fys.conf.ServerWorker;
import com.fys.handler.FlowManagerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private ChannelFuture bind;

    private ServerWorker serverWorker;
    private Channel managerChannel;
    private FlowManagerHandler flowManagerHandler;

    public Server(ServerWorker serverWorker, Channel managerChannel) {
        this.serverWorker = serverWorker;
        this.managerChannel = managerChannel;
        flowManagerHandler =
                new FlowManagerHandler("server on " + serverWorker.getServerPort(), "发送到使用者的", "从使用者接收的");
    }


    public void start(Promise<Server> promise) {
        ServerBootstrap sb = new ServerBootstrap();
        bind = sb.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 180));
                        ch.pipeline().addLast(flowManagerHandler);
                        ch.pipeline().addLast(new ServerHandler(Server.this));
                        ch.pipeline().addLast(ExceptionHandler.INSTANCE);
                    }
                }).bind(Config.bindHost, serverWorker.getServerPort());

        //添加服务开启成功失败事件
        bind.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                promise.setSuccess(this);
            } else {
                promise.setFailure(future.cause());
            }
        });
    }

    public void stop() {
        if (managerChannel != null && managerChannel.isActive()) {
            managerChannel.close();
        }
        if (bind != null && bind.channel().isActive()) {
            bind.channel().close().addListener(f -> log.info("Server在端口:{}关闭成功", serverWorker.getServerPort()));
        }
    }

    public ServerWorker getServerWorker() {
        return serverWorker;
    }

    public Channel getManagerChannel() {
        return managerChannel;
    }

    //当外网用户连接到监听的端口后，将打开与客户端的连接，并传输数据
    private static class ServerHandler extends ChannelInboundHandlerAdapter {

        private Server server;

        ServerHandler(Server server) {
            this.server = server;
        }

        @Override
        public void channelActive(ChannelHandlerContext userConnection) {
            Promise<Channel> promise = ServerManager.getConnection(server);
            promise.addListener((GenericFutureListener<Future<Channel>>) future -> {
                if (future.isSuccess()) {
                    Channel clientChannel = future.getNow();
                    clientChannel.pipeline().addLast("linkClient", new TransactionHandler(userConnection.channel(), true));
                    clientChannel.pipeline().addLast(ExceptionHandler.INSTANCE);
                    userConnection.pipeline().replace(this, "linkUser", new TransactionHandler(clientChannel, true));
                    userConnection.channel().config().setAutoRead(true);
                } else {
                    //promise失败可能是：超时没结果被定时任务取消的
                    log.error("服务端获取对客户端的连接失败,关闭userConnection", future.cause());
                    userConnection.close();
                }
            });
        }
    }
}
