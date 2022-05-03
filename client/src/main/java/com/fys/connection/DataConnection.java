package com.fys.connection;

import com.fys.InnerConnectionFactory;
import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.NeedDataConnectionCmd;
import com.fys.cmd.message.NewDataConnectionCmd;
import com.fys.cmd.message.RawDataCmd;
import com.fys.cmd.message.StartTransactionCmd;
import com.fys.handler.CmdDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final NeedDataConnectionCmd msg;

    public DataConnection(String localHost, int localPort, String serverHost, int serverPort, NeedDataConnectionCmd msg) {
        this.localHost = localHost;
        this.localPort = localPort;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.msg = msg;
    }

    ConnectionToLocal localConnection;
    ConnectionToService serviceConnection;

    //1.连接客户端和服务端
    public void startConnection() {
        localConnection = new ConnectionToLocal();
        serviceConnection = new ConnectionToService();
        localConnection.bindToService(serviceConnection);
    }

    //2.客户端连接成功后，才开始bind服务端
    private void onClientActive(ConnectionToLocal local) {
        serviceConnection.bindToLocal(local);
    }

    //3.服务器连接成功后，发送指令
    private void onServiceActive(ConnectionToService service) {
        NewDataConnectionCmd cmd = new NewDataConnectionCmd(msg.getSessionId());
        service.writeAndFlush(cmd);
    }

    private void onClientRead(ByteBuf msg, ConnectionToLocal local, ConnectionToService service) {
        RawDataCmd rawDataCmd = new RawDataCmd(msg);
        service.writeAndFlush(rawDataCmd);
    }

    private void onServiceRead(Object msg, ConnectionToLocal local, ConnectionToService service) {
        if (msg instanceof StartTransactionCmd) {
            local.startRead();
        } else if (msg instanceof RawDataCmd) {
            local.writeAndFlush(((RawDataCmd) msg).getContent());
        }
    }

    private class ConnectionToLocal {

        private ChannelFuture channelToLocal;
        private Channel channel;
        private ChannelPipeline pipeline;

        public ConnectionToLocal() {
            this.channelToLocal = InnerConnectionFactory.createChannel(localHost, localPort, false);
            this.channel = channelToLocal.channel();
            this.pipeline = channel.pipeline();
        }

        public void bindToService(ConnectionToService service) {
            channel.closeFuture().addListener((ChannelFutureListener) future -> service.close());
            channelToLocal.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                onClientRead(msg, ConnectionToLocal.this, service);
                            }
                        });
                        onClientActive(ConnectionToLocal.this);
                    } else {
                        log.info("连接到local失败 - {}", channel);
                    }
                }
            });
        }

        public void startRead() {
            channelToLocal.channel().config().setAutoRead(true);
        }

        public ChannelFuture writeAndFlush(ByteBuf msg) {
            return channel.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }

        public ChannelFuture close() {
            channelToLocal.cancel(false);
            return channel.close();
        }

    }


    private class ConnectionToService {

        private ChannelFuture channelToService;
        private Channel channel;
        private ChannelPipeline pipeline;

        public ConnectionToService() {
            this.channelToService = InnerConnectionFactory.createChannel(serverHost, serverPort, true);
            this.channel = channelToService.channel();
            this.pipeline = channel.pipeline();
        }

        public void bindToLocal(ConnectionToLocal local) {
            channel.closeFuture().addListener((ChannelFutureListener) future -> local.close());
            channelToService.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        pipeline.addLast(new CmdEncoder()); //编码器
                        pipeline.addLast(new CmdDecoder()); //解码器
                        pipeline.addLast(new SimpleChannelInboundHandler<Cmd>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Cmd msg) {
                                onServiceRead(msg, local, ConnectionToService.this);
                            }
                        });
                        onServiceActive(ConnectionToService.this);
                    } else {
                        log.info("连接到服务端失败 - {}", channel);
                    }
                }
            });
        }

        public ChannelFuture writeAndFlush(Cmd data) {
            return channel.writeAndFlush(data).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }

        public ChannelFuture close() {
            channelToService.cancel(false);
            return channel.close();
        }

    }

}
