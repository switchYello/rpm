package com.fys.cmd.serverToClient;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * hcy 2020/2/18
 */
public class NeedNewConnectionCmd implements Cmd {

    private static Logger log = LoggerFactory.getLogger(NeedNewConnectionCmd.class);
    private String id;

    public NeedNewConnectionCmd(String id) {
        this.id = id;
    }

    @Override
    public ByteBuf toByte() {
        log.info("服务器-> client 需要新的连接id:{}", id);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(Cmd.needCreateNewConnection).writeCharSequence(id, StandardCharsets.UTF_8);
        return buffer;
    }


}
