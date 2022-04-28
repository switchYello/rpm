package com.fys.connection;

import com.fys.InnerConnectionFactory;
import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.TransactionHandler;
import com.fys.cmd.message.DataConnectionCmd;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.GenericProgressiveFutureListener;
import io.netty.util.concurrent.ProgressiveFuture;
import io.netty.util.concurrent.ProgressivePromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

/**
 * @author hcy
 * @since 2022/4/28 0:36
 */
public class DataConnection {

    private static final Logger log = LoggerFactory.getLogger(DataConnection.class);
    private final String localHost;
    private final int localPort;
    private final String serverHost;
    private final int serverPort;
    private final DataConnectionCmd msg;

    public DataConnection(String localHost, int localPort, String serverHost, int serverPort, DataConnectionCmd msg) {
        this.localHost = localHost;
        this.localPort = localPort;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.msg = msg;
    }

    public void startConnection() {
        ChannelFuture channelToLocal = InnerConnectionFactory.createChannel(localHost, localPort, true);
        ChannelFuture channelToService = InnerConnectionFactory.createChannel(serverHost, serverPort, false);
        ProgressivePromise<Void> promise = InnerConnectionFactory.createProgressivePromise();
        promise.addListener(new GenericProgressiveFutureListener<ProgressiveFuture<Void>>() {

            @Override
            public void operationComplete(ProgressiveFuture<Void> future) {
                if (!future.isSuccess()) {
                    channelToLocal.cancel(false);
                    channelToLocal.channel().close();
                    channelToService.cancel(false);
                    channelToService.channel().close();
                }
            }

            @Override
            public void operationProgressed(ProgressiveFuture<Void> future, long progress, long total) {
                if (progress == total) {
                    Channel c1 = channelToLocal.channel();
                    Channel c2 = channelToService.channel();

                    //local -> server
                    c1.pipeline().addLast(new TransactionHandler(c2, true));

                    //server -> local
                    c2.pipeline().addLast(new CmdEncoder());
                    c2.pipeline().addLast(new TransactionHandler(c1, true));

                    //发送认证消息
                    c2.writeAndFlush(msg).addListeners((ChannelFutureListener) f -> {
                                if (!f.isSuccess()) {
                                    log.error("数据连接认证失败", future.cause());
                                    return;
                                }
                                c2.config().setAutoRead(true);
                            }
                            , CLOSE_ON_FAILURE);

                }
            }
        });

        AtomicInteger successCount = new AtomicInteger(0);
        channelToLocal.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    promise.tryProgress(successCount.incrementAndGet(), 2);
                } else {
                    log.error("连接到Local失败", future.cause());
                    promise.setFailure(future.cause());
                }
            }
        });
        channelToService.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    promise.tryProgress(successCount.incrementAndGet(), 2);
                } else {
                    log.error("连接到Remote失败", future.cause());
                    promise.setFailure(future.cause());
                }
            }
        });


/*        InnerConnectionFactory.createChannel(localHost, localPort, true).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture localFuture) {
                if (!localFuture.isSuccess()) {
                    log.error("连接到Local失败", localFuture.cause());
                    return;
                }

                final Channel channelToLocal = localFuture.channel();
                log.debug("连接到Local成功 [{} -> {}]", channelToLocal.localAddress(), channelToLocal.remoteAddress());

                InnerConnectionFactory.createChannel(serverHost, serverPort, true).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture serverFuture) {
                        if (!serverFuture.isSuccess()) {
                            log.error("连接到Remote失败", serverFuture.cause());
                            channelToLocal.close();
                        }

                        Channel channelToServer = serverFuture.channel();
                        log.debug("连接到Server成功 [{} -> {}]", channelToLocal.localAddress(), channelToLocal.remoteAddress());

                        //local -> server
                        channelToLocal.pipeline().addLast(new TransactionHandler(channelToServer, true));

                        //server -> local
                        channelToServer.pipeline().addLast(new CmdEncoder());
                        channelToServer.pipeline().addLast(new TransactionHandler(channelToLocal, true));

                        //发送认证消息
                        channelToServer.writeAndFlush(msg)
                                .addListeners((ChannelFutureListener) future -> {
                                    if (!future.isSuccess()) {
                                        log.error("数据连接认证失败", future.cause());
                                    }
                                }, CLOSE_ON_FAILURE);

                    }
                });
            }
        });*/
    }


}
