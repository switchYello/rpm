package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * hcy 2020/2/19
 */
public class ServerStartSuccessCmd implements Cmd {


    @Override
    public ByteBuf toByte() {
        return Unpooled.buffer().writeByte(ServerToClient.serverStartSuccessCmd);
    }

}
