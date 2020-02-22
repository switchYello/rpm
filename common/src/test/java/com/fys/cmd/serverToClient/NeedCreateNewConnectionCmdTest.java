package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * hcy 2020/2/22
 */
public class NeedCreateNewConnectionCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        NeedCreateNewConnectionCmd src = new NeedCreateNewConnectionCmd((short) 90, "127.0.0.2", (short) 100, System.nanoTime());
        src.encoderTo(buffer);
        assertEquals(Cmd.ServerToClient.needCreateNewConnectionCmd, buffer.readByte());
        NeedCreateNewConnectionCmd dec = NeedCreateNewConnectionCmd.decoderFrom(buffer);
        assertEquals(src.getConnectionToken(), dec.getConnectionToken());
        assertEquals(src.getLocalPort(), dec.getLocalPort());
        assertEquals(src.getServerPort(), dec.getServerPort());
        assertEquals(src.getLocalHost(), dec.getLocalHost());
        buffer.release();
    }
}