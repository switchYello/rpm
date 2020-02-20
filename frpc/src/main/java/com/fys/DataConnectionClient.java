package com.fys;

import com.fys.cmd.clientToServer.DataConnection;
import com.fys.cmd.handler.CmdEncoder;
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
    private String serverId;

    public DataConnectionClient(String serverId) {
        this.serverId = serverId;
    }

    public void start() {
        Bootstrap b = new Bootstrap();
        b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(serverHost, serverPort)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new CmdEncoder());
//                        ch.pipeline().addLast(new DataConnectionHandler(DataConnectionClient.this));
                    }
                })
                .connect()
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
                                        //告诉服务器本连接是数据连接
                                        connectionToServer.writeAndFlush(new DataConnection(serverId));
                                        connectionToServer.read();
                                    } else {
                                        log.info("连接到本地端口:{}失败", Config.localPort);
                                        connectionToServer.close();
                                    }
                                }
                            });


                        } else {
                            log.error("数据连接连接失败", future.cause());
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
                .handler(IgnoreHandler.INSTANCE)
                .connect();
    }

    @ChannelHandler.Sharable
    private static class IgnoreHandler extends ChannelInboundHandlerAdapter {
        private static IgnoreHandler INSTANCE = new IgnoreHandler();
    }

//
//    /*
//     * 数据传输handler处理逻辑
//     * */
//    private static class DataConnectionHandler extends ChannelInboundHandlerAdapter {
//
//        private static Logger log = LoggerFactory.getLogger(DataConnectionHandler.class);
//        private final int localPort = Config.localPort;
//        private DataConnectionClient dataConnection;
//
//
//
//
//        DataConnectionHandler(DataConnectionClient dataConnection) {
//            this.dataConnection = dataConnection;
//        }
//
//        //到此方法说明clent已经连接到server了
//        //在此连接到本地，然后将此连接的所有输出引入到本地连接
//        @Override
//        public void channelActive(final ChannelHandlerContext ctx) {
//            //连接到本地需要代理的端口
//            ChannelFuture connectionToLocal = createConnectionToLocal(localPort);
//            connectionToLocal.addListener(new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    if (future.isSuccess()) {
//                        ctx.pipeline().addLast("linkServer", new TransactionHandler(future.channel(), false));
//                        future.channel().pipeline().addLast("linkLocal", new TransactionHandler(ctx.channel(), true));
//                        //告诉服务器本连接是数据连接
//                        ctx.writeAndFlush(new DataConnection(dataConnection.serverId));
//                        ctx.read();
//                    } else {
//                        log.info("连接到本地端口:{}失败", localPort);
//                        ctx.close();
//                    }
//                }
//            });
//        }
//
//
//    }
//

}
