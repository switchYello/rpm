package com.fys.handler;

import com.fys.cmd.message.clientToServer.Pong;
import com.fys.cmd.message.serverToClient.Ping;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
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

    /*
     * 忽略所有pong,并发送空Buffer到父handler来刷新读超时时间
     * */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Pong) {
            super.channelRead(ctx, Unpooled.EMPTY_BUFFER);
            return;
        }
        super.channelRead(ctx, msg);
    }

}
