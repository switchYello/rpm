package com.fys.handler;

import com.fys.cmd.message.clientToServer.Pong;
import com.fys.cmd.message.serverToClient.Ping;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此类只有当server开启成功后才会被添加到pipline
 * hcy 2020/2/20
 * 处理管理连接 ping pong 的 handler
 * 逻辑是长时间没读到数据则主动断开连接，长时间没写数据则写一个ping过去
 */
public class PingPongHandler extends IdleStateHandler {

    private static Logger log = LoggerFactory.getLogger(PingPongHandler.class);
    private static int writeTimeOut = 10;
    private static int readTimeout = writeTimeOut * 4 + 2;

    public PingPongHandler() {
        super(readTimeout, writeTimeOut, 0);
    }

    /**
     * PingPongHandler这个类利用了IdleStateHandler定时发送ping
     * 原理在channelIdle方法里面，
     * 但是收到的pong会被继续向下传播，这里添加一个SimpleChannelInboundHandler<Pong> 来丢弃所有的pong
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
    }

    /*
     * 长时间没写（写超时），则发送ping
     * 长时间没读（读超时），则断开连接
     * */
    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        if (evt.state() == IdleState.WRITER_IDLE) {
            ctx.writeAndFlush(new Ping()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            log.debug("读超时断开连接：{}", evt);
            ctx.flush().close();
        }
    }

}
