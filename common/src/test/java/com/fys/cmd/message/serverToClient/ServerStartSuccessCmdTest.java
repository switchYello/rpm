package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
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
        ServerStartSuccessCmd src = new ServerStartSuccessCmd(70, "127.0.5.7", 50);
        src.encoderTo(buffer);
        assertEquals(Cmd.ServerToClient.serverStartSuccessCmd, buffer.readByte());
        ServerStartSuccessCmd dec = ServerStartSuccessCmd.decoderFrom(buffer);
        assertEquals(src, dec);
        buffer.release();
    }
}