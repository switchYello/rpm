package com.fys.cmd.message.clientToServer;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * hcy 2020/2/22
 */
public class WantManagerCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        WantManagerCmd src = new WantManagerCmd((short) 70,"127.0.0.1", (short) 90);
        src.encoderTo(buffer);
        assertEquals(Cmd.ClientToServer.wantManagerCmd, buffer.readByte());
        WantManagerCmd dec = WantManagerCmd.decoderFrom(buffer);
        assertEquals(src.getLocalPort(), dec.getLocalPort());
        assertEquals(src.getServerPort(), dec.getServerPort());
        assertEquals(src.getLocalHost(), dec.getLocalHost());
        buffer.release();
    }
}