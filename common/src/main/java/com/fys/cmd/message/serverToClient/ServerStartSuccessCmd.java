package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.util.Objects;

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
        buf.writeShort(ByteBufUtil.utf8Bytes(localHost));
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerStartSuccessCmd that = (ServerStartSuccessCmd) o;
        return serverPort == that.serverPort &&
                localPort == that.localPort &&
                Objects.equals(localHost, that.localHost);
    }

    @Override
    public int hashCode() {

        return Objects.hash(serverPort, localHost, localPort);
    }

    @Override
    public String toString() {
        return "Server[" + serverPort + "->" + localHost + ":" + localPort + "]开启成功";
    }
}
