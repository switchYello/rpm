package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/3/26
 */
public class LoginFailCmd implements Cmd {
    private String clientName;
    private String failMsg;

    public LoginFailCmd(String clientName, String failMsg) {
        this.clientName = clientName;
        this.failMsg = failMsg;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(ServerToClient.loginFail);
        buf.writeInt(ByteBufUtil.utf8Bytes(clientName));
        buf.writeCharSequence(clientName, UTF_8);
        buf.writeInt(ByteBufUtil.utf8Bytes(failMsg));
        buf.writeCharSequence(failMsg, UTF_8);
    }

    public static LoginFailCmd decoderFrom(ByteBuf in) {
        CharSequence clientName = in.readCharSequence(in.readInt(), UTF_8);
        CharSequence failMsg = in.readCharSequence(in.readInt(), UTF_8);
        return new LoginFailCmd(clientName.toString(), failMsg.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginFailCmd that = (LoginFailCmd) o;
        return Objects.equals(clientName, that.clientName) &&
                Objects.equals(failMsg, that.failMsg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientName, failMsg);
    }

    @Override
    public String toString() {
        return "登录失败:[clientName:+" + clientName + ",+FailMsg:" + failMsg + "]";
    }
}
