package com.fys.connection;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author hcy
 * @since 2022/4/28 14:44
 */
public class ManagerConnection {

    private ChannelHandlerContext ctx;

    public ManagerConnection(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelFuture writeMessage(Object obj) {
        return ctx.writeAndFlush(obj);
    }

    public void close() {
        ctx.close();
    }

}
