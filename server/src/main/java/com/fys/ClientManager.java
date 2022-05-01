package com.fys;

import com.fys.cmd.listener.Listeners;
import com.fys.cmd.message.DataConnectionCmd;
import com.fys.conf.ClientInfo;
import com.fys.conf.EventLoops;
import com.fys.connection.DataConnection;
import com.fys.connection.ManagerConnection;
import io.netty.channel.ChannelFutureListener;
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

    private static final Logger log = LoggerFactory.getLogger(ClientManager.class);

    //存储客户端 managerConnection
    private Map<String, ManagerConnection> clients = new ConcurrentHashMap<>();
    //存储需要客户端数据连接的promise
    private Map<Long, Promise<DataConnection>> needDataConnection = new ConcurrentHashMap<>();

    /**
     * 注册控制连接
     *
     * @param client 客户端
     */
    public void registerManagerConnection(ManagerConnection client) {
        client.nativeChannel().channel().closeFuture().addListener((ChannelFutureListener) future -> clients.remove(client.getClientName()));
        ManagerConnection old = clients.put(client.getClientName(), client);
        if (old != null && old != client) {
            old.close();
        }
    }

    /**
     * 注册数据连接
     *
     * @param token          请求token
     * @param dataConnection 数据连接
     */
    public void registerDataConnection(long token, DataConnection dataConnection) {
        Promise<DataConnection> promise = needDataConnection.remove(token);
        if (promise == null) {
            log.warn("not find promise for DataConnection");
            dataConnection.close();
            return;
        }
        boolean success = promise.trySuccess(dataConnection);
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
     * @param clientInfo 客户端信息
     */
    public Promise<DataConnection> getTargetChannel(ClientInfo clientInfo) {
        Promise<DataConnection> promise = EventLoops.newPromise();
        ManagerConnection manager = getClient(clientInfo.getClientName());
        if (manager == null) {
            promise.setFailure(new RuntimeException("未找到客户端:" + clientInfo.getClientName()));
            return promise;
        }
        DataConnectionCmd msg = new DataConnectionCmd(clientInfo.getLocalHost(), clientInfo.getLocalPort());
        needDataConnection.put(msg.getSessionId(), promise);
        manager.writeMessage(msg).addListeners(Listeners.ERROR_LOG, ChannelFutureListener.CLOSE_ON_FAILURE);
        EventLoops.schedule(() -> {
            Promise<DataConnection> p = needDataConnection.remove(msg.getSessionId());
            if (p != null) {
                p.tryFailure(new TimeoutException("timeout1"));
            }
        }, 20, TimeUnit.SECONDS);
        return promise;
    }

    /**
     * 根据clientId查询客户端
     *
     * @param clientId 客户端名
     */
    private ManagerConnection getClient(String clientId) {
        return clients.get(clientId);
    }

}
