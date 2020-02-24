package com.fys.cmd.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
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

    public String serverName;

    public FlowManagerHandler(String serverName) {
        this.serverName = serverName;
    }

    public static FlowManagerHandler INSTANCE = new FlowManagerHandler("$");
    private AtomicLong inFlow = new AtomicLong(0);
    private AtomicLong outFlow = new AtomicLong(0);

    private static final int[] ds = {0, 1024, 1024 * 1024, 1024 * 1024 * 1024};
    private static final String[] dw = {"B", "KB", "MB", "GB"};
    private static final DecimalFormat format = new DecimalFormat(".##");

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ScheduledFuture<?> scheduledFuture = ctx.executor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                double inValue = inFlow.doubleValue();
                double outValue = outFlow.doubleValue();

                String in = "";
                String out = "";
                for (int i = ds.length - 1; i >= 0; i--) {
                    if (inValue > ds[i]) {
                        in = format.format(inValue / ds[i]) + dw[i];
                        break;
                    }
                }
                for (int i = ds.length - 1; i >= 0; i--) {
                    if (outValue > ds[i]) {
                        out = format.format(outValue / ds[i]) + dw[i];
                        break;
                    }
                }

                log.info("{}的进站流量:{},出站流量:{}", serverName, in, out);
            }
        }, 30, 60, TimeUnit.SECONDS);

        //这里添加关闭定时的监听
        ctx.channel().closeFuture().addListener((ChannelFutureListener) future -> scheduledFuture.cancel(true));
        super.handlerAdded(ctx);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        outFlow.addAndGet(msg.readableBytes());
        out.add(msg.readRetainedSlice(msg.readableBytes()));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        inFlow.addAndGet(msg.readableBytes());
        out.add(msg.readRetainedSlice(msg.readableBytes()));
    }

}
