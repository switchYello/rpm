package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * @author hcy
 * @since 2022/5/3 16:22
 * 由服务端发送到客户端，表示开始传输数据
 */
public class StartTransactionCmd implements Cmd {

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(ServerToClient.START_TRANSACTION_CMD);
    }

}
