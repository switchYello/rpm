package com.fys.handler;

import com.fys.cmd.message.clientToServer.Pong;
import com.fys.cmd.message.serverToClient.Ping;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/19
 */
@ChannelHandler.Sharable
public class PingHandler extends SimpleChannelInboundHandler<Ping> {

    private Logger log = LoggerFactory.getLogger(PingHandler.class);

    public static PingHandler INSTANCE = new PingHandler();

    private PingHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Ping msg) {
        log.debug("收到服务端ping，回复pong");
        ctx.writeAndFlush(new Pong());
    }

}