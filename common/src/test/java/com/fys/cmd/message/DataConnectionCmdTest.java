package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * hcy 2020/2/23
 */
public class DataConnectionCmdTest {

    @Test
    public void encoderTo() {
        ByteBuf buffer = Unpooled.buffer();
        DataConnectionCmd src = new DataConnectionCmd("127.0.2.8", 80);
        src.encoderTo(buffer);
        Assertions.assertEquals(Cmd.dataConnectionCmd, buffer.readByte());
        DataConnectionCmd dec = DataConnectionCmd.decoderFrom(buffer);
        Assertions.assertEquals(src.getLocalHost(), dec.getLocalHost());
        Assertions.assertEquals(src.getLocalPort(), dec.getLocalPort());
        Assertions.assertEquals(src.getSessionId(), dec.getSessionId());
        buffer.release();
    }
}