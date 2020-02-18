package com.fys;

import com.fys.cmd.handler.TransactionHandler;
import com.fys.cmd.serverToClient.NeedNewConnectionCmd;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private int port;
    private ChannelFuture bind;
    private Channel managerChanel;
    private String clientName;
    private Queue<Promise<Channel>> hasConnectionPromises = new LinkedList<>();
    private Queue<Promise<Channel>> waitConnections = new LinkedList<>();


    public Server(int port, Channel managerChanel, String clientName) {
        this.port = port;
        this.clientName = clientName;
        this.managerChanel = managerChanel;
    }

    //开启服务监听客户端要求的端口，等待用户连接但不自动读
    public void start() {
        ServerBootstrap sb = new ServerBootstrap();
        bind = sb.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new ServerHandler(Server.this));
                    }
                })
                .bind("127.0.0.1", port)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("服务端for:{}在端口:{}启动成功", clientName, port);
                    } else {
                        log.error("服务端for:{}在端口:{}启动失败", clientName, port, future.cause());
                    }
                });
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


    public void close() {
        if (bind != null) {
            bind.channel().close();
        }
        managerChanel.close();
        hasConnectionPromises.forEach(c -> c.cancel(true));
        waitConnections.forEach(c -> c.cancel(true));
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
            managerChanel.writeAndFlush(new NeedNewConnectionCmd(id))
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            waitConnections.add(promise);
                        } else {
                            log.info("向客户端发送创建连接指令失败");
                            promise.setFailure(future.cause());
                        }
                    });
            return promise;
        } else {
            return hasConnectionPromises.poll();
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
