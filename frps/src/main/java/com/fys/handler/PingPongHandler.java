package com.fys.handler;

import com.fys.cmd.clientToServer.Pong;
import com.fys.cmd.serverToClient.Ping;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 此类只有当server开启成功后才会被添加到pipline
 * hcy 2020/2/20
 * 处理管理连接 ping pong 的 handler
 * 逻辑是定时发送ping，如果长时间没收到pong 则主动断开连接
 */
public class PingPongHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(PingPongHandler.class);
    //最后一次接收到pong的时间戳
    private long lastPong;
    //多长时间ping一次，秒
    private int pingRate = 10;
    //最大多久没有pong就断开，毫秒
    private long maxPongDelay = pingRate * 1000 * 4;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.executor().schedule(new SchedulePing(ctx), 2, TimeUnit.SECONDS);
        lastPong = System.currentTimeMillis();
        super.handlerAdded(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Pong) {
            lastPong = System.currentTimeMillis();
            log.debug("收到客户端pong");
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private class SchedulePing implements Runnable {

        private ChannelHandlerContext ctx;

        SchedulePing(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.channel().isActive()) {
                return;
            }
            if (System.currentTimeMillis() - lastPong > maxPongDelay) {
                log.error("超过最大Pong超时时间断开连接");
                ctx.close();
                return;
            }
            ctx.writeAndFlush(new Ping()).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.executor().schedule(this, pingRate, TimeUnit.SECONDS);
                } else {
                    ctx.close();
                }
            });
        }
    }


}
