package com.fys;

import com.fys.cmd.message.DataConnectionCmd;
import com.fys.conf.ClientInfo;
import com.fys.connection.DataConnection;
import com.fys.connection.ManagerConnection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author hcy
 * @since 2022/4/28 14:43
 */
public class ClientManager {

    private static Logger log = LoggerFactory.getLogger(ClientManager.class);

    //存储客户端 managerConnection
    private Map<String, ManagerConnection> clients = new ConcurrentHashMap<>();
    //存储需要客户端数据连接的promise
    private Map<Long, Promise<Channel>> needDataConnection = new ConcurrentHashMap<>();

    /**
     * 注册控制连接
     *
     * @param clientId
     * @param client
     */
    public void registerManagerConnection(String clientId, ManagerConnection client) {
        ManagerConnection old = clients.put(clientId, client);
        if (old != null) {
            old.close();
        }
    }

    /**
     * 注册数据连接
     *
     * @param token
     * @param dataConnection
     */
    public void registerDataConnection(long token, DataConnection dataConnection) {
        Promise<Channel> promise = needDataConnection.remove(token);
        if (promise == null) {
            log.warn("not find promise for DataConnection");
            dataConnection.close();
            return;
        }
        boolean success = promise.trySuccess(dataConnection.channel());
        if (!success) {
            promise.cancel(false);
            dataConnection.close();
        } else {
            log.debug("数据连接批次成功");
        }
    }

    /**
     * 获取客户端目标连接
     *
     * @param clientInfo
     * @param executor
     * @return
     */
    public Promise<Channel> getTargetChannel(ClientInfo clientInfo, EventExecutor executor) {
        Promise<Channel> promise = executor.newPromise();
        ManagerConnection client = getClient(clientInfo.getClientName());
        if (client == null) {
            promise.setFailure(new RuntimeException("未找到客户端:" + clientInfo.getClientName()));
            return promise;
        }
        DataConnectionCmd msg = new DataConnectionCmd(0, clientInfo.getLocalHost(), clientInfo.getLocalPort(), System.nanoTime());
        needDataConnection.put(msg.getToken(), promise);
        client.writeMessage(msg).addListeners(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    log.debug("发送数据连接请求成功");
                } else {
                    log.debug("发送数据连接请求失败");
                }
                executor.schedule(() -> {
                    Promise<Channel> p = needDataConnection.remove(msg.getToken());
                    if (p != null) {
                        p.tryFailure(new TimeoutException("timeout1"));
                    }
                }, 10, TimeUnit.SECONDS);
            }
        }, ChannelFutureListener.CLOSE_ON_FAILURE);
        return promise;
    }


    /**
     * 根据clientId查询客户端
     *
     * @param clientId
     * @return
     */
    private ManagerConnection getClient(String clientId) {
        return clients.get(clientId);
    }

}
