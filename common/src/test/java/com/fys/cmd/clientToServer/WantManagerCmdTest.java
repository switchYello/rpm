package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * hcy 2020/2/22
 */
public class WantManagerCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        WantManagerCmd src = new WantManagerCmd(80, UUID.randomUUID().toString());
        src.encoderTo(buffer);
        assertEquals(Cmd.ClientToServer.wantManagerCmd,buffer.readByte());
        WantManagerCmd dec = WantManagerCmd.decoderFrom(buffer);
        assertEquals(src.getClientName(), dec.getClientName());
        assertEquals(src.getServerWorkPort(), dec.getServerWorkPort());
    }
}