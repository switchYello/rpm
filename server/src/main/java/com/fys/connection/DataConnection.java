package com.fys.connection;

import com.fys.cmd.handler.ErrorLogHandler;
import com.fys.cmd.message.RawDataCmd;
import com.fys.cmd.message.StartTransactionCmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/4/28 17:01
 */
public class DataConnection {

    private static Logger log = LoggerFactory.getLogger(DataConnection.class);

    private ChannelHandlerContext ctx;
    private Channel target;

    //
    public DataConnection(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        ctx.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (target == null) {
                    throw new IllegalStateException("未绑定前不可能读取数据");
                }
                if (msg instanceof RawDataCmd) {
                    target.writeAndFlush(((RawDataCmd) msg).getContent()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    return;
                }
                log.info("接收到未识别的数据:{}", msg);
                ReferenceCountUtil.release(msg);
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
        RawDataCmd rawDataCmd = new RawDataCmd(msg);
        return ctx.writeAndFlush(rawDataCmd);
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
