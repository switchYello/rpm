package com.fys.handler;

import com.fys.ServerManager;
import com.fys.cmd.clientToServer.WantManagerCmd;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * hcy 2020/2/20
 * 处理客户端发送的开信号
 */
public class WantManagerCmdHandler extends SimpleChannelInboundHandler<WantManagerCmd> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WantManagerCmd msg) throws Exception {

        //本地开启一个服务，返回开启服务的future
        ChannelFuture serverStartFuture = ServerManager.startNewServer(msg.getServerWorkPort(), ctx.channel(), msg.getClentName());

        //只有服务器端启动成功才添加PingPongHandler
        serverStartFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ctx.pipeline().addLast(new PingPongHandler());
                }
            }
        });
    }


}
