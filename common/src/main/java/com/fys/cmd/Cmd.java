package com.fys.cmd;

import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/10
 * 消息总是由 长度 + data 的形式组成的,不同消息的data形式不同,看具体实现
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
