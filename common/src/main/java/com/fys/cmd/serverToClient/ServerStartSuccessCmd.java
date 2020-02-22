package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/19
 */
public class ServerStartSuccessCmd implements Cmd {

    private short serverPort;
    private String localHost;
    private short localPort;

    public ServerStartSuccessCmd(short serverPort, String localHost, short localPort) {
        this.serverPort = serverPort;
        this.localHost = localHost;
        this.localPort = localPort;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ServerToClient.serverStartSuccessCmd);
        buf.writeShort(serverPort);
        buf.writeShort(localHost.length());
        buf.writeCharSequence(localHost, UTF_8);
        buf.writeShort(localPort);
    }

    public static ServerStartSuccessCmd decoderFrom(ByteBuf in) {
        short serverPort = in.readShort();
        short localHostLength = in.readShort();
        CharSequence localHost = in.readCharSequence(localHostLength, UTF_8);
        short localPort = in.readShort();
        return new ServerStartSuccessCmd(serverPort, localHost.toString(), localPort);
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
        return "ServerStartSuccessCmd{" +
                "serverPort=" + serverPort +
                ", localHost='" + localHost + '\'' +
                ", localPort=" + localPort +
                '}';
    }
}
