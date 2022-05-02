package com.fys.cmd.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/5/1 20:40
 */
public class ErrorLogHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ErrorLogHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        if ("Connection reset by peer".equals(cause.getMessage())) {
            log.error("Connection reset by peer - {}", channel);
            return;
        }
        log.error("连接报错 - {}", channel, cause);
        channel.close();
    }
}
