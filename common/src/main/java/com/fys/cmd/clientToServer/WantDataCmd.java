package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * hcy 2020/2/18
 */
public class WantDataCmd implements Cmd {

    private static Logger log = LoggerFactory.getLogger(WantDataCmd.class);
    private String serverId;

     public WantDataCmd(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public ByteBuf toByte() {
        log.info("client-> 服务器 此连接指定服务id:{}", serverId);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(ClientToServer.wantDataCmd).writeCharSequence(serverId, StandardCharsets.UTF_8);
        return buffer;
    }


}
