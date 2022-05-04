package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * @author hcy
 * @since 2022/5/3 16:22
 */
public class StartTransactionCmd implements Cmd {

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeInt(ServerToClient.START_TRANSACTION_CMD);
    }

    public static StartTransactionCmd decoderFrom(ByteBuf in) {
        return new StartTransactionCmd();
    }

}
