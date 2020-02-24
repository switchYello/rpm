package com.fys;

import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.handler.FlowManagerHandler;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.handler.TransactionHandler;
import com.fys.cmd.listener.ErrorLogListener;
import com.fys.cmd.message.clientToServer.WantManagerCmd;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 客户端要求监听某一个端口后创建并启动此类
 * 接收外接连接传送到客户端
 * 因此此类不会autoRead
 * hcy 2020/2/17
 */
public class Server {
    /*
     * 刚创建完成是init状态，启动完成后转成start，
     * 如果managerChannel关闭了，则pause一段时间等待复用（一段时间后没有再次请求则关闭）
     * stop服务一被标记终止
     *
     * */
    public enum Status {
        start, pause, stop
    }

    private static Logger log = LoggerFactory.getLogger(Server.class);
    private EventLoopGroup boss = App.boss;
    private EventLoopGroup work = App.work;
    private volatile Status status = Status.start;

    private String id;
    private ChannelFuture bind;
    private WantManagerCmd want;
    private Channel managerChannel;
    //一个Server内的连接共用一个流量统计
    private FlowManagerHandler flowManagerHandler;

    //定时停止server
    private ScheduledFuture<?> stopSchedule;

    public Server(WantManagerCmd wantManagerCmd, Channel managerChannel) {
        this.want = wantManagerCmd;
        this.managerChannel = managerChannel;
        this.id = "[SID " + want.getServerPort() + " -> " + want.getLocalHost() + ":" + want.getLocalPort() + "]";
        this.flowManagerHandler = new FlowManagerHandler(id);
        //服务启动成功，为管理添加关闭事件，关闭连接时同时暂停Server
        managerChannel.closeFuture().addListener((ChannelFutureListener) managerFuture -> {
            pause();
        });
    }

    //开启服务监听客户端要求的端口，等待用户连接但不自动读数据
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
                }).bind(Config.bindHost, want.getServerPort());
        //添加服务开启成功失败事件
        bind.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                promise.setSuccess(this);
            } else {
                promise.setFailure(future.cause());
            }
        });
    }

    public String getId() {
        return id;
    }

    public int getServerPort() {
        return want.getServerPort();
    }

    public int getLocalPort() {
        return want.getLocalPort();
    }

    public String getLocalHost() {
        return want.getLocalHost();
    }


    //向管理chanel输出，且必须写成功,失败就关闭
    public ChannelFuture write(Object o) {
        if (managerChannel != null) {
            return managerChannel.writeAndFlush(o).addListeners(ErrorLogListener.INSTANCE, ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            throw new IllegalStateException("Server无法发送信息，因为ManagerChannel is null");
        }
    }

    /*
     * 重新将服务标记成启动状态
     * */
    public void reStart(WantManagerCmd wantManagerCmd, Channel managerChannel, Promise<Server> promise) {
        stopSchedule.cancel(true);
        if (this.status != Status.pause) {
            promise.setFailure(new IllegalStateException("当前server:" + this.getId() + "不处于pause状态,不能重启"));
            return;
        }
        this.want = wantManagerCmd;
        this.managerChannel = managerChannel;
        this.id = "[SID " + want.getServerPort() + " -> " + want.getLocalHost() + ":" + want.getLocalPort() + "]";
        //服务启动成功，为管理添加关闭事件，关闭连接时同时暂停Server
        managerChannel.closeFuture().addListener((ChannelFutureListener) managerFuture -> {
            pause();
        });
        this.status = Status.start;
        promise.setSuccess(this);
    }

    /*
     * 管理连接断了，暂停对外服务
     * 一定时间后如果扔是pause则停止服务（因为可能会被重连）
     * 执行pause操作前面判断状态使用managerChannel的eventLoop
     * 后面添加到ServerMnager操作使用ServerManager的EventLoop操作
     * */
    private void pause() {
        if (this.status != Status.start) {
            return;
        }
        this.status = Status.pause;
        ServerManager.pauseServer(this);
        stopSchedule = ServerManager.schedule(() -> {
            if (this.status == Status.pause) {
                stop();
            }
        }, 30, TimeUnit.SECONDS);
    }

    /*
     * pause内设置的定时任务触发后回掉此函数，设置定时任务时使用的是ServerManager的EventLoop,
     * 所以此函数一定会使用ServerManager的EventLoop执行
     * */
    private void stop() {
        log.info("准备关闭:{}", this);
        //防止多次关闭
        if (status == Status.stop) {
            log.info("关闭{}失败，已经处于关闭状态", this);
            return;
        }
        this.status = Status.stop;
        ServerManager.stopServer(this);
        if (bind != null && bind.channel().isActive()) {
            bind.channel().close().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("关闭服务:{}成功", id);
                } else {
                    log.info("关闭服务:" + id + "失败，因为", future.cause());
                }
            });
        }
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Server{" +
                "id='" + id + '\'' +
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

            Status currentStatus = server.getStatus();
            if (currentStatus != Status.start) {
                log.info("Server:{} 状态是 {} 状态，不准接受连接", server.getId(), currentStatus);
                userConnection.close();
                return;
            }

            log.debug("Server:{} 被连接，准备获取客户端连接", server.getId());
            Promise<Channel> promise = ServerManager.getConnection(server);
            promise.addListener((GenericFutureListener<Future<Channel>>) future -> {
                if (future.isSuccess()) {
                    Channel clientChannel = future.getNow();
                    clientChannel.pipeline().addLast("linkClient", new TransactionHandler(userConnection.channel(), true));
                    clientChannel.pipeline().addLast(ExceptionHandler.INSTANCE);
                    userConnection.pipeline().replace(this, "linkUser", new TransactionHandler(clientChannel, false));
                    userConnection.read();
                } else {
                    //promise失败可能是：超时没结果被定时任务取消的
                    log.error("服务端获取对客户端的连接失败,关闭userConnection", future.cause());
                    userConnection.close();
                }
            });
        }
    }
}
