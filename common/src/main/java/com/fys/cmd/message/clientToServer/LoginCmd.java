package com.fys.cmd.message.clientToServer;

import com.fys.cmd.exception.AuthenticationException;
import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/3/25
 * 登录
 */
public class LoginCmd implements Cmd {

    private String clientName;

    public LoginCmd(String clientName) {
        this.clientName = clientName;
    }

    // flag 长度 clientName md5
    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(ClientToServer.login);
        buf.writeInt(ByteBufUtil.utf8Bytes(clientName));
        buf.writeCharSequence(clientName, UTF_8);
    }

    public static LoginCmd decoderFrom(ByteBuf in) {
        int clientNameLength = in.readInt();
        if (clientNameLength > 50) {
            throw new AuthenticationException();
        }
        CharSequence clientName = in.readCharSequence(clientNameLength, UTF_8);
        return new LoginCmd(clientName.toString());
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginCmd loginCmd = (LoginCmd) o;
        return Objects.equals(clientName, loginCmd.clientName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(clientName);
    }

    @Override
    public String toString() {
        return "LoginCmd{" +
                "clientName='" + clientName + '\'' +
                '}';
    }

}
