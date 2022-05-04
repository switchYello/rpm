package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * @author hcy
 * @since 2022/5/4 12:16
 */
public class DataCmd implements Cmd {
    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(ClientToServer.DATA_CMD);
    }
}
