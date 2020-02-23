package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/18
 */
public class DataConnectionCmd implements Cmd {

    private short serverPort;
    private String localHost;
    private short localPort;
    private long token;

    public DataConnectionCmd(short serverPort, String localHost, short localPort, long token) {
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
        short serverPort = in.readShort();
        CharSequence localHost = in.readCharSequence(in.readShort(), UTF_8);
        short localPort = in.readShort();
        long token = in.readLong();
        return new DataConnectionCmd(serverPort, localHost.toString(), localPort, token);
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
