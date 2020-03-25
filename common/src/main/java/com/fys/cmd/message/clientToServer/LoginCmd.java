package com.fys.cmd.message.clientToServer;

import com.fys.cmd.exception.AuthenticationException;
import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/3/25
 * 登录
 */
public class LoginCmd implements Cmd {

    private String clientName;
    private String token;

    public LoginCmd(String clientName, String token) {
        this.clientName = clientName;
        this.token = token;
    }

    // flag 长度 clientName md5
    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.login);
        buf.writeInt(ByteBufUtil.utf8Bytes(clientName));
        buf.writeCharSequence(clientName, UTF_8);
        buf.writeBytes(md5(clientName + token));
    }

    public static LoginCmd decoderFrom(ByteBuf in, String password) {
        int clientNameLength = in.readInt();
        CharSequence clientName = in.readCharSequence(clientNameLength, UTF_8);
        ByteBuf md5 = in.readBytes(16);

        byte[] bytes = md5(clientName + password);
        if (ByteBufUtil.equals(md5, Unpooled.wrappedBuffer(bytes))) {
            return new LoginCmd(clientName.toString(), password);
        }
        throw AuthenticationException.INSTANCE;
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public String getLocalHost() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
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
