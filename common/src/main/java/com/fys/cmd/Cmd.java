package com.fys.cmd;

import io.netty.buffer.ByteBuf;

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

    //返回 标志位 + 数据的字节数组
    ByteBuf toByte();

}
