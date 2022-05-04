package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/18
 */
public class Ping implements Cmd {

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(Cmd.PING);
    }

}
