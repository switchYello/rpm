package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * hcy 2020/2/22
 */
public class ServerStartFailCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        ServerStartFailCmd src = new ServerStartFailCmd(80,"54.21.56.1",  70, UUID.randomUUID().toString());
        src.encoderTo(buffer);
        assertEquals(Cmd.ServerToClient.serverStartFailCmd, buffer.readByte());
        ServerStartFailCmd dec = ServerStartFailCmd.decoderFrom(buffer);
        assertEquals(src.getFailMsg(), dec.getFailMsg());
        assertEquals(src.getLocalPort(), dec.getLocalPort());
        assertEquals(src.getServerPort(), dec.getServerPort());
        assertEquals(src.getLocalHost(), dec.getLocalHost());
        buffer.release();
    }
}