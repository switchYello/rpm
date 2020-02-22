package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * hcy 2020/2/18
 */
public class Ping implements Cmd {

    private static Ping instance = new Ping();

    private byte[] data = {ServerToClient.ping};


    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ServerToClient.ping);
    }

    public static Ping decoderFrom(ByteBuf in) {
        return instance;
    }
}
