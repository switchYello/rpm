package com.fys;

import io.netty.channel.*;

/**
 * 转换数据用的handler，将读到的数据传入out中
 * hcy 2020/2/10
 */
public class TransactionHandler extends ChannelInboundHandlerAdapter {

    private boolean autoRead;
    private Channel out;

    //输入管道，是否自动读取
    public TransactionHandler(Channel out, boolean autoRead) {
        this.out = out;
        this.autoRead = autoRead;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        if (autoRead) {
            out.writeAndFlush(msg);
        } else {
            out.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.read();
                    }
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
