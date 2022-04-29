package com.fys.server;

import com.fys.ClientManager;
import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.handler.TransactionHandler;
import com.fys.conf.ClientInfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/4/28 15:15
 */
public class DataServer {

    private static final Logger log = LoggerFactory.getLogger(DataServer.class);

    EventLoopGroup boss;
    ClientManager clientManager;
    ClientInfo clientInfo;

    public DataServer(EventLoopGroup boss, ClientManager clientManager, ClientInfo clientInfo) {
        this.boss = boss;
        this.clientManager = clientManager;
        this.clientInfo = clientInfo;
    }

    public void start() {
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(boss, boss)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new CmdEncoder());
                        //控制超时，防止链接上来但不发送消息任何的连接
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 300));
                        ch.pipeline().addLast(new DataHandler());
                    }
                })
                .bind(clientInfo.getServerHost(), clientInfo.getServerPort())
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("服务端在端口:{}启动成功", clientInfo.getServerPort());
                    } else {
                        log.error("服务端在端口:" + clientInfo.getServerPort() + "启动失败", future.cause());
                        boss.shutdownGracefully();
                    }
                });
    }

    private class DataHandler extends ChannelInboundHandlerAdapter {
        /**
         * 用户连接创建后，开始与本地连接沟通
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.debug("收到客户请求，准备获取客户端连接");
            //获取本地连接
            Promise<Channel> targetPromise = clientManager.getTargetChannel(clientInfo, ctx.executor());
            //获取过程中，如果用户端关闭，则取消获取
            Channel channelToUser = ctx.channel();
            channelToUser.closeFuture().addListener((ChannelFutureListener) future -> targetPromise.cancel(false));
            //当获取成功后，关联两条数据流。如获取失败，关闭客户端流
            targetPromise.addListener(new GenericFutureListener<Future<Channel>>() {
                @Override
                public void operationComplete(Future<Channel> future) {
                    if (future.isSuccess()) {
                        Channel channelToClient = future.getNow();
                        channelToClient.pipeline().addLast(new TransactionHandler(channelToUser, true));
                        channelToUser.pipeline().addLast(new TransactionHandler(channelToClient, true));
                        channelToUser.config().setAutoRead(true);
                    } else {
                        log.error("获取客户端连接失败", future.cause());
                        ctx.close();
                    }
                }
            });
            super.channelActive(ctx);
        }
    }


}
