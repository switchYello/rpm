package com.fys.handler;

import com.fys.cmd.serverToClient.ServerStartFailCmd;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/19
 */
@ChannelHandler.Sharable
public class ServerStartFailHandler extends SimpleChannelInboundHandler<ServerStartFailCmd> {

    private Logger log = LoggerFactory.getLogger(ServerStartFailHandler.class);

    public static ServerStartFailHandler INSTANCE = new ServerStartFailHandler();

    private ServerStartFailHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerStartFailCmd msg) {
        log.info("服务器无法创建server将关闭连接稍后尝试,因为:{}", msg.getFailMsg());
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }

}