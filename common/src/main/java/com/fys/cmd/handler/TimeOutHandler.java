package com.fys.cmd.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/21
 */
public class TimeOutHandler extends IdleStateHandler {

    private static Logger log = LoggerFactory.getLogger(TimeOutHandler.class);

    public TimeOutHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }


    @Override
    public final void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.debug("超时关闭连接:{},Event:{}", ctx, evt);
        ctx.flush().close();
    }
}
