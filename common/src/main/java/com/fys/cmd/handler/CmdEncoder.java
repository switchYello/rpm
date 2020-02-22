package com.fys.cmd.handler;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * hcy 2020/2/18
 */
public class CmdEncoder extends MessageToByteEncoder<Cmd> {

    //输出
    @Override
    protected void encode(ChannelHandlerContext ctx, Cmd msg, ByteBuf out) {
        msg.encoderTo(out);
    }


}
