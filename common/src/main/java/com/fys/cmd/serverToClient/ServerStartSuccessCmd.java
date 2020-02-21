package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

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
    public ByteBuf toByte() {
        ByteBuf msg = Unpooled.buffer(ByteBufUtil.utf8MaxBytes(serverId) + 1);
        msg.writeByte(ServerToClient.serverStartSuccessCmd).writeCharSequence(serverId, StandardCharsets.UTF_8);
        return msg;
    }

    public String getServerId() {
        return serverId;
    }
}
