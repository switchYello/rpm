package com.fys.cmd.message.clientToServer;

import com.fys.cmd.exception.AuthenticationException;
import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/3/25
 * 登录
 */
public class LoginCmd implements Cmd {

    private String clientName;
    private byte[] md5;

    public LoginCmd(String clientName, String token) {
        this.clientName = clientName;
        this.md5 = md5(clientName + token);
    }

    public LoginCmd(String clientName, byte[] md5) {
        this.clientName = clientName;
        this.md5 = md5;
    }

    // flag 长度 clientName md5
    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.login);
        buf.writeInt(ByteBufUtil.utf8Bytes(clientName));
        buf.writeCharSequence(clientName, UTF_8);
        buf.writeBytes(md5);
    }

    public static LoginCmd decoderFrom(ByteBuf in) {
        int clientNameLength = in.readInt();
        CharSequence clientName = in.readCharSequence(clientNameLength, UTF_8);
        byte[] md5 = new byte[16];
        in.readBytes(md5);
        return new LoginCmd(clientName.toString(), md5);
    }

    public String getClientName() {
        return clientName;
    }


    public void check(String token) {
        if (md5 == null || md5.length != 16) {
            throw AuthenticationException.INSTANCE;
        }

        byte[] bytes = md5(clientName + token);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != md5[i]) {
                throw AuthenticationException.INSTANCE;
            }
        }
    }

    @Override
    public String toString() {
        return "LoginCmd{" +
                "clientName='" + clientName + '\'' +
                '}';
    }

    /*
     * MD5算法摘要出的是128bit的数据，等于16Byte数据，转成16进制字符串为32个字符
     * */
    private static byte[] md5(String src) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            return md.digest(src.getBytes(UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[16];
    }

}
