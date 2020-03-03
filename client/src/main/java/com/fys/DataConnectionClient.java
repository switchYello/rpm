package com.fys;

import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.handler.TransactionHandler;
import com.fys.cmd.listener.ErrorLogListener;
import com.fys.cmd.message.DataConnectionCmd;
import com.fys.conf.ServerInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Promise;

/**
 * hcy 2020/2/17
 * 由客户端连接到服务端，用户传输数据
 */
public class DataConnectionClient {

    private static EventLoopGroup work = GuiStart.work;
    private Channel channelToServer;
    private DataConnectionCmd msg;
    private ServerInfo serverInfo;

    public DataConnectionClient(ServerInfo serverInfo, DataConnectionCmd msg) {
        this.msg = msg;
        this.serverInfo = serverInfo;
    }

    /*
     * 数据连接连接到服务器上时，如果不说明自己是数据连接，服务器是不会主动发数据的，所以这里改成自动读提高性能
     * */
    public void start(Promise<DataConnectionClient> promise) {
        //连接到Local端口成功后，尝试连接到服务器
        createConnectionToLocal(msg.getLocalHost(), msg.getLocalPort()).addListener((ChannelFutureListener) localFuture -> {
            if (localFuture.isSuccess()) {
                final Channel channelToLocal = localFuture.channel();
                createConnectionToServer(serverInfo.getServerIp(), serverInfo.getServerPort()).addListener((ChannelFutureListener) serverFuture -> {
                    if (serverFuture.isSuccess()) {
                        this.channelToServer = serverFuture.channel();
                        channelToServer.pipeline().addBefore(ExceptionHandler.NAME, "linkServer", new TransactionHandler(channelToLocal, true));
                        channelToLocal.pipeline().addBefore(ExceptionHandler.NAME, "linkLocal", new TransactionHandler(channelToServer, true));
                        promise.setSuccess(DataConnectionClient.this);
                    } else {
                        channelToLocal.close();
                        promise.setFailure(serverFuture.cause());
                    }
                });
            } else {
                promise.setFailure(localFuture.cause());
            }
        });
    }

    //通过数据连接向服务器发送数据，用于刚创建链接后向服务器发送信号标识自己是数据连接
    //调用此方法要保证client的连接是创建完成的
    public void write(Object msg) {
        if (channelToServer != null) {
            channelToServer.writeAndFlush(msg).addListeners(ErrorLogListener.INSTANCE, ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            throw new IllegalStateException("channelToServer尚未完成，不能发送信息");
        }
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
    private static ChannelFuture createConnectionToServer(String host, int port) {
        return newClientBootStrap(work)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
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
