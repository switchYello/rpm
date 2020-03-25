package com.fys.cmd.message.clientToServer;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

/**
 * hcy 2020/3/25
 */
public class LoginCmdTest {
    
    @Test
    public void test() {
        LoginCmd login = new LoginCmd("hakshdjkfsad", "21312365");
        ByteBuf buffer = Unpooled.buffer();
        login.encoderTo(buffer);
        Assert.assertEquals(Cmd.ClientToServer.login, buffer.readByte());
        LoginCmd decode = LoginCmd.decoderFrom(buffer, "21312365");
        Assert.assertEquals(login.getClientName(), decode.getClientName());
    }

}