package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/10
 * 消息总是由 长度 + data 的形式组成的,不同消息的data形式不同,看具体实现
 */
public interface Cmd {

    byte dataConnectionCmd = 0;
    byte ping = 4;
    byte pong = 6;


    interface ServerToClient {
        byte serverStartSuccessCmd = 2;
        byte serverStartFailCmd = 3;
        byte loginFail = 7;
    }

    interface ClientToServer {
        byte login = 5;
    }


    /*
     * 将当前对象序列化到Bytebuf中
     * */
    void encoderTo(ByteBuf buf);

}
