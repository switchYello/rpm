package com.fys;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/17
 * 由客户端连接到服务端，用户传输数据
 */
public class Client {

    private static EventLoopGroup work = new NioEventLoopGroup(1);
    private String serverhost;
    private int serverPort;
    private int localPort = 80;


    public Client(String serverhost, int serverPort) {
        this.serverhost = serverhost;
        this.serverPort = serverPort;
    }

    public void start() {
        Bootstrap b = new Bootstrap();
        b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(serverhost, serverPort)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ClientHandler())
                .connect();
    }

    /*
     * 数据传输handler处理逻辑
     * */
    private static class ClientHandler extends ChannelInboundHandlerAdapter {

        private static Logger log = LoggerFactory.getLogger(ClientHandler.class);
        private int localPort = 80;

        //到此方法说明clent已经连接到server了
        //在此连接到本地，然后将此连接的所有输出引入到本地连接
        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            //连接到本地需要代理的端口
            ChannelFuture connectionToLocal = createConnectionToLocal("127.0.0.1", localPort, ctx);
            //告诉服务器本连接是数据连接
            ctx.writeAndFlush(Unpooled.buffer().writeInt(1).writeByte(Cmd.dataCmd));
            //等待连接完成，转发数据
            connectionToLocal.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        ctx.pipeline().addLast(new TransactionHandler(future.channel(), false));
                        ctx.read();
                    } else {
                        log.info("连接到本地端口:{}失败", localPort);
                        ctx.close();
                    }
                }
            });
        }

        //创建一个指向本地端口的连接
        private ChannelFuture createConnectionToLocal(String host, int port, final ChannelHandlerContext ctx) {
            Bootstrap b = new Bootstrap();
            return b.group(work)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(host, port)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new TransactionHandler(ctx.channel(), true))
                    .connect();
        }

    }


}
