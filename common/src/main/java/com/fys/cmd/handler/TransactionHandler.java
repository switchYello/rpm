package com.fys.cmd.handler;

import io.netty.channel.*;
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
            out.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            out.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.read();
                    } else {
                        log.error("透传handler写入失败in:" + ctx + ",to:" + out, future.cause());
                        ctx.close();
                    }
                }
            });
        }
    }

    //如果输入的channel被关闭了，则手动关闭输出的管道
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (out.isActive()) {
            log.debug("[{}]被关闭了，所以同步关闭另一侧", ctx.name());
            out.flush().close();
        } else {
            log.debug("[{}]被关闭了，另一侧已经被关闭了，不处理", ctx.name());
        }
        super.channelInactive(ctx);
    }
}
