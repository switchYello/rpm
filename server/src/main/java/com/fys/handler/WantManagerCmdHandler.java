package com.fys.handler;

import com.fys.Server;
import com.fys.ServerManager;
import com.fys.cmd.message.clientToServer.WantManagerCmd;
import com.fys.cmd.message.serverToClient.ServerStartFailCmd;
import com.fys.cmd.message.serverToClient.ServerStartSuccessCmd;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

/**
 * hcy 2020/2/20
 * 处理客户端发送的开信号
 */
@ChannelHandler.Sharable
public class WantManagerCmdHandler extends SimpleChannelInboundHandler<WantManagerCmd> {

    private static Logger log = LoggerFactory.getLogger(WantManagerCmdHandler.class);
    public static WantManagerCmdHandler INSTANCE = new WantManagerCmdHandler();

    private WantManagerCmdHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WantManagerCmd msg) {

        //本地开启一个服务，返回开启服务的future
        Promise<Server> serverStartFuture = ServerManager.startNewServer(msg, ctx.channel());

        //只有服务器端启动成功才添加PingPongHandler
        serverStartFuture.addListener((GenericFutureListener<Future<Server>>) future -> {
            if (future.isSuccess()) {
                Server server = future.getNow();
                log.debug("创建Server成功回复客户端，ServerId:{}", server.getId());
                ctx.writeAndFlush(new ServerStartSuccessCmd(msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort()))
                        .addListener(CLOSE_ON_FAILURE);
            } else {
                log.error("创建Server失败回复客户端," + msg.toString(), future.cause());
                String cause = future.cause() != null ? future.cause().toString() : "未知原因";
                ctx.writeAndFlush(new ServerStartFailCmd(msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort(), cause))
                        .addListener(CLOSE);
            }
        });
    }
}
