package com.fys;

import com.fys.handler.FrpsInit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static Logger log = LoggerFactory.getLogger(App.class);
    public static EventLoopGroup boss = new NioEventLoopGroup(1);
    public static EventLoopGroup work = new NioEventLoopGroup(1);

    private static int port = 9050;

    public static void main(String[] args) {

        ServerBootstrap sb = new ServerBootstrap();
        sb.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new FrpsInit());
        ChannelFuture bind = sb.bind("127.0.0.1", port);
        bind.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    log.info("服务端在端口:{}启动成功", port);
                }
            }
        });
    }
}
