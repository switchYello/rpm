package com.fys.connection;

import io.netty.channel.Channel;
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

    public Channel channel() {
        return ctx.channel();
    }

    public void close() {
        ctx.close();
    }


}
