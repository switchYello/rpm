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
 * hcy 2020/2/20
 * 处理管理连接 ping pong 的 handler
 * 逻辑是定时发送ping，如果长时间没收到pong 则主动断开连接
 */
public class PingPongHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(PingPongHandler.class);
    //最后一次接收到pong的时间戳
    private long lastPong = System.currentTimeMillis();
    //最大多久没有pong就断开，毫秒
    private long maxPongDelay = 5000 * 4;
    //多长时间ping一次，单位秒
    private int pingRate = 5;
    //是否是接收到的第一个数据
    private boolean first = true;

    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Pong) {
            lastPong = System.currentTimeMillis();
            log.info("收到客户端pong");
            if (first) {
                first = false;
                ctx.executor().schedule(new SchedulePing(ctx), 1, TimeUnit.SECONDS);
            }
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
                log.info("超过最大pong超时时间断开连接");
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
