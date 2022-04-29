package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/18
 */
public final class DataConnectionCmd implements Cmd {

    private String localHost;
    private int localPort;
    private long sessionId;

    public DataConnectionCmd(String localHost, int localPort) {
        this(localHost, localPort, System.nanoTime());
    }

    private DataConnectionCmd(String localHost, int localPort, long sessionId) {
        this.localHost = localHost;
        this.localPort = localPort;
        this.sessionId = sessionId;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(Cmd.dataConnectionCmd);  //flag
        buf.writeShort(localHost.length());
        buf.writeCharSequence(localHost, UTF_8);
        buf.writeShort(localPort);
        buf.writeLong(sessionId);
    }

    public static DataConnectionCmd decoderFrom(ByteBuf in) {
        CharSequence localHost = in.readCharSequence(in.readShort(), UTF_8);
        int localPort = in.readUnsignedShort();
        long token = in.readLong();
        return new DataConnectionCmd(localHost.toString(), localPort, token);
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getLocalHost() {
        return localHost;
    }

    public long getSessionId() {
        return sessionId;
    }

}
