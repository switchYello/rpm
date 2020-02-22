package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * hcy 2020/2/22
 */
public class ServerStartSuccessCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        ServerStartSuccessCmd src = new ServerStartSuccessCmd((short) 70, "127.0.5.7", (short) 50);
        src.encoderTo(buffer);
        assertEquals(Cmd.ServerToClient.serverStartSuccessCmd, buffer.readByte());
        ServerStartSuccessCmd dec = ServerStartSuccessCmd.decoderFrom(buffer);
        assertEquals(src.getLocalPort(), dec.getLocalPort());
        assertEquals(src.getServerPort(), dec.getServerPort());
        assertEquals(src.getLocalPort(), dec.getLocalPort());
        buffer.release();
    }
}