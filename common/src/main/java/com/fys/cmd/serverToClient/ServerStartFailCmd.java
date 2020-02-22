package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 告诉客户端，无法创建server，并返回无法创建的原因
 */
public class ServerStartFailCmd implements Cmd {

    private String failMsg;

    public ServerStartFailCmd(String failMsg) {
        this.failMsg = failMsg;
    }


    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ServerToClient.serverStartFailCmd);
        buf.writeShort(failMsg.length());
        buf.writeCharSequence(failMsg, UTF_8);
    }

    public static ServerStartFailCmd decoderFrom(ByteBuf in) {
        short msgLength = in.readShort();
        CharSequence charSequence = in.readCharSequence(msgLength, UTF_8);
        return new ServerStartFailCmd(charSequence.toString());
    }

    public String getFailMsg() {
        return failMsg;
    }
}
