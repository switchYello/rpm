package com.fys.server;

import com.fys.ClientManager;
import com.fys.Debug;
import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.TimeOutHandler;
import com.fys.cmd.message.DataCmd;
import com.fys.cmd.message.ManagerCmd;
import com.fys.cmd.util.EventLoops;
import com.fys.conf.ServerInfo;
import com.fys.connection.DataConnection;
import com.fys.connection.ManagerConnection;
import com.fys.handler.ServerCmdDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/4/28 15:14
 */
public class ManagerServer {

    private static final Logger log = LoggerFactory.getLogger(ManagerServer.class);

    private ClientManager clientManager;
    private ServerInfo serverInfo;

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
                    protected void initChannel(Channel ch) {
                        if (Debug.isDebug) {
                            ch.pipeline().addLast(new LoggingHandler());
                            ch.pipeline().addLast(new CmdEncoder());
                            ch.pipeline().addLast(new ServerCmdDecoder());
                            ch.pipeline().addLast(new ConnectionInitHandler());
                        } else {
                            ch.pipeline().addLast(new TimeOutHandler(0, 0, 600));
                            ch.pipeline().addLast(new CmdEncoder());
                            ch.pipeline().addLast(new ServerCmdDecoder());
                            ch.pipeline().addLast(new ConnectionInitHandler());
                        }
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


    private class ConnectionInitHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            //管理连接
            if (msg instanceof ManagerCmd) {
                log.debug("收到管理连接请求");
                new ManagerConnection(ctx, clientManager, serverInfo);
            }
            //数据连接
            else if (msg instanceof DataCmd) {
                log.debug("收到数据连接请求");
                new DataConnection(ctx, clientManager);
            }
            //无法识别
            else {
                log.error("收到无法识别的消息:{}", msg);
                ReferenceCountUtil.release(msg);
                ctx.close();
            }
            ctx.pipeline().remove(this);
        }
    }

}
