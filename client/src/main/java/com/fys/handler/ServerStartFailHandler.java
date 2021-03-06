package com.fys.handler;

import com.fys.cmd.message.serverToClient.ServerStartFailCmd;
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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerStartFailCmd msg) {
        log.error(msg.toString());
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }

}