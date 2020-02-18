package com.fys;

import com.fys.handler.TransactionHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

/**
 * 客户端要求监听某一个端口后创建并启动此类
 * 接收外接连接传送到客户端
 * 因此此类不会autoRead
 * hcy 2020/2/17
 */
public class Server {

    private static Logger log = LoggerFactory.getLogger(Server.class);
    private String id = UUID.randomUUID().toString();
    private EventLoopGroup boss = App.boss;
    private EventLoopGroup work = App.work;

    //监听的地址和端口
    private String host;
    private int port;
    private ChannelFuture bind;


    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        ServerBootstrap sb = new ServerBootstrap();
        bind = sb.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(ServerHandler.INSTANCE)
                .bind(host, port)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("服务端在端口" + port + "启动成功");
                    } else {
                        log.error("服务端在端口:" + port + "启动失败", future.cause());
                    }
                });
    }

    public void stop() {
        if (bind != null) {
            bind.channel().close();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server server = (Server) o;
        return Objects.equals(id, server.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //当外网用户连接到监听的端口后，将打开与客户端的连接，并传输数据
    @ChannelHandler.Sharable
    private static class ServerHandler extends ChannelInboundHandlerAdapter {

        private static ServerHandler INSTANCE = new ServerHandler();

        @Override
        public void channelActive(ChannelHandlerContext webConnection) {
            Promise<ChannelHandlerContext> promise = ConnectionPool.getConnection(Server.this);
            promise.addListener((GenericFutureListener<Future<ChannelHandlerContext>>) future -> {
                if (future.isSuccess()) {
                    ChannelHandlerContext clientConnection = future.getNow();
                    clientConnection.pipeline().addLast(new TransactionHandler(webConnection.channel(), true));
                    webConnection.pipeline().addLast(new TransactionHandler(clientConnection.channel(), false));
                    webConnection.read();
                } else {
                    log.info("服务端获取对客户端失败");
                    webConnection.close();
                }
            });
        }
    }
}
