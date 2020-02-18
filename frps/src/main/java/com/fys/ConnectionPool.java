package com.fys;

import com.fys.cmd.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * hcy 2020/2/17
 * 保存客户端的连接池
 */
public class ConnectionPool {

    private static Logger log = LoggerFactory.getLogger(ConnectionPool.class);
    private static EventLoopGroup work = App.work;
    public static ChannelHandlerContext managerHandlerContext;

    private static Map<Server, Queue<Promise<ChannelHandlerContext>>> hasConnectionPromises = new HashMap<>();
    private static Map<Server, Queue<Promise<ChannelHandlerContext>>> noConnectionPromises = new HashMap<>();


    //如果池中存在连接，则优先使用池中的，否则创建promise存入等待队列中
    public static Promise<ChannelHandlerContext> getConnection(Server server) {

        if (hasConnectionPromises.getOrDefault(server, new LinkedList<>()).isEmpty()) {
            Promise<ChannelHandlerContext> promise = new DefaultPromise<>(work.next());
            ByteBuf createNewConnection = Unpooled.buffer().writeInt(1).writeByte(Cmd.createNewConnection);
            managerHandlerContext.writeAndFlush(createNewConnection).addListener(future -> {
                if (future.isSuccess()) {
                    noConnectionPromises.getOrDefault(server, new LinkedList<>()).add(promise);
                } else {
                    log.info("向客户端发送创建连接指令失败");
                    promise.setFailure(future.cause());
                }
            });
            return promise;
        } else {
            return hasConnectionPromises.get(server).poll();
        }
    }

    //添加客户端连接到池中，如果有等待需要的promise则优先给promise
    public static void addConnection(ChannelHandlerContext context) {
        if (noConnectionPromises.isEmpty()) {
            Promise<ChannelHandlerContext> promise = new DefaultPromise<>(work.next());
            promise.setSuccess(context);
            hasConnectionPromises.add(promise);
        } else {
            noConnectionPromises.poll().setSuccess(context);
        }
    }


}
