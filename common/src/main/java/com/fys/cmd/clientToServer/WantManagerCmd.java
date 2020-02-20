package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * 由客户端发给服务端，表明自己是一个管理器连接，并发送想要管理的端口
 * hcy 2020/2/18
 */
public class WantManagerCmd implements Cmd {

    private int serverWorkPort;
    private String clentName;

    public WantManagerCmd(int serverWorkPort, String clentName) {
        this.serverWorkPort = serverWorkPort;
        this.clentName = clentName;
    }

    @Override
    public ByteBuf toByte() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(ClientToServer.wantManagerCmd).writeShort(serverWorkPort).writeCharSequence(clentName, StandardCharsets.UTF_8);
        return buffer;
    }

    public int getServerWorkPort() {
        return serverWorkPort;
    }

    public String getClentName() {
        return clentName;
    }
}
