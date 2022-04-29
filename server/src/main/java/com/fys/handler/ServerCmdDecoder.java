package com.fys.handler;

import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.DataConnectionCmd;
import com.fys.cmd.message.Ping;
import com.fys.cmd.message.Pong;
import com.fys.cmd.message.LoginCmd;
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

        //收到客户端pong,只有管理连接才能收到pong，数据连接不会发送Ping也就不会收到Pong
        if (flag == Cmd.pong) {
            out.add(Pong.decoderFrom(in));
            return;
        }
        //收到客户端的ping
        if (flag == Cmd.ping) {
            out.add(Ping.decoderFrom(in));
            return;
        }

        //新建数据连接
        if (flag == Cmd.dataConnectionCmd) {
            DataConnectionCmd cmd = DataConnectionCmd.decoderFrom(in);
            out.add(cmd);
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
