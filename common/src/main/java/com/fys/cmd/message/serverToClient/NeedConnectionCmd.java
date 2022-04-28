package com.fys.cmd.message.serverToClient;

import com.fys.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;

/**
 * @author hcy
 * @since 2022/4/24 1:02
 */
public class NeedConnectionCmd implements Cmd {
    @Override
    public void encoderTo(ByteBuf buf) {

    }
}
