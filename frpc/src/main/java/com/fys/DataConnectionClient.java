package com.fys;

import com.fys.cmd.handler.*;
import com.fys.cmd.listener.ErrorLogListener;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
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
    private Channel channelToServer;
    private NeedCreateNewConnectionCmd msg;

    public DataConnectionClient(NeedCreateNewConnectionCmd msg) {
        this.msg = msg;
    }

    /*
     * 数据连接连接到服务器上时，如果不说明自己是数据连接，服务器是不会主动发数据的，所以这里改成自动读提高性能
     * */
    public Promise<DataConnectionClient> start() {
        Bootstrap b = new Bootstrap();
        Promise<DataConnectionClient> promise = new DefaultPromise<>(work.next());
        b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(serverHost, serverPort)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new CmdEncoder());
                        ch.pipeline().addLast(FlowManagerHandler.INSTANCE);
                    }
                }).connect()
                //此监听器内表示与服务器创建连接的状态
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {
                            log.debug("数据连接，连接到服务器成功，准备连接到本地 {} 端口", msg.getLocalPort());
                            channelToServer = future.channel();
                            //连接到本地需要代理的端口
                            createConnectionToLocal(msg.getLocalPort(), channelToServer.eventLoop())
                                    .addListener((ChannelFutureListener) localFuture -> {
                                        if (localFuture.isSuccess()) {
                                            log.debug("连接到本地 {} 端口成功", msg.getLocalPort());
                                            channelToServer.pipeline().addLast("linkServer", new TransactionHandler(localFuture.channel(), true));
                                            localFuture.channel().pipeline().addLast("linkLocal", new TransactionHandler(channelToServer, true));
                                            promise.setSuccess(DataConnectionClient.this);
                                        } else {
                                            log.error("连接到本地 " + msg.getLocalPort() + " 端口失败,即将关闭和服务器的连接", localFuture.cause());
                                            channelToServer.close();
                                            promise.setFailure(localFuture.cause());
                                        }
                                    });
                        } else {
                            log.error("数据连接连接失败，无法连接到服务器", future.cause());
                            promise.setFailure(future.cause());
                        }
                    }
                });
        return promise;
    }

    //通过数据连接向服务器发送数据，用于刚创建链接后向服务器发送信号标识自己是数据连接
    public void write(Object msg) {
        if (channelToServer != null) {
            channelToServer.writeAndFlush(msg).addListener(ErrorLogListener.INSTANCE);
        } else {
            log.error("channelToServer is null");
        }
    }

    //创建一个指向本地端口的连接
    private ChannelFuture createConnectionToLocal(int port, EventLoopGroup group) {
        Bootstrap b = new Bootstrap();
        return b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(msg.getLocalHost(), port)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 120));
                        ch.pipeline().addLast(ExceptionHandler.INSTANCE);
                    }
                })
                .connect();
    }


}
