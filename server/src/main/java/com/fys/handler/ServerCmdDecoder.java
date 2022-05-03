package com.fys.handler;

import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.LoginCmd;
import com.fys.cmd.message.NewDataConnectionCmd;
import com.fys.cmd.message.Ping;
import com.fys.cmd.message.Pong;
import com.fys.cmd.message.RawDataCmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * hcy 2020/2/10
 * length，code，data
 * 此类处理服务端和客户端managerChannel
 */
public class ServerCmdDecoder extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(ServerCmdDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int flag = in.readInt();

        if (flag == Cmd.rawData) {
            out.add(RawDataCmd.decoderFrom(in));
            return;
        }

        if (flag == Cmd.ClientToServer.newDataConnectionCmd) {
            out.add(NewDataConnectionCmd.decoderFrom(in));
            return;
        }

        //收到客户端的ping
        if (flag == Cmd.ping) {
            out.add(Ping.decoderFrom(in));
            return;
        }

        //收到客户端pong。服务端不会向客户端发送ping，所以不会受到pong，所以这里逻辑不会走到
        if (flag == Cmd.pong) {
            out.add(Pong.decoderFrom(in));
            return;
        }

        //收到登录
        if (flag == Cmd.ClientToServer.login) {
            out.add(LoginCmd.decoderFrom(in));
            return;
        }

        //无法识别的指令
        log.error("无法识别客户端:{} 发送的指令,指令:{}", ctx.channel().remoteAddress(), flag);
        in.skipBytes(actualReadableBytes());
        ctx.close();
    }

}
