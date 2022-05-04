package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author hcy
 * @since 2022/5/3 16:21
 * 由服务端发送到客户端，表明需要一个指向此地址的连接
 */
public class NeedDataConnectionCmd implements Cmd {

    private String localHost;
    private int localPort;
    private long sessionId;

    public NeedDataConnectionCmd(String localHost, int localPort) {
        this.localHost = localHost;
        this.localPort = localPort;
        this.sessionId = System.nanoTime();
    }

    //length host
    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(ServerToClient.NEED_DATA_CONNECTION_CMD);  //flag
        buf.writeShort(ByteBufUtil.utf8Bytes(localHost)); //host length
        buf.writeCharSequence(localHost, UTF_8); //host
        buf.writeShort(localPort); //port
        buf.writeLong(sessionId); //sessionId
    }

    public static NeedDataConnectionCmd decoderFrom(ByteBuf in) {
        short hostLength = in.readShort();
        CharSequence localHost = in.readCharSequence(hostLength, UTF_8);
        int localPort = in.readUnsignedShort();
        long token = in.readLong();
        NeedDataConnectionCmd cmd = new NeedDataConnectionCmd(localHost.toString(), localPort);
        cmd.sessionId = token;
        return cmd;
    }

    public String getLocalHost() {
        return localHost;
    }

    public int getLocalPort() {
        return localPort;
    }

    public long getSessionId() {
        return sessionId;
    }
}
