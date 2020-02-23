package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/18
 * 服务端向客户端下发需要新连接请求，同时将服务端id发送给客户端
 * 客户端新连接时将id待会，服务点据此识别是哪一个客户端连接的
 */
public class NeedCreateNewConnectionCmd implements Cmd {

    private short serverPort;
    private String localHost;
    private short localPort;

    //意思是 servePort端口号所在的服务想要客户端连接到 localHost:localPort 上
    public NeedCreateNewConnectionCmd(short serverPort, String localHost, short localPort) {
        this.serverPort = serverPort;
        this.localHost = localHost;
        this.localPort = localPort;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ServerToClient.needCreateNewConnectionCmd);
        buf.writeShort(serverPort);
        buf.writeShort(localHost.length());
        buf.writeCharSequence(localHost, UTF_8);
        buf.writeShort(localPort);
    }

    public static NeedCreateNewConnectionCmd decoderFrom(ByteBuf in) {
        short serverPort = in.readShort();
        short localHostLength = in.readShort();
        CharSequence localHost = in.readCharSequence(localHostLength, UTF_8);
        short localPort = in.readShort();
        return new NeedCreateNewConnectionCmd(serverPort, localHost.toString(), localPort);
    }

    @Override
    public short getServerPort() {
        return serverPort;
    }

    @Override
    public short getLocalPort() {
        return localPort;
    }

    @Override
    public String getLocalHost() {
        return localHost;
    }

    @Override
    public String toString() {
        return "NeedCreateNewConnectionCmd{" +
                "serverPort=" + serverPort +
                ", localHost='" + localHost + '\'' +
                ", localPort=" + localPort +
                '}';
    }
}
