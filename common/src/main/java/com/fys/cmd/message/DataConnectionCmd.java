package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/18
 */
public class DataConnectionCmd implements Cmd {

    private int serverPort;
    private String localHost;
    private int localPort;
    private long token;

    public DataConnectionCmd(int serverPort, String localHost, int localPort, long token) {
        this.serverPort = serverPort;
        this.localHost = localHost;
        this.localPort = localPort;
        this.token = token;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(Cmd.dataConnectionCmd);  //flag
        buf.writeShort(serverPort);
        buf.writeShort(localHost.length());
        buf.writeCharSequence(localHost, UTF_8);
        buf.writeShort(localPort);
        buf.writeLong(token);
    }

    public static DataConnectionCmd decoderFrom(ByteBuf in) {
        int serverPort = in.readUnsignedShort();
        CharSequence localHost = in.readCharSequence(in.readShort(), UTF_8);
        int localPort = in.readUnsignedShort();
        long token = in.readLong();
        return new DataConnectionCmd(serverPort, localHost.toString(), localPort, token);
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

    public long getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "DataConnectionCmd{" +
                "serverPort=" + serverPort +
                ", localHost='" + localHost + '\'' +
                ", localPort=" + localPort +
                ", token=" + token +
                '}';
    }

}
