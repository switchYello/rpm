package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/18
 * 服务端向客户端下发需要新连接请求，同时将服务端id发送给客户端
 * 客户端新连接时将id待会，服务点据此识别是哪一个客户端连接的
 */
public class NeedCreateNewConnectionCmd implements Cmd {

    private static Logger log = LoggerFactory.getLogger(NeedCreateNewConnectionCmd.class);
    
    @Override
    public ByteBuf toByte() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(ServerToClient.needCreateNewConnectionCmd);
        return buffer;
    }

}
