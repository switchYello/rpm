package com.fys.cmd.handler;

import com.fys.cmd.message.Ping;
import com.fys.cmd.message.Pong;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingHandler extends IdleStateHandler {

    private static Logger log = LoggerFactory.getLogger(PingHandler.class);
    //写超时时间
    private static int writeTimeOut = 10;
    //读超时时间  = 4 * 写时间 + 2
    private static int readTimeout = writeTimeOut * 4 + 2;

    public PingHandler() {
        super(readTimeout, writeTimeOut, 0);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().addAfter(ctx.name(), null, new SimpleChannelInboundHandler<Pong>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Pong msg) {
                log.debug("收到Pong");
            }
        });
        super.handlerAdded(ctx);
    }

    /*
     * 长时间没写（写超时），则发送Ping
     * 长时间没读（读超时），则断开连接
     * */
    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        if (evt.state() == IdleState.WRITER_IDLE) {
            log.debug("发送Ping");
            ctx.writeAndFlush(new Ping()).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            log.info("PingPong读超时,断开连接 - {}", ctx.channel());
            ctx.flush().close();
        }
    }

}
