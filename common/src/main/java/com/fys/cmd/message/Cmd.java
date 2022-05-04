package com.fys.cmd.message;

import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/10
 * 消息总是由 长度 + data 的形式组成的,不同消息的data形式不同,看具体实现
 */
public interface Cmd {

    int PREFIX = 'R' << 24 | 'P' << 16 | 'M' << 8;
    //int PREFIX = 0;

    //raw数据
    int RAW_DATA = PREFIX | 1;

    //ping pong
    int PING = PREFIX | 2;
    int PONG = PREFIX | 3;

    //登陆失败
    interface ServerToClient {
        //登录认证失败
        int LOGIN_FAIL = PREFIX | 4;
        //获取新连接
        int NEED_DATA_CONNECTION_CMD = PREFIX | 5;
        //开始数据传输
        int START_TRANSACTION_CMD = PREFIX | 6;
    }

    //发起登录
    interface ClientToServer {
        int LOGIN = PREFIX | 7;
        //新连接
        int NEW_DATA_CONNECTION_CMD = PREFIX | 8;
        //标识Manager连接
        int MANAGER_CMD = PREFIX | 9;
        //标识数据连接
        int DATA_CMD = PREFIX | 10;
    }

    /*
     * 将当前对象序列化到Bytebuf中
     * */
    void encoderTo(ByteBuf buf);

}
