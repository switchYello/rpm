package com.fys.handler;

import com.fys.cmd.Cmd;
import com.fys.cmd.serverToClient.NeedCreateNewConnectionCmd;
import com.fys.cmd.serverToClient.Ping;
import com.fys.cmd.serverToClient.ServerStartFailCmd;
import com.fys.cmd.serverToClient.ServerStartSuccessCmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * hcy 2020/2/18
 */
public class CmdDecoder extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(CmdDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte flag = in.readByte();

        if (flag == Cmd.ServerToClient.serverStartSuccessCmd) {
            out.add(ServerStartSuccessCmd.decoderFrom(in));
            return;
        }
        if (flag == Cmd.ServerToClient.serverStartFailCmd) {
            out.add(ServerStartFailCmd.decoderFrom(in));
            return;
        }
        if (flag == Cmd.ServerToClient.ping) {
            out.add(Ping.decoderFrom(in));
            return;
        }
        if (flag == Cmd.ServerToClient.needCreateNewConnectionCmd) {
            out.add(NeedCreateNewConnectionCmd.decoderFrom(in));
            return;
        }
        log.error("无法识别服务端发送的指令,指令:{}", flag);
        ctx.close();
    }


}
