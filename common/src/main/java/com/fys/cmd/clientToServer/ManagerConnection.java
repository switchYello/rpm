package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 由客户端发给服务端，表明自己是一个管理器连接，并发送想要管理的端口
 * hcy 2020/2/18
 */
public class ManagerConnection implements Cmd {

    private int serverWorkPort;

    public ManagerConnection(int serverWorkPort) {
        this.serverWorkPort = serverWorkPort;
    }

    @Override
    public ByteBuf toByte() {
        return Unpooled.buffer().writeByte(Cmd.managerCmd).writeShort(serverWorkPort);
    }

}
