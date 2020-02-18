package com.fys.cmd;

import io.netty.buffer.ByteBuf;

/**
 * hcy 2020/2/10
 * 0 连接命令，需要指定某个端口，此后将此客户端与该端口绑定，表示客户端创建连接并注入池中
 * 1 关闭连接命令，表示客户端想关闭连接，
 */
public interface Cmd {
    //client to server
    byte dataCmd = 0;
    byte managerCmd = 1;

    //server to client
    byte createNewConnection = 3;

    //返回标志位  + 数据的字节数组
    ByteBuf toByte();


}
