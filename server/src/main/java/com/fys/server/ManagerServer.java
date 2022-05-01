package com.fys.server;

import com.fys.ClientManager;
import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.message.DataConnectionCmd;
import com.fys.cmd.message.LoginAuthInfo;
import com.fys.cmd.message.Ping;
import com.fys.conf.EventLoops;
import com.fys.conf.ServerInfo;
import com.fys.connection.DataConnection;
import com.fys.connection.ManagerConnection;
import com.fys.handler.ServerCmdDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/4/28 15:14
 */
public class ManagerServer {

    private static final Logger log = LoggerFactory.getLogger(ManagerServer.class);

    ClientManager clientManager;
    ServerInfo serverInfo;

    public ManagerServer(ClientManager clientManager, ServerInfo serverInfo) {
        this.clientManager = clientManager;
        this.serverInfo = serverInfo;
    }

    public void start() {
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(EventLoops.BOSS, EventLoops.WORKER)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new CmdEncoder());
                        //控制超时，防止链接上来但不发送消息任何的连接
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 300));
                        ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new ServerCmdDecoder());
                        ch.pipeline().addLast(new ManagerHandler());
                    }
                })
                .bind(serverInfo.getBindHost(), serverInfo.getBindPort())
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("控制端在端口:{}启动成功", serverInfo.getBindPort());
                    } else {
                        log.error("控制端在端口:" + serverInfo.getBindPort() + "启动失败", future.cause());
                    }
                });
    }

    private class ManagerHandler extends SimpleChannelInboundHandler<Object> {

        AttributeKey<ManagerConnection> managerConnectionKey = AttributeKey.valueOf("ManagerConnection");

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            //登录,创建ManagerConnection 处理登录消息
            if (msg instanceof LoginAuthInfo) {
                log.debug("收到client登录请求:{}", ((LoginAuthInfo) msg).getClientName());
                ManagerConnection managerConnection = ctx.channel().attr(managerConnectionKey).get();
                if (managerConnection == null) {
                    managerConnection = new ManagerConnection(ctx, clientManager, serverInfo);
                    ctx.channel().attr(managerConnectionKey).set(managerConnection);
                }
                managerConnection.handlerLogin((LoginAuthInfo) msg);
                return;
            }
            //收到Ping
            if (msg instanceof Ping) {
                ManagerConnection managerConnection = ctx.channel().attr(managerConnectionKey).get();
                if (managerConnection != null) {
                    managerConnection.handlerPing();
                }
                return;
            }
            //数据连接
            if (msg instanceof DataConnectionCmd) {
                log.debug("收到client数据连接请求:{}", msg);
                DataConnectionCmd cmd = (DataConnectionCmd) msg;
                //移除多余handler
                ctx.pipeline().remove(ServerCmdDecoder.class);
                ctx.pipeline().remove(ManagerHandler.class);
                clientManager.registerDataConnection(cmd.getSessionId(), new DataConnection(ctx));
                return;
            }
            throw new RuntimeException("消息不能识别" + msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Channel channel = ctx.channel();
            if ("Connection reset by peer".equals(cause.getMessage())) {
                log.error("Connection reset by peer local:{},remote:{}", channel.localAddress(), channel.remoteAddress());
                return;
            }
            log.error("ServerCmdDecoder:" + channel.localAddress() + " remote" + channel.remoteAddress(), cause);
        }
    }

}
