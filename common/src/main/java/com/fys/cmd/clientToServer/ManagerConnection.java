package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * 由客户端发给服务端，表明自己是一个管理器连接，并发送想要管理的端口
 * hcy 2020/2/18
 */
public class ManagerConnection implements Cmd {

    private int serverWorkPort;
    private String name;

    public ManagerConnection(int serverWorkPort, String name) {
        this.serverWorkPort = serverWorkPort;
        this.name = name;
    }

    @Override
    public ByteBuf toByte() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(Cmd.managerCmd).writeShort(serverWorkPort).writeCharSequence(name, StandardCharsets.UTF_8);
        return buffer;
    }

}
