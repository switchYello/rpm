package com.fys.connection;

import com.fys.cmd.handler.ErrorLogHandler;
import com.fys.cmd.message.NewDataConnectionCmd;
import com.fys.cmd.message.RawDataCmd;
import com.fys.cmd.message.StartTransactionCmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author hcy
 * @since 2022/4/28 17:01
 */
public class DataConnection {

    private NewDataConnectionCmd cmd;
    private ChannelHandlerContext ctx;

    public DataConnection(NewDataConnectionCmd cmd, ChannelHandlerContext ctx) {
        this.cmd = cmd;
        this.ctx = ctx;
    }

    public ChannelFuture writeAndFlush(ByteBuf msg) {
        return ctx.writeAndFlush(new RawDataCmd(msg));
    }

    //从该channel读到的数据，写入target中
    public void bindToChannel(Channel target) {
        ctx.channel().closeFuture().addListener((ChannelFutureListener) future -> target.close());
        ctx.pipeline().addLast(new SimpleChannelInboundHandler<RawDataCmd>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, RawDataCmd msg) {
                target.writeAndFlush(msg.getContent()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        });
        ctx.pipeline().addLast(new ErrorLogHandler());
    }

    //开始传输数据
    public void startTransaction() {
        StartTransactionCmd cmd = new StartTransactionCmd();
        ctx.writeAndFlush(cmd).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    public void close() {
        ctx.close();
    }

}
