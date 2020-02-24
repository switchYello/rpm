package com.fys.cmd.message.clientToServer;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/18
 */
public class Pong implements Cmd {

    private static Pong instance = new Pong();

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.pong);
    }

    public static Pong decoderFrom(ByteBuf in) {
        return instance;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public String getLocalHost() {
        return null;
    }



}
