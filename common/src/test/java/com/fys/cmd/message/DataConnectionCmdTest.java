package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * hcy 2020/2/23
 */
public class DataConnectionCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        DataConnectionCmd src = new DataConnectionCmd( 90, "127.0.2.8",  80, System.nanoTime());
        src.encoderTo(buffer);
        assertEquals(Cmd.dataConnectionCmd, buffer.readByte());
        DataConnectionCmd dec = DataConnectionCmd.decoderFrom(buffer);
        assertEquals(src.getLocalHost(), dec.getLocalHost());
        assertEquals(src.getLocalPort(), dec.getLocalPort());
        assertEquals(src.getServerPort(), dec.getServerPort());
        assertEquals(src.getToken(), dec.getToken());
        buffer.release();
    }
}