package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/18
 */
public class Pong implements Cmd {

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(Cmd.PONG);
    }

}
