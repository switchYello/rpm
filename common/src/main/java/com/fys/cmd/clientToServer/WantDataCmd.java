package com.fys.cmd.clientToServer;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hcy 2020/2/18
 */
public class WantDataCmd implements Cmd {

    private static Logger log = LoggerFactory.getLogger(WantDataCmd.class);
    private long connectionToken;

    public WantDataCmd(long connectionToken) {
        this.connectionToken = connectionToken;
    }


    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(ClientToServer.wantDataCmd);  //flag
        buf.writeLong(connectionToken);             //Token
    }

    @Override
    public short getServerPort() {
        return 0;
    }

    @Override
    public short getLocalPort() {
        return 0;
    }

    @Override
    public String getLocalHost() {
        return null;
    }

    public static WantDataCmd decoderFrom(ByteBuf in) {
        long token = in.readLong();
        return new WantDataCmd(token);
    }

    public long getConnectionToken() {
        return connectionToken;
    }

    @Override
    public String toString() {
        return "WantDataCmd{" +
                "connectionToken=" + connectionToken +
                '}';
    }
}
