package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 由客户端发给服务端，表明自己是一个管理器连接，并发送想要管理的端口
 * hcy 2020/2/18
 */
public class WantManagerCmd implements Cmd {

    private int serverWorkPort;
    private String clientName;

    public WantManagerCmd(int serverWorkPort, String clientName) {
        this.serverWorkPort = serverWorkPort;
        this.clientName = clientName;
    }

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.wantManagerCmd);
        buf.writeShort(serverWorkPort);
        buf.writeShort(clientName.length());
        buf.writeCharSequence(clientName, UTF_8);
    }
    
    public static WantManagerCmd decoderFrom(ByteBuf in) {
        short serverWorkPort = in.readShort();
        short clientNameLength = in.readShort();
        CharSequence clientName = in.readCharSequence(clientNameLength, UTF_8);
        return new WantManagerCmd(serverWorkPort, clientName.toString());
    }

    public int getServerWorkPort() {
        return serverWorkPort;
    }

    public String getClientName() {
        return clientName;
    }
}
