package com.fys.cmd.message;

import com.fys.cmd.util.CodeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

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

    @Override
    public void encoderTo(ByteBuf buf) {
        long timeStamp = System.currentTimeMillis();
        buf.writeInt(ClientToServer.LOGIN);
        buf.writeShort(ByteBufUtil.utf8Bytes(clientName)); //用户名长度
        buf.writeCharSequence(clientName, UTF_8); //用户名
        buf.writeLong(timeStamp); //时间戳
        String md5 = CodeUtil.md5Str((clientName + timeStamp + serverToken).getBytes(UTF_8)); //用户名加时间戳加token取md5，md5是32位长度
        buf.writeCharSequence(md5, UTF_8); //签名 32位固定长度
    }

    //客户端名长度(小于50)  客户端名  时间戳  签名
    public static LoginAuthInfo decoderFrom(ByteBuf in) {
        int clientNameLength = in.readShort();
        String clientName = in.readCharSequence(clientNameLength, UTF_8).toString();
        long timeStamp = in.readLong();
        String md5 = in.readCharSequence(32, UTF_8).toString();
        return new LoginAuthInfo(clientName, timeStamp, md5);
    }

    @Override
    public String toString() {
        return "LoginCmd{" +
                "clientName='" + clientName + '\'' +
                '}';
    }

}
