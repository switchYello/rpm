package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * hcy 2020/2/22
 */
public class WantDataCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        WantDataCmd src = new WantDataCmd((short) 90,"127.0.2.8",(short) 80);
        src.encoderTo(buffer);
        assertEquals(Cmd.ClientToServer.wantDataCmd, buffer.readByte());
        WantDataCmd dec = WantDataCmd.decoderFrom(buffer);
        assertEquals(src.getLocalHost(),dec.getLocalHost());
        assertEquals(src.getLocalPort(), dec.getLocalPort());
        assertEquals(src.getServerPort(), dec.getServerPort());
        buffer.release();
    }
}