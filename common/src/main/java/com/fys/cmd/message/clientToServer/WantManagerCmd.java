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
 * 由客户端发给服务端，表明自己是一个管理器连接，并发送想要管理的端口
 * hcy 2020/2/18
 */
public class WantManagerCmd implements Cmd {

    private int serverWorkPort;
    private String localHost;
    private int localPort;
    private byte[] md5;

    public WantManagerCmd(int serverWorkPort, String localHost, int localPort, String password) {
        this.serverWorkPort = serverWorkPort;
        this.localHost = localHost;
        this.localPort = localPort;
        this.md5 = md5(serverWorkPort + localHost + localPort + password);
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.wantManagerCmd); //标志位
        buf.writeShort(serverWorkPort);             //服务端口
        buf.writeShort(ByteBufUtil.utf8Bytes(localHost));         //本地host 长度
        buf.writeCharSequence(localHost, UTF_8);    //本地host
        buf.writeShort(localPort);                  //本地端口
        buf.writeBytes(md5);                        //md5
    }


    public static WantManagerCmd decoderFrom(ByteBuf in, String password) {
        int serverWorkPort = in.readUnsignedShort();
        CharSequence localHost = in.readCharSequence(in.readUnsignedShort(), UTF_8);
        int localPort = in.readUnsignedShort();
        ByteBuf md5 = in.readBytes(16);

        byte[] serverMd5 = md5(serverWorkPort + localHost.toString() + localPort + password);
        if (ByteBufUtil.equals(md5, Unpooled.wrappedBuffer(serverMd5))) {
            return new WantManagerCmd(serverWorkPort, localHost.toString(), localPort, password);
        }
        throw AuthenticationException.INSTANCE;
    }

    @Override
    public int getServerPort() {
        return serverWorkPort;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }

    @Override
    public String getLocalHost() {
        return localHost;
    }

    @Override
    public String toString() {
        return "{" +
                "serverWorkPort=" + serverWorkPort +
                ", localHost='" + localHost + '\'' +
                ", localPort=" + localPort +
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
