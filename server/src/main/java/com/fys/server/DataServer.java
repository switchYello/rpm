package com.fys.server;

import com.fys.ClientManager;
import com.fys.cmd.handler.ErrorLogHandler;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.util.EventLoops;
import com.fys.conf.ClientInfo;
import com.fys.connection.DataConnection;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
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

    private ClientManager clientManager;
    private ClientInfo clientInfo;

    public DataServer(ClientManager clientManager, ClientInfo clientInfo) {
        this.clientManager = clientManager;
        this.clientInfo = clientInfo;
    }

    public void start() {
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(EventLoops.BOSS, EventLoops.WORKER)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        //控制超时，防止链接上来但不发送消息任何的连接
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 300));
                        ch.pipeline().addLast(new DataHandler());
                        ch.pipeline().addLast(new ErrorLogHandler());
                    }
                })
                .bind(clientInfo.getServerHost(), clientInfo.getServerPort())
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("数据端在端口:{} 启动成功", clientInfo.getServerPort());
                    } else {
                        log.error("数据端在端口:" + clientInfo.getServerPort() + "启动失败", future.cause());
                    }
                });
    }

    private class DataHandler extends SimpleChannelInboundHandler<ByteBuf> {

        private DataConnection target;

        /**
         * 用户连接创建后，开始与本地连接沟通
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.debug("收到客户请求，准备获取客户端连接");
            //获取本地连接
            Promise<DataConnection> targetPromise = clientManager.getTargetChannel(clientInfo);

            Channel channelToUser = ctx.channel();
            //获取过程中，如果用户端关闭，则取消获取
            channelToUser.closeFuture().addListener((ChannelFutureListener) future -> targetPromise.cancel(false));
            targetPromise.addListener(new GenericFutureListener<Future<DataConnection>>() {
                @Override
                public void operationComplete(Future<DataConnection> future) {
                    if (future.isSuccess()) {
                        target = future.getNow();
                        target.bindToChannel(channelToUser);
                        target.startTransaction();
                        channelToUser.config().setAutoRead(true);
                    } else {
                        log.error("获取客户端连接失败", future.cause());
                        ctx.close();
                    }
                }
            });
            super.channelActive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            target.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (target != null) {
                target.close();
            }
            super.channelInactive(ctx);
        }
    }


}
