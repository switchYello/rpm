package com.fys;

import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.FlowManagerHandler;
import com.fys.cmd.handler.TransactionHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/17
 * 由客户端连接到服务端，用户传输数据
 */
public class DataConnectionClient {

    private static Logger log = LoggerFactory.getLogger(DataConnectionClient.class);
    private static EventLoopGroup work = AppClient.work;
    private String serverHost = Config.serverHost;
    private int serverPort = Config.serverPort;

    public ChannelFuture start() {
        Bootstrap b = new Bootstrap();
        return b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(serverHost, serverPort)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new CmdEncoder());
                        ch.pipeline().addLast(FlowManagerHandler.INSTANCE);
                    }
                })
                .connect()
                //此监听器内表示与服务器创建连接的状态
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            final Channel connectionToServer = future.channel();
                            //连接到本地需要代理的端口
                            final ChannelFuture connectionToLocal = createConnectionToLocal(Config.localPort);
                            connectionToLocal.addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    if (future.isSuccess()) {
                                        connectionToServer.pipeline().addLast("linkServer", new TransactionHandler(future.channel(), false));
                                        future.channel().pipeline().addLast("linkLocal", new TransactionHandler(connectionToServer, true));
                                        connectionToServer.read();
                                    } else {
                                        log.info("连接到本地端口:{}失败,关闭和服务器的连接", Config.localPort);
                                        connectionToServer.close();
                                    }
                                }
                            });
                        } else {
                            log.error("数据连接连接失败，无法连接到服务器", future.cause());
                        }
                    }
                });
    }


    //创建一个指向本地端口的连接
    private ChannelFuture createConnectionToLocal(int port) {
        Bootstrap b = new Bootstrap();
        return b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress("0.0.0.0", port)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .handler(IgnoreHandler.INSTANCE)
                .connect();
    }

    @ChannelHandler.Sharable
    private static class IgnoreHandler extends ChannelInboundHandlerAdapter {
        private static IgnoreHandler INSTANCE = new IgnoreHandler();
    }

}
