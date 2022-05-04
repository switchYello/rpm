package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.LoginFailCmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * hcy 2020/3/26
 */
public class LoginFailCmdTest {

    @Test
    public void encoderTo() {
        LoginFailCmd msg = new LoginFailCmd("clientname拉拉", "error msg啦啦啦");
        ByteBuf buffer = Unpooled.buffer();
        msg.encoderTo(buffer);
        Assertions.assertEquals(Cmd.ServerToClient.LOGIN_FAIL, buffer.readByte());
        Assertions.assertEquals(msg, LoginFailCmd.decoderFrom(buffer));

    }
}