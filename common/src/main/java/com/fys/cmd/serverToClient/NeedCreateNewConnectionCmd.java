package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * hcy 2020/2/18
 * 服务端向客户端下发需要新连接请求，同时将服务端id发送给客户端
 * 客户端新连接时将id待会，服务点据此识别是哪一个客户端连接的
 */
public class NeedCreateNewConnectionCmd implements Cmd {

    //服务端让客户端创建新连接，客户端收到后，将此token原样带回，标识哪个服务端请求的
    private long connectionToken;

    public NeedCreateNewConnectionCmd(long connectionToken) {
        this.connectionToken = connectionToken;
    }

    @Override
    public ByteBuf toByte() {
        ByteBuf buffer = Unpooled.buffer(9);
        buffer.writeByte(ServerToClient.needCreateNewConnectionCmd);
        buffer.writeLong(connectionToken);
        return buffer;
    }

    public long getConnectionToken() {
        return connectionToken;
    }
}
