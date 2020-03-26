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

    public static Ping decoderFrom(ByteBuf in) {
        return instance;
    }

}
