package com.fys.handler;

import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.DataCmd;
import com.fys.cmd.message.LoginCmd;
import com.fys.cmd.message.ManagerCmd;
import com.fys.cmd.message.NewDataConnectionCmd;
import com.fys.cmd.message.Ping;
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

        if (flag == Cmd.RAW_DATA) {
            out.add(RawDataCmd.decoderFrom(in));
            return;
        }

        if (flag == Cmd.ClientToServer.NEW_DATA_CONNECTION_CMD) {
            out.add(NewDataConnectionCmd.decoderFrom(in));
            return;
        }

        //收到客户端的ping
        if (flag == Cmd.PING) {
            out.add(new Ping());
            return;
        }

        //收到登录
        if (flag == Cmd.ClientToServer.LOGIN) {
            out.add(LoginCmd.decoderFrom(in));
            return;
        }

        if (flag == Cmd.ClientToServer.MANAGER_CMD) {
            out.add(new ManagerCmd());
            return;
        }

        if (flag == Cmd.ClientToServer.DATA_CMD) {
            out.add(new DataCmd());
            return;
        }

        //无法识别的指令
        log.error("无法识别客户端:{} 发送的指令,指令:{}", ctx.channel().remoteAddress(), flag);
        in.skipBytes(actualReadableBytes());
        ctx.close();
    }

}
