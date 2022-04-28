package com.fys;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author hcy
 * @since 2022/4/28 23:24
 */
public class EchoServer {

    public static void main(String[] args) {
        ServerBootstrap sb = new ServerBootstrap();
        ChannelFuture bind = sb.group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                ByteBuf slice = buf.slice();
                                int size = slice.readableBytes();
                                for (int i = 0; i < size; i++) {
                                    byte c = slice.readByte();
                                    System.out.println((char) c);
                                    if ('e' == c) {
                                        ctx.writeAndFlush(Unpooled.wrappedBuffer("\n~bye".getBytes()));
                                        ctx.close();
                                    }
                                }
                                ctx.writeAndFlush(msg);
                            }
                        });
                    }
                })
                .bind("0.0.0.0", 8880);
        bind.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("echo 服务启动成功");
                } else {
                    future.cause().printStackTrace();
                }
            }
        });
    }

}
