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
    int rawData = prefix | 'a';

    //ping pong
    int ping = prefix | 'p';
    int pong = prefix | 'd';

    //登陆失败
    interface ServerToClient {
        //登录认证失败
        int loginFail = prefix | 'f';
        //获取新连接
        int needDataConnectionCmd = prefix | 'd';
        //开始数据传输
        int startTransactionCmd = prefix | 't';
    }

    //发起登录
    interface ClientToServer {
        int login = prefix | (byte) 'l';
        //新连接
        int newDataConnectionCmd = prefix | 'n';
    }

    /*
     * 将当前对象序列化到Bytebuf中
     * */
    void encoderTo(ByteBuf buf);

}
