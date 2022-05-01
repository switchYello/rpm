package com.fys;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultProgressivePromise;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.ProgressivePromise;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author hcy
 * @since 2022/4/24 0:44
 */
public class InnerConnectionFactory {

    public static EventLoopGroup work = new NioEventLoopGroup(1);
    private final static Logger log = LoggerFactory.getLogger(InnerConnectionFactory.class);

    public static <T> Promise<T> createPromise() {
        return new DefaultPromise<>(work.next());
    }

    public static <T> ProgressivePromise<T> createProgressivePromise() {
        return new DefaultProgressivePromise<>(work.next());
    }

    public static ChannelFuture createChannel(String host, int port, boolean autoRead) {
        Bootstrap b = new Bootstrap();
        return b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_READ, autoRead)
                .handler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
//                                 ch.pipeline().addLast(new Rc4Md5Handler(config.getToken()));
//                                 ch.pipeline().addLast(new CmdEncoder());
//                                 ch.pipeline().addLast(new CmdDecoder());

                             }
                         }
                )
                .connect()
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        Channel channel = future.channel();
                        InetSocketAddress local = (InetSocketAddress) channel.localAddress();
                        SocketAddress remote = channel.remoteAddress();
                        log.debug("连接创建完成[{} -> {}]", local, remote);
                    } else {
                        log.debug("连接创建失败 to [{}:{}]", host, port,future.cause());
                    }
                });
    }

}
