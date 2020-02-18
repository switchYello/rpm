package com.fys;

import com.fys.cmd.handler.CmdEncoder;
import com.fys.handler.FrpsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class App {

    private static Logger log = LoggerFactory.getLogger(App.class);
    public static EventLoopGroup boss = new NioEventLoopGroup(1);
    public static EventLoopGroup work = new NioEventLoopGroup(1);


    public static void main(String[] args) throws IOException {
        Config.init();

        ServerBootstrap sb = new ServerBootstrap();
        sb.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_LINGER, 1)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new CmdEncoder());
                        ch.pipeline().addLast(new FrpsHandler());
                    }
                })
                .bind(Config.bindHost, Config.bindPort)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("服务端在端口:{}启动成功", Config.bindPort);
                    } else {
                        log.error("服务端在端口:" + Config.bindPort + "启动失败", future.cause());
                    }
                });
    }
}
