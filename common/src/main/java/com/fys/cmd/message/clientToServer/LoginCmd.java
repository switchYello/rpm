package com.fys.cmd.message.clientToServer;

import com.fys.cmd.exception.AuthenticationException;
import com.fys.cmd.message.Cmd;
import com.fys.cmd.util.CodeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.Arrays;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/3/25
 * 登录
 */
public class LoginCmd implements Cmd {

    private String clientName;
    private String serverToken;

    public LoginCmd(String clientName, String serverToken) {
        this.clientName = clientName;
        this.serverToken = serverToken;
    }

    // flag 长度 clientName md5
    @Override
    public void encoderTo(ByteBuf buf) {
        long timeStamp = System.currentTimeMillis();
        byte[] md5 = CodeUtil.md5((clientName + timeStamp + serverToken).getBytes(UTF_8)); //用户名加时间戳加token取md5，md5是128位
        buf.writeInt(ClientToServer.login);
        buf.writeInt(ByteBufUtil.utf8Bytes(clientName)); //用户名长度
        buf.writeCharSequence(clientName, UTF_8); //用户名
        buf.writeLong(timeStamp); //时间戳
        buf.writeBytes(md5); //签名
    }

    public static LoginCmd decoderFrom(ByteBuf in, String serverToken) {
        int clientNameLength = in.readInt();
        if (clientNameLength > 50) {
            throw new AuthenticationException();
        }
        String clientName = in.readCharSequence(clientNameLength, UTF_8).toString();
        long timeStamp = in.readLong();
        byte[] readMd5 = in.readBytes(128).array();

        byte[] md5 = CodeUtil.md5((clientName + timeStamp + serverToken).getBytes(UTF_8));
        if (Arrays.equals(readMd5, md5)) {
            return new LoginCmd(clientName, serverToken);
        }
        throw new AuthenticationException();
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
