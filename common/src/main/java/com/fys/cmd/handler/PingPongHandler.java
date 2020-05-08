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


public class PingPongHandler extends IdleStateHandler {

    private static Logger log = LoggerFactory.getLogger(PingPongHandler.class);
    //读超时时间
    private static int writeTimeOut = 10;
    //写超时时间
    private static int readTimeout = writeTimeOut * 4 + 2;

    public PingPongHandler() {
        super(readTimeout, writeTimeOut, 0);
    }

    /**
     * 此处添加连个handler来处理ping 和 pong消息
     * 对于pong消息，直接丢弃
     * 对于ping消息，需要回复pong
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        ctx.pipeline().addAfter(ctx.name(), null, new SimpleChannelInboundHandler<Pong>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Pong msg) {
                log.debug("收到Pong");
            }
        });
        ctx.pipeline().addAfter(ctx.name(), null, new SimpleChannelInboundHandler<Ping>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Ping msg) {
                log.debug("收到Ping");
                ctx.writeAndFlush(new Pong()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        });
    }

    /*
     * 长时间没写（写超时），则发送Ping
     * 长时间没读（读超时），则断开连接
     * */
    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        if (evt.state() == IdleState.WRITER_IDLE) {
            log.debug("发送Ping");
            ctx.writeAndFlush(new Ping()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            log.info("读超时断开连接：{}", ctx.channel().remoteAddress());
            ctx.flush().close();
        }
    }

}
