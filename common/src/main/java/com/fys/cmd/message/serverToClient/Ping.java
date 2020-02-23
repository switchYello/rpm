package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/18
 */
public class Ping implements Cmd {

    private static Ping instance = new Ping();

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ServerToClient.ping);
    }

    @Override
    public short getServerPort() {
        return 0;
    }

    @Override
    public short getLocalPort() {
        return 0;
    }

    @Override
    public String getLocalHost() {
        return null;
    }

    public static Ping decoderFrom(ByteBuf in) {
        return instance;
    }
}