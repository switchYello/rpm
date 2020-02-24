package com.fys.cmd.message.clientToServer;

import com.fys.cmd.exception.AuthenticationException;
import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 由客户端发给服务端，表明自己是一个管理器连接，并发送想要管理的端口
 * hcy 2020/2/18
 */
public class WantManagerCmd implements Cmd {

    private int serverWorkPort;
    private String localHost;
    private int localPort;
    private int hash;

    public WantManagerCmd(int serverWorkPort, String localHost, int localPort, String password) {
        this.serverWorkPort = serverWorkPort;
        this.localHost = localHost;
        this.localPort = localPort;
        this.hash = (serverWorkPort + localHost + localPort + password).hashCode();
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.wantManagerCmd);
        buf.writeShort(serverWorkPort);
        buf.writeShort(localHost.length());
        buf.writeCharSequence(localHost, UTF_8);
        buf.writeShort(localPort);
        buf.writeInt(hash);
    }


    public static WantManagerCmd decoderFrom(ByteBuf in, String password) {
        int serverWorkPort = in.readUnsignedShort();
        CharSequence localHost = in.readCharSequence(in.readUnsignedShort(), UTF_8);
        int localPort = in.readUnsignedShort();
        int hash = in.readInt();
        if ((serverWorkPort + localHost.toString() + localPort + password).hashCode() == hash) {
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
}
