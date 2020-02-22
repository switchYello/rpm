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
        log.error(msg.toString());
        log.info("映射{}->{}:{}开启成功", msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort());
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }

}