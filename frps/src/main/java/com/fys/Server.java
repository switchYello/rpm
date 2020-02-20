package com.fys;

import com.fys.cmd.handler.TransactionHandler;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import com.fys.cmd.serverToClient.ServerStartFailCmd;
import com.fys.cmd.serverToClient.ServerStartSuccessCmd;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

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
    private Queue<Promise<Channel>> hasConnectionPromises = new LinkedList<>();
    private Queue<Promise<Channel>> waitConnections = new LinkedList<>();


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
    public ChannelFuture start() {
        ServerBootstrap sb = new ServerBootstrap();
        bind = sb.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_LINGER, 1)
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
                        managerChanel.writeAndFlush(new ServerStartSuccessCmd(id));
                        ServerManager.register(this);
                        log.info("服务端for:{}在端口:{}启动成功", clientName, port);
                    } else {
                        managerChanel.writeAndFlush(new ServerStartFailCmd(future.toString()));
                        log.error("服务端for:{}在端口:{}启动失败", clientName, port, future.cause());
                    }
                });
        return bind;
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
    public void addConnection(Channel dataChanel) {
        Promise<Channel> poll = waitConnections.poll();
        if (poll == null) {
            Promise<Channel> promise = new DefaultPromise<>(work.next());
            promise.setSuccess(dataChanel);
            hasConnectionPromises.add(promise);
            return;
        }
        if (poll.isCancelled()) {
            addConnection(dataChanel);
        } else {
            poll.setSuccess(dataChanel);
        }
    }

    //如果池中存在连接，则优先使用池中的，否则创建promise存入等待队列中
    public Promise<Channel> getConnection() {
        if (hasConnectionPromises.isEmpty()) {
            Promise<Channel> promise = new DefaultPromise<>(work.next());
            managerChanel.writeAndFlush(new NeedCreateNewConnectionCmd())
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            waitConnections.add(promise);
                        } else {
                            log.info("向客户端发送创建连接指令失败");
                            promise.setFailure(future.cause());
                            managerChanel.close();
                        }
                    });
            return promise;
        } else {
            return hasConnectionPromises.poll();
        }
    }

    //关闭服务端，其余需要关闭的在服务端的监听事件里处理
    public void close() {
        ServerManager.unRegister(this);
        if (bind != null && bind.channel().isActive()) {
            bind.channel().close();
        }
        if (managerChanel != null && managerChanel.isActive()) {
            managerChanel.close();
        }
        for (Iterator<Promise<Channel>> iterator = hasConnectionPromises.iterator(); iterator.hasNext(); ) {
            Promise<Channel> next = iterator.next();
            iterator.remove();
            Channel now = next.getNow();
            if (now != null && now.isActive()) {
                now.close();
            }
            next.cancel(true);
        }
        for (Iterator<Promise<Channel>> iterator = waitConnections.iterator(); iterator.hasNext(); ) {
            Promise<Channel> next = iterator.next();
            iterator.remove();
            next.cancel(true);
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
                    log.info("服务端获取对客户端失败");
                    webConnection.close();
                }
            });
        }
    }
}
