package com.fys.connection;

import com.fys.cmd.handler.ErrorLogHandler;
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

    private ChannelHandlerContext ctx;
    private Channel target;

    //
    public DataConnection(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        ctx.pipeline().addLast(new SimpleChannelInboundHandler<RawDataCmd>() {
            @Override
            public void channelRead0(ChannelHandlerContext ctx, RawDataCmd msg) {
                if (target == null) {
                    throw new IllegalStateException("未绑定前不可能读取数据");
                }
                target.writeAndFlush(msg.getContent()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) {
                if (target != null) {
                    target.close();
                }
            }
        });
        ctx.pipeline().addLast(new ErrorLogHandler());
    }

    public ChannelFuture writeAndFlush(ByteBuf msg) {
        return ctx.writeAndFlush(new RawDataCmd(msg));
    }

    //发送开始传输数据指令给客户端，没发送前客户端不会传输数据
    public void startTransaction() {
        StartTransactionCmd cmd = new StartTransactionCmd();
        ctx.writeAndFlush(cmd).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    public void close() {
        ctx.close();
    }

    //从该channel读到的数据，写入target中
    public void bindToChannel(Channel target) {
        this.target = target;
    }

}
