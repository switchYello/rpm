package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/18
 */
public class WantDataCmd implements Cmd {

    private short serverPort;
    private String localHost;
    private short localPort;

    public WantDataCmd(short serverPort, String localHost, short localPort) {
        this.serverPort = serverPort;
        this.localHost = localHost;
        this.localPort = localPort;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.wantDataCmd);  //flag
        buf.writeShort(serverPort);             //Token
        buf.writeShort(localHost.length());
        buf.writeCharSequence(localHost, UTF_8);
        buf.writeShort(localPort);
    }

    public static WantDataCmd decoderFrom(ByteBuf in) {
        short serverPort = in.readShort();
        CharSequence localHost = in.readCharSequence(in.readShort(), UTF_8);
        short localPort = in.readShort();
        return new WantDataCmd(serverPort, localHost.toString(), localPort);
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
        return "WantDataCmd{" +
                "serverPort=" + serverPort +
                ", localHost='" + localHost + '\'' +
                ", localPort=" + localPort +
                '}';
    }
}
