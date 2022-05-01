package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/10
 * 消息总是由 长度 + data 的形式组成的,不同消息的data形式不同,看具体实现
 */
public interface Cmd {

    int prefix = 'R' << 24 | 'P' << 16 | 'M' << 8;

    //请求数据连接
    int dataConnectionCmd = prefix | (byte) 1;
    //ping pong
    int ping = prefix | (byte) 2;
    int pong = prefix | (byte) 3;

    //登陆失败
    interface ServerToClient {
        int loginFail = prefix | (byte) 6;
    }

    //发起登录
    interface ClientToServer {
        int login = prefix | (byte) 7;
    }

    /*
     * 将当前对象序列化到Bytebuf中
     * */
    void encoderTo(ByteBuf buf);

}
