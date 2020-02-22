package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * hcy 2020/2/22
 */
public class ServerStartFailCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        ServerStartFailCmd src = new ServerStartFailCmd(UUID.randomUUID().toString());
        src.encoderTo(buffer);
        assertEquals(Cmd.ServerToClient.serverStartFailCmd,buffer.readByte());
        ServerStartFailCmd dec = ServerStartFailCmd.decoderFrom(buffer);
        assertEquals(src.getFailMsg(), dec.getFailMsg());
    }
}