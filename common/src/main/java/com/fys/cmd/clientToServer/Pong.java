package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * hcy 2020/2/18
 */
public class Pong implements Cmd {

    private static Pong instance = new Pong();

    private byte[] data = {ClientToServer.pong};

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.pong);
    }

    public static Pong decoderFrom(ByteBuf in) {
        return instance;
    }

}
