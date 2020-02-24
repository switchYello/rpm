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
    private static final int[] ds = {0, 1024, 1024 * 1024, 1024 * 1024 * 1024};
    private static final String[] dw = {"B", "KB", "MB", "GB"};
    private static final DecimalFormat format = new DecimalFormat(".##");
    public static FlowManagerHandler ClientFLowManager = new FlowManagerHandler("Client", "发送到本地端口的", "从本地端口接收的");

    private AtomicLong decodeFlow = new AtomicLong(0);
    private AtomicLong encodeFlow = new AtomicLong(0);

    public String serverName;
    private String encodeName, decodeName;

    public FlowManagerHandler(String serverName, String encodeName, String decodeName) {
        this.serverName = serverName;
        this.encodeName = encodeName;
        this.decodeName = decodeName;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ScheduledFuture<?> scheduledFuture = ctx.executor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {


                String decode = "";
                for (int i = ds.length - 1; i >= 0; i--) {
                    if (decodeFlow.doubleValue() > ds[i]) {
                        decode = format.format(decodeFlow.doubleValue() / ds[i]) + dw[i];
                        break;
                    }
                }

                String encode = "";
                for (int i = ds.length - 1; i >= 0; i--) {
                    if (encodeFlow.doubleValue() > ds[i]) {
                        encode = format.format(encodeFlow.doubleValue() / ds[i]) + dw[i];
                        break;
                    }
                }

                log.info("{}:{}流量:{},{}流量:{}", serverName, decodeName, decode, encodeName, encode);
            }
        }, 30, 60, TimeUnit.SECONDS);

        //这里添加关闭定时的监听
        ctx.channel().closeFuture().addListener((ChannelFutureListener) future -> scheduledFuture.cancel(true));
        super.handlerAdded(ctx);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        encodeFlow.addAndGet(msg.readableBytes());
        out.add(msg.readRetainedSlice(msg.readableBytes()));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        decodeFlow.addAndGet(msg.readableBytes());
        out.add(msg.readRetainedSlice(msg.readableBytes()));
    }

}
