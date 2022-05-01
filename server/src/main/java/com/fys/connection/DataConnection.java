package com.fys.connection;

import com.fys.cmd.handler.TransactionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author hcy
 * @since 2022/4/28 17:01
 */
public class DataConnection {

    private ChannelHandlerContext ctx;

    public DataConnection(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelFuture writeAndFlush(Object msg) {
        return ctx.writeAndFlush(msg);
    }

    //从该channel读到的数据，写入target中
    public void bindToChannel(Channel target) {
        ctx.pipeline().addLast(new TransactionHandler(target, true));
    }

    public void close() {
        ctx.close();
    }

    private Channel nativeChannel() {
        return ctx.channel();
    }

}
