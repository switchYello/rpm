package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/19
 */
public class ServerStartSuccessCmd implements Cmd {

    private int serverPort;
    private String localHost;
    private int localPort;

    public ServerStartSuccessCmd(int serverPort, String localHost, int localPort) {
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
        int serverPort = in.readUnsignedShort();
        int localHostLength = in.readUnsignedShort();
        CharSequence localHost = in.readCharSequence(localHostLength, UTF_8);
        int localPort = in.readUnsignedShort();
        return new ServerStartSuccessCmd(serverPort, localHost.toString(), localPort);
    }

    @Override
    public int getServerPort() {
        return serverPort;
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
        return "ServerStartSuccessCmd{" +
                "serverPort=" + serverPort +
                ", localHost='" + localHost + '\'' +
                ", localPort=" + localPort +
                '}';
    }
}
