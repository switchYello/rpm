package com.fys.handler;

import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.LoginFailCmd;
import com.fys.cmd.message.NeedDataConnectionCmd;
import com.fys.cmd.message.Pong;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * hcy 2020/2/18
 */
public class ManagerCmdDecoder extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(ManagerCmdDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int flag = in.readInt();
        if (flag == Cmd.PONG) {
            out.add(new Pong());
            return;
        }
        if (flag == Cmd.ServerToClient.NEED_DATA_CONNECTION_CMD) {
            out.add(NeedDataConnectionCmd.decoderFrom(in));
            return;
        }
        if (flag == Cmd.ServerToClient.LOGIN_FAIL) {
            out.add(LoginFailCmd.decoderFrom(in));
            return;
        }
        log.error("无法识别服务端发送的指令,指令:{}", flag);
        in.skipBytes(actualReadableBytes());
        ctx.close();
    }

}
