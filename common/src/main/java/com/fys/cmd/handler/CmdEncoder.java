package com.fys.cmd.handler;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * hcy 2020/2/18
 */
public class CmdEncoder extends MessageToMessageEncoder<Cmd> {

    //输出
    @Override
    protected void encode(ChannelHandlerContext ctx, Cmd msg, List<Object> out) {
        ByteBuf data = msg.toByte();
        ByteBuf byteBuf = Unpooled.buffer(4 + data.readableBytes()).writeInt(data.readableBytes()).writeBytes(data);
        out.add(byteBuf);
    }

}
