package com.fys.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 转换数据用的handler，将读到的数据传入out中
 * hcy 2020/2/10
 */
public class TransactionHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(TransactionHandler.class);
    private boolean autoRead;
    private Channel out;

    //输入管道，是否自动读取
    public TransactionHandler(Channel out, boolean autoRead) {
        this.out = out;
        this.autoRead = autoRead;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (autoRead) {
            out.writeAndFlush(msg);
        } else {
            out.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.read();
                } else {
                    log.error("透传handler写入失败in:{},to:{}", ctx, out);
                }
            });
        }
    }

    //如果输入的channel被关闭了，则手动关闭输出的管道
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (out.isActive()) {
            out.flush().close();
        }
        super.channelInactive(ctx);
    }

}
