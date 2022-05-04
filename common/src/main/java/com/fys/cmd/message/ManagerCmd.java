package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * @author hcy
 * @since 2022/5/4 12:10
 * 标识该连接是管理连接
 */
public class ManagerCmd implements Cmd {

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(ClientToServer.MANAGER_CMD);
    }

}
