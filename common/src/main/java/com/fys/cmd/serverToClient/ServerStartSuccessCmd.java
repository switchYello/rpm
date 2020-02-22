package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/19
 */
public class ServerStartSuccessCmd implements Cmd {

    //既然服务器创建成功，则返回客户端时将id带回
    private String serverId;

    public ServerStartSuccessCmd(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ServerToClient.serverStartSuccessCmd);
        buf.writeShort(serverId.length());
        buf.writeCharSequence(serverId, UTF_8);
    }

    public static ServerStartSuccessCmd decoderFrom(ByteBuf in) {
        short serverIdLength = in.readShort();
        CharSequence charSequence = in.readCharSequence(serverIdLength, UTF_8);
        return new ServerStartSuccessCmd(charSequence.toString());
    }

    public String getServerId() {
        return serverId;
    }
}
