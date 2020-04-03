package com.fys;

import com.fys.cmd.handler.*;
import com.fys.cmd.listener.ErrorLogListener;
import com.fys.cmd.message.DataConnectionCmd;
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
    private Config config;
    private DataConnectionCmd msg;

    public DataConnectionClient(Config config, DataConnectionCmd msg) {
        this.config = config;
        this.msg = msg;
    }

    /*
     * 数据连接连接到服务器上时，如果不说明自己是数据连接，服务器是不会主动发数据的，所以这里改成自动读提高性能
     * */
    public void start() {
        //连接到Local端口成功后，尝试连接到服务器
        createConnectionToLocal(msg.getLocalHost(), msg.getLocalPort()).addListener((ChannelFutureListener) localFuture -> {
            if (!localFuture.isSuccess()) {
                log.error("Client连接到Local失败", localFuture.cause());
                return;
            }
            final Channel channelToLocal = localFuture.channel();

            createConnectionToServer(config.getServerIp(), config.getServerPort()).addListener((ChannelFutureListener) serverFuture -> {
                if (serverFuture.isSuccess()) {
                    Channel channelToServer = serverFuture.channel();
                    channelToServer.pipeline().addBefore(ExceptionHandler.NAME, "linkServer", new TransactionHandler(channelToLocal, true));
                    channelToLocal.pipeline().addBefore(ExceptionHandler.NAME, "linkLocal", new TransactionHandler(channelToServer, true));
                    //发送认证消息
                    channelToServer.writeAndFlush(msg).addListeners(ErrorLogListener.INSTANCE, ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    log.error("Client连接到Remote失败", serverFuture.cause());
                    channelToLocal.close();
                }
            });
        });
    }

    //创建一个指向本地端口的连接
    private ChannelFuture createConnectionToLocal(String host, int port) {
        return newClientBootStrap(work)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 180));
                        ch.pipeline().addLast(ExceptionHandler.NAME, ExceptionHandler.INSTANCE);
                    }
                })
                .connect(host, port);
    }

    //创建一个指向服务器端口的连接
    //客户端连接服务器不加超时管理，因为服务器会判断超时主动断开
    private ChannelFuture createConnectionToServer(String host, int port) {
        return newClientBootStrap(work)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new Rc4Md5Handler(config.getToken()));
                        ch.pipeline().addLast(new CmdEncoder());
                        ch.pipeline().addLast(ExceptionHandler.NAME, ExceptionHandler.INSTANCE);
                    }
                })
                .connect(host, port);
    }

    private static Bootstrap newClientBootStrap(EventLoopGroup group) {
        return new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.TCP_NODELAY, true);
    }


}
