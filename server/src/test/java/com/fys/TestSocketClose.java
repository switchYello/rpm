package com.fys;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * hcy 2020/2/23
 */
public class TestSocketClose {

    @Test
    public void testClose() throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        ServerBootstrap sb = new ServerBootstrap();
        ChannelFuture bind = sb.group(boss, boss)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInboundHandlerAdapter())
                .bind(Config.bindHost, 35647)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        System.out.println("ServerSocketChannel启动成功");

                        //启动成功就关闭channel
                        future.channel().close().addListener((ChannelFutureListener) future1 -> {
                            if (!future1.isSuccess()) {
                                Assert.fail(future1.cause().toString());
                            }
                        });
                    } else {
                        System.out.println("ServerSocketChannel启动失败");
                    }
                });

        bind.channel().closeFuture().addListener((ChannelFutureListener) future -> {
            //断言一定关闭成功
            if (!future.isSuccess()) {
                Assert.fail(future.cause().toString());
            }
            Assert.assertTrue("Channel没有真的被关闭", !future.channel().isActive());
            System.out.println("服务关闭成功");
            boss.shutdownGracefully();
            boss.shutdownGracefully();

        });
        TimeUnit.MILLISECONDS.sleep(500);
    }


}
