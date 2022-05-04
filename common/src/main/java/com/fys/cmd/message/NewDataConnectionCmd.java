package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * @author hcy
 * @since 2022/5/3 16:22
 * 由客户端发送给服务端，标识此连接是新建数据连接
 */
public class NewDataConnectionCmd implements Cmd {

    private int sessionId;

    public NewDataConnectionCmd(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(ClientToServer.NEW_DATA_CONNECTION_CMD);
        buf.writeInt(sessionId);
    }

    public static NewDataConnectionCmd decoderFrom(ByteBuf in) {
        int sessionId = in.readInt();
        return new NewDataConnectionCmd(sessionId);
    }

    public int getSessionId() {
        return sessionId;
    }
}
