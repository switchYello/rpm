package com.fys.cmd.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/22
 */
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(ExceptionHandler.class);
    public static ExceptionHandler INSTANCE = new ExceptionHandler();

    public static final String NAME = "ExceptionHandler";

    private ExceptionHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            log.error("收尾:Connection reset by peer");
            return;
        }
        log.error("收尾", cause);
    }
}
