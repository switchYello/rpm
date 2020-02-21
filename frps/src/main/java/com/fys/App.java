package com.fys;

import com.fys.cmd.handler.CmdEncoder;
import com.fys.handler.ServerCmdDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class App {

    private static Logger log = LoggerFactory.getLogger(App.class);
    public static EventLoopGroup boss = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
    public static EventLoopGroup work = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
    public static Class<? extends ServerChannel> serverClass = Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    public static Class<? extends Channel> workClass = Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;

    /*
     * -p=9090
     * */
    public static void main(String[] args) throws IOException {
        log.info(Arrays.toString(args));
        String confPath = null;
        for (String arg : args) {
            String[] split = arg.split("=");
            assertTrue(split.length == 2, "参数不正确，无法解析:" + arg);
            if ("-c".equals(split[0])) {
                confPath = split[1];
            }
        }
        Config.init(confPath);

        ServerBootstrap sb = new ServerBootstrap();
        sb.group(boss, work)
                .channel(serverClass)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new CmdEncoder());
                        ch.pipeline().addLast(new ServerCmdDecoder());
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

    private static void assertTrue(boolean c, String message) {
        if (!c) {
            throw new RuntimeException(message);
        }
    }

}
