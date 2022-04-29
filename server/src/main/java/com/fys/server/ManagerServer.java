package com.fys.server;

import com.fys.ClientManager;
import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.message.DataConnectionCmd;
import com.fys.cmd.message.LoginAuthInfo;
import com.fys.cmd.message.Ping;
import com.fys.cmd.message.Pong;
import com.fys.cmd.message.LoginFailCmd;
import com.fys.cmd.util.CodeUtil;
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
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author hcy
 * @since 2022/4/28 15:14
 */
public class ManagerServer {

    private static final Logger log = LoggerFactory.getLogger(ManagerServer.class);

    ClientManager clientManager;
    ServerInfo serverInfo;
    EventLoopGroup boss;

    public ManagerServer(EventLoopGroup boss, ClientManager clientManager, ServerInfo serverInfo) {
        this.clientManager = clientManager;
        this.serverInfo = serverInfo;
        this.boss = boss;
    }

    public void start() {
        ServerBootstrap sb = new ServerBootstrap();
        sb.group(boss, boss)
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
                        boss.shutdownGracefully();
                    }
                });
    }

    private class ManagerHandler extends SimpleChannelInboundHandler<Object> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            //登录
            if (msg instanceof LoginAuthInfo) {
                log.debug("收到client登录请求:{}", ((LoginAuthInfo) msg).getClientName());
                handlerLogin(ctx, (LoginAuthInfo) msg);
                return;
            }
            //收到Ping
            if (msg instanceof Ping) {
                handlerPing(ctx);
            }
            //数据连接
            if (msg instanceof DataConnectionCmd) {
                log.debug("收到client数据连接请求:{}", msg);
                handlerDataConnection(ctx, (DataConnectionCmd) msg);
                return;
            }
            throw new RuntimeException("消息不能识别" + msg);
        }

        /**
         * 验证登录信息，如已登录则忽略，如验证失败则发送响应后关闭连接
         *
         * @param ctx
         * @param msg
         */
        private void handlerLogin(ChannelHandlerContext ctx, LoginAuthInfo msg) {
            ManagerConnection client = clientManager.getClient(msg.getClientName());
            if (client != null) {
                log.warn("忽略重复登录:{}", msg);
                return;
            }
            byte[] md5 = CodeUtil.md5((msg.getClientName() + msg.getTimeStamp() + serverInfo.getToken()).getBytes(UTF_8));
            if (Arrays.equals(msg.getReadMd5(), md5)) {
                clientManager.registerManagerConnection(msg.getClientName(), new ManagerConnection(ctx));
                log.info("客户端登录成功 [{} -> {}]", ctx.channel().localAddress(), ctx.channel().remoteAddress());
                return;
            }
            log.info("客户端验证失败:{}", msg);
            ctx.writeAndFlush(new LoginFailCmd(msg.getClientName(), "验证失败")).addListener(ChannelFutureListener.CLOSE);
        }

        private void handlerDataConnection(ChannelHandlerContext ctx, DataConnectionCmd msg) {
            //移除多余handler
            ctx.pipeline().remove(ServerCmdDecoder.class);
            ctx.pipeline().remove(ManagerHandler.class);
            clientManager.registerDataConnection(msg.getSessionId(), new DataConnection(ctx));
        }


        private void handlerPing(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(new Pong()).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
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
