package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/10
 * 消息总是由 长度 + data 的形式组成的,不同消息的data形式不同,看具体实现
 */
public interface Cmd {

    byte dataConnectionCmd = 0;

    interface ServerToClient {
        byte serverStartSuccessCmd = 2;
        byte serverStartFailCmd = 3;
        byte ping = 4;
    }

    interface ClientToServer {
        byte wantManagerCmd = 5;
        byte pong = 6;
        byte login = 7;
    }


    /*
     * 将当前对象序列化到Bytebuf中
     * */
    void encoderTo(ByteBuf buf);

    //发送者的服务器端口
    int getServerPort();

    //想要发送到的客户端端口
    String getLocalHost();

    //想要发送到的客户端端口
    int getLocalPort();


}
