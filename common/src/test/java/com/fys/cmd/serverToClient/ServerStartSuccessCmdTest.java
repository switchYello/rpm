package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * hcy 2020/2/22
 */
public class ServerStartSuccessCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        ServerStartSuccessCmd src = new ServerStartSuccessCmd(UUID.randomUUID().toString());
        src.encoderTo(buffer);
        assertEquals(Cmd.ServerToClient.serverStartSuccessCmd, buffer.readByte());
        ServerStartSuccessCmd dec = ServerStartSuccessCmd.decoderFrom(buffer);
        assertEquals(src.getServerId(), dec.getServerId());
    }
}