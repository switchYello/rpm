package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * 告诉客户端，无法创建server，并返回无法创建的原因
 */
public class ServerStartFailCmd implements Cmd {

    private String failMsg;

    public ServerStartFailCmd(String failMsg) {
        this.failMsg = failMsg;
    }

    @Override
    public ByteBuf toByte() {
        ByteBuf buffer = Unpooled.buffer(ByteBufUtil.utf8MaxBytes(failMsg) + 1);
        buffer.writeByte(ServerToClient.serverStartFailCmd).writeCharSequence(failMsg, StandardCharsets.UTF_8);
        return buffer;
    }

    public String getFailMsg() {
        return failMsg;
    }
}
