package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * hcy 2020/3/26
 */
public class LoginFailCmdTest {

    @Test
    public void encoderTo() {
        LoginFailCmd msg = new LoginFailCmd("clientname拉拉", "error msg啦啦啦");
        ByteBuf buffer = Unpooled.buffer();
        msg.encoderTo(buffer);
        assertEquals(Cmd.ServerToClient.loginFail, buffer.readByte());
        assertEquals(msg, LoginFailCmd.decoderFrom(buffer));

    }
}