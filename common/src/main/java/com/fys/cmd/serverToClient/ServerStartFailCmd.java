package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 告诉客户端，无法创建server，并返回无法创建的原因
 */
public class ServerStartFailCmd implements Cmd {

    private short serverPort;
    private String localHost;
    private short localPort;
    private String failMsg;

    public ServerStartFailCmd(short serverPort, String localHost, short localPort, String failMsg) {
        this.serverPort = serverPort;
        this.localHost = localHost;
        this.localPort = localPort;
        this.failMsg = failMsg;
    }


    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ServerToClient.serverStartFailCmd);
        buf.writeShort(serverPort);
        buf.writeShort(localHost.length());
        buf.writeCharSequence(localHost, UTF_8);
        buf.writeShort(localPort);
        buf.writeShort(failMsg.length());
        buf.writeCharSequence(failMsg, UTF_8);
    }

    public static ServerStartFailCmd decoderFrom(ByteBuf in) {
        short serverPort = in.readShort();
        short localHostLength = in.readShort();
        CharSequence localHost = in.readCharSequence(localHostLength, UTF_8);
        short localPort = in.readShort();
        short msgLength = in.readShort();
        CharSequence charSequence = in.readCharSequence(msgLength, UTF_8);
        return new ServerStartFailCmd(serverPort, localHost.toString(), localPort, charSequence.toString());
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

    public String getFailMsg() {
        return failMsg;
    }


    @Override
    public String toString() {
        return "Server [" + serverPort + " -> " + localPort + "]开启失败,因为" + failMsg;
    }
}
