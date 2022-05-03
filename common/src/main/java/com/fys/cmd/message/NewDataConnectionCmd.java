package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * @author hcy
 * @since 2022/5/3 16:22
 */
public class NewDataConnectionCmd implements Cmd {

    private long sessionId;

    public NewDataConnectionCmd(long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(ClientToServer.newDataConnectionCmd);
        buf.writeLong(sessionId);
    }

    public static NewDataConnectionCmd decoderFrom(ByteBuf in) {
        long sessionId = in.readLong();
        return new NewDataConnectionCmd(sessionId);
    }

    public long getSessionId() {
        return sessionId;
    }
}
