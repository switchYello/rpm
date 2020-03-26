package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 告诉客户端，无法创建server，并返回无法创建的原因
 */
public class ServerStartFailCmd implements Cmd {

    private int serverPort;
    private String localHost;
    private int localPort;
    private String failMsg;

    public ServerStartFailCmd(int serverPort, String localHost, int localPort, String failMsg) {
        this.serverPort = serverPort;
        this.localHost = localHost;
        this.localPort = localPort;
        this.failMsg = failMsg;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ServerToClient.serverStartFailCmd);
        buf.writeShort(serverPort);
        buf.writeShort(ByteBufUtil.utf8Bytes(localHost));
        buf.writeCharSequence(localHost, UTF_8);
        buf.writeShort(localPort);
        buf.writeShort(ByteBufUtil.utf8Bytes(failMsg));
        buf.writeCharSequence(failMsg, UTF_8);
    }

    public static ServerStartFailCmd decoderFrom(ByteBuf in) {
        int serverPort = in.readUnsignedShort();
        int localHostLength = in.readUnsignedShort();
        CharSequence localHost = in.readCharSequence(localHostLength, UTF_8);
        int localPort = in.readUnsignedShort();
        int msgLength = in.readUnsignedShort();
        CharSequence charSequence = in.readCharSequence(msgLength, UTF_8);
        return new ServerStartFailCmd(serverPort, localHost.toString(), localPort, charSequence.toString());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerStartFailCmd that = (ServerStartFailCmd) o;
        return serverPort == that.serverPort &&
                localPort == that.localPort &&
                Objects.equals(localHost, that.localHost) &&
                Objects.equals(failMsg, that.failMsg);
    }

    @Override
    public int hashCode() {

        return Objects.hash(serverPort, localHost, localPort, failMsg);
    }

    @Override
    public String toString() {
        return "Server[" + serverPort + "->" + localHost + ":" + localPort + "]开启失败，因为" + failMsg;
    }
}
