package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * hcy 2020/2/18
 */
public class WantDataCmd implements Cmd {

    private static Logger log = LoggerFactory.getLogger(WantDataCmd.class);
    private String serverId;
    private long connectionToken;

    public WantDataCmd(long connectionToken, String serverId) {
        this.connectionToken = connectionToken;
        this.serverId = serverId;
    }


    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.wantDataCmd);  //flag
        buf.writeShort(serverId.length());          //serverId Length
        buf.writeCharSequence(serverId, UTF_8);     //serverId
        buf.writeLong(connectionToken);             //Token
    }

    public static WantDataCmd decoderFrom(ByteBuf in) {
        short serverIdLength = in.readShort();
        CharSequence serverIdSequence = in.readCharSequence(serverIdLength, UTF_8);
        long token = in.readLong();
        return new WantDataCmd(token, serverIdSequence.toString());
    }

    public String getServerId() {
        return serverId;
    }

    public long getConnectionToken() {
        return connectionToken;
    }

    @Override
    public String toString() {
        return "{" +
                "serverId='" + serverId + '\'' +
                ", connectionToken=" + connectionToken +
                '}';
    }
}
