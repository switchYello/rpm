package com.fys.cmd.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * hcy 2020/2/20
 * 流量统计，定时输出流量信息
 */
@ChannelHandler.Sharable
public class FlowManagerHandler extends MessageToMessageCodec<ByteBuf, ByteBuf> {

    private static Logger log = LoggerFactory.getLogger(FlowManagerHandler.class);

    public static FlowManagerHandler INSTANCE = new FlowManagerHandler();
    private AtomicLong inFlow = new AtomicLong(0);
    private AtomicLong outFlow = new AtomicLong(0);

    private FlowManagerHandler() {
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ScheduledFuture<?> scheduledFuture = ctx.executor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("进站流量:{}MB,出站流量:{}MB", inFlow.doubleValue() / 1024 / 1024 / 1024, outFlow.doubleValue() / 1024 / 1024 / 1024);
            }
        }, 10, 10, TimeUnit.SECONDS);


        ctx.channel().closeFuture().addListener((ChannelFutureListener) future -> scheduledFuture.cancel(true));
        super.handlerAdded(ctx);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        outFlow.addAndGet(msg.readableBytes());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        inFlow.addAndGet(msg.readableBytes());
    }

}
