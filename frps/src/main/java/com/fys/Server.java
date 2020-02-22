package com.fys;

import com.fys.cmd.clientToServer.WantManagerCmd;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.handler.TransactionHandler;
import com.fys.cmd.listener.ErrorLogListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
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

    public enum Status {
        start, stopIng, stop;
    }

    private static Logger log = LoggerFactory.getLogger(Server.class);
    private EventLoopGroup boss = App.boss;
    private EventLoopGroup work = App.work;
    private volatile Status status = Status.start;


    private final String id;
    private ChannelFuture bind;
    private WantManagerCmd want;
    private Channel managerChannel;

    public Server(WantManagerCmd wantManagerCmd, Channel managerChannel) {
        this.want = wantManagerCmd;
        this.managerChannel = managerChannel;
        this.id = "Server_[" + want.getServerPort() + "->" + want.getLocalPort() + "]";
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
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 90));
                        ch.pipeline().addLast(new ServerHandler(Server.this));
                    }
                }).bind(Config.bindHost, want.getServerPort());
        //添加服务开启成功失败事件
        bind.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("Server在端口:{}启动成功", want.getServerPort());
                promise.setSuccess(this);
            } else {
                log.error("Server在端口:" + want.getServerPort() + "启动失败", future.cause());
                promise.setFailure(future.cause());
            }
        });
        return promise;
    }

    public String getId() {
        return id;
    }

    public short getServerPort() {
        return want.getServerPort();
    }

    public short getLocalPort() {
        return want.getLocalPort();
    }

    public String getLocalHost() {
        return want.getLocalHost();
    }

    public Status getStatus() {
        return status;
    }

    public EventLoop getEventLoop() {
        return work.next();
    }

    //向管理chanel输出，且必须写成功,失败就关闭
    public ChannelFuture write(Object o) {
        return managerChannel.writeAndFlush(o).addListeners(ErrorLogListener.INSTANCE, ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    /*
     * 停止Server，即直接关闭manager连接，其他行为都在连接的回掉函数里处理
     * 如果因为管理连接被关闭导致的server关闭,则UnRegister此Server.如果是主动关闭Server则不需要UnRegister
     * */
    private void stop() {
        //防止多次关闭
        if (status != Status.start) {
            return;
        }
        status = Status.stopIng;
        log.info("准备关闭:{}", this);
        ServerManager.unRegister(this);
        if (managerChannel != null && managerChannel.isActive()) {
            managerChannel.close();
        }
        if (bind != null && bind.channel().isActive()) {
            bind.channel().close();
        }
        status = Status.stop;
    }

    @Override
    public String toString() {
        return "Server{" +
                "status=" + status +
                ", serverPort=" + want.getServerPort() +
                ", localHost=" + want.getLocalHost() +
                ", localPort=" + want.getLocalPort() +
                '}';
    }

    //当外网用户连接到监听的端口后，将打开与客户端的连接，并传输数据
    private static class ServerHandler extends ChannelInboundHandlerAdapter {

        private Server server;

        ServerHandler(Server server) {
            this.server = server;
        }

        @Override
        public void channelActive(ChannelHandlerContext userConnection) {
            log.debug("Server:{} 被连接，准备获取客户端连接", server.getId());
            Promise<Channel> promise = ServerManager.getConnection(userConnection.channel().eventLoop(), server);
            promise.addListener((GenericFutureListener<Future<Channel>>) future -> {
                if (future.isSuccess()) {
                    log.debug("Server:{} 获取客户端连接成功", server.getId());
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
