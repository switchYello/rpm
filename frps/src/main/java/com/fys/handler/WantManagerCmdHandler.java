package com.fys.handler;

import com.fys.Server;
import com.fys.ServerManager;
import com.fys.cmd.clientToServer.WantManagerCmd;
import com.fys.cmd.serverToClient.ServerStartFailCmd;
import com.fys.cmd.serverToClient.ServerStartSuccessCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

/**
 * hcy 2020/2/20
 * 处理客户端发送的开信号
 */
public class WantManagerCmdHandler extends SimpleChannelInboundHandler<WantManagerCmd> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WantManagerCmd msg) {

        //本地开启一个服务，返回开启服务的future
        Promise<Server> serverStartFuture = ServerManager.startNewServer(msg.getServerWorkPort(), ctx.channel(), msg.getClientName());

        //只有服务器端启动成功才添加PingPongHandler
        serverStartFuture.addListener((GenericFutureListener<Future<Server>>) future -> {
            if (future.isSuccess()) {
                Server server = future.getNow();
                ctx.writeAndFlush(new ServerStartSuccessCmd(server.getId())).addListener(CLOSE_ON_FAILURE);
                ctx.pipeline().addLast(new PingPongHandler());
            } else {
                ctx.writeAndFlush(new ServerStartFailCmd(future.toString())).addListener(CLOSE);
            }
        });
    }
}
