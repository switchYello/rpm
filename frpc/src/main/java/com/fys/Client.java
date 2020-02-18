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
public class Client {

    private static EventLoopGroup work = AppClient.work;
    private String serverhost = Config.serverHost;
    private int serverPort = Config.serverPort;
    private String serverId;

    public Client(String serverId) {
        this.serverId = serverId;
    }

    public void start() {
        Bootstrap b = new Bootstrap();
        b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(serverhost, serverPort)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ClientInit(this))
                .connect();
    }

    private static class ClientInit extends ChannelInitializer<Channel> {
        private Client client;

        public ClientInit(Client client) {
            this.client = client;
        }

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new CmdEncoder());
            ch.pipeline().addLast(new ClientHandler(client));
        }
    }

    /*
     * 数据传输handler处理逻辑
     * */
    private static class ClientHandler extends ChannelInboundHandlerAdapter {

        private static Logger log = LoggerFactory.getLogger(ClientHandler.class);
        private final int localPort = Config.localPort;
        private Client client;

        public ClientHandler(Client client) {
            this.client = client;
        }

        //到此方法说明clent已经连接到server了
        //在此连接到本地，然后将此连接的所有输出引入到本地连接
        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            //连接到本地需要代理的端口
            ChannelFuture connectionToLocal = createConnectionToLocal("127.0.0.1", localPort, ctx);
            //告诉服务器本连接是数据连接
            ctx.writeAndFlush(new DataConnection(client.serverId));
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
