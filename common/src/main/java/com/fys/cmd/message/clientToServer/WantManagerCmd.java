package com.fys.cmd.message.clientToServer;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 由客户端发给服务端，表明自己是一个管理器连接，并发送想要管理的端口
 * hcy 2020/2/18
 */
public class WantManagerCmd implements Cmd {

    private short serverWorkPort;
    private String localHost;
    private short localPort;

    public WantManagerCmd(short serverWorkPort, String localHost, short localPort) {
        this.serverWorkPort = serverWorkPort;
        this.localHost = localHost;
        this.localPort = localPort;
    }

    public WantManagerCmd(int serverWorkPort, String localHost, int localPort) {
        this((short) serverWorkPort, localHost, (short) localPort);
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.wantManagerCmd);
        buf.writeShort(serverWorkPort);
        buf.writeShort(localHost.length());
        buf.writeCharSequence(localHost, UTF_8);
        buf.writeShort(localPort);
    }


    public static WantManagerCmd decoderFrom(ByteBuf in) {
        short serverWorkPort = in.readShort();
        short localHostLength = in.readShort();
        CharSequence localHost = in.readCharSequence(localHostLength, UTF_8);
        short localPort = in.readShort();
        return new WantManagerCmd(serverWorkPort, localHost.toString(), localPort);
    }

    @Override
    public short getServerPort() {
        return serverWorkPort;
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
        return "{" +
                "serverWorkPort=" + serverWorkPort +
                ", localHost='" + localHost + '\'' +
                ", localPort=" + localPort +
                '}';
    }
}
