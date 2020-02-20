package com.fys.cmd;

import com.fys.cmd.clientToServer.WantDataCmd;
import com.fys.cmd.clientToServer.WantManagerCmd;
import com.fys.cmd.clientToServer.Pong;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * hcy 2020/2/10
 * 0 连接命令，需要指定某个端口，此后将此客户端与该端口绑定，表示客户端创建连接并注入池中
 * 1 关闭连接命令，表示客户端想关闭连接，
 */
public interface Cmd {

    interface ClientToServer {
        byte wantDataCmd = 0;
        byte wantManagerCmd = 1;
        byte pong = 5;
    }

    interface ServerToClient {
        byte needCreateNewConnectionCmd = 3;
        byte ping = 4;
        byte serverStartFailCmd = 6;
        byte serverStartSuccessCmd = 7;
    }

    //返回标志位  + 数据的字节数组
    ByteBuf toByte();

    static Cmd encoder(byte code, ByteBuf msg) {
        if (code == ClientToServer.wantDataCmd) {
            return new WantDataCmd(msg.readCharSequence(msg.readableBytes(), StandardCharsets.UTF_8).toString());
        }
        if (code == ClientToServer.wantManagerCmd) {
            return new WantManagerCmd(msg.readShort(), msg.readCharSequence(msg.readableBytes() - 2, StandardCharsets.UTF_8).toString());
        }
        if (code == ClientToServer.pong) {
            return new Pong();
        }

        throw new RuntimeException("无法识别的指令");
    }


}
