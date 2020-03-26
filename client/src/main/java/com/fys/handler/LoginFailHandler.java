package com.fys.handler;

import com.fys.cmd.message.serverToClient.LoginFailCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/3/26
 */
public class LoginFailHandler extends SimpleChannelInboundHandler<LoginFailCmd> {
    private static Logger log = LoggerFactory.getLogger(LoginFailCmd.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginFailCmd msg) throws Exception {
        log.info(msg.toString());
        ctx.close();
    }
}
