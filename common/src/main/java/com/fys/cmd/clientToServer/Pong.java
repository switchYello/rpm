package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * hcy 2020/2/18
 */
public class Pong implements Cmd {

    private byte[] data = {ClientToServer.pong};

    @Override
    public ByteBuf toByte() {
        return Unpooled.wrappedBuffer(data);
    }
}
