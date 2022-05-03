package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/10
 * 消息总是由 长度 + data 的形式组成的,不同消息的data形式不同,看具体实现
 */
public interface Cmd {

    int prefix = 'R' << 24 | 'P' << 16 | 'M' << 8;
    //int prefix = 0;

    //raw数据
    int rawData = prefix | 1;

    //ping pong
    int ping = prefix | 2;
    int pong = prefix | 3;

    //登陆失败
    interface ServerToClient {
        //登录认证失败
        int loginFail = prefix | 4;
        //获取新连接
        int needDataConnectionCmd = prefix | 5;
        //开始数据传输
        int startTransactionCmd = prefix | 6;
    }

    //发起登录
    interface ClientToServer {
        int login = prefix | 7;
        //新连接
        int newDataConnectionCmd = prefix | 8;
    }

    /*
     * 将当前对象序列化到Bytebuf中
     * */
    void encoderTo(ByteBuf buf);

}
