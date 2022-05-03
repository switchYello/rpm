package com.fys;

import com.fys.cmd.util.EventLoops;
import com.fys.conf.ClientInfo;
import com.fys.connection.DataConnection;
import com.fys.connection.ManagerConnection;
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
     */
    public void registerManagerConnection(ManagerConnection client) {
        ManagerConnection old = clients.put(client.getClientName(), client);
        if (old != null && old != client) {
            old.close();
        }
    }

    /**
     * 取消注册客户端
     */
    public void unRegisterManagerConnection(ManagerConnection client) {
        clients.remove(client.getClientName(), client);
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

    public void registerNeedDataPromise(long sessionId, Promise<DataConnection> promise) {
        needDataConnection.put(sessionId, promise);
        EventLoops.schedule(() -> {
            Promise<DataConnection> p = needDataConnection.remove(sessionId);
            if (p != null) {
                p.tryFailure(new TimeoutException("timeout1"));
            }
        }, 20, TimeUnit.SECONDS);
    }


    /**
     * 获取客户端目标连接
     *
     * @param clientInfo 客户端信息
     */
    public Promise<DataConnection> getTargetChannel(ClientInfo clientInfo) {
        ManagerConnection manager = clients.get(clientInfo.getClientName());
        if (manager == null) {
            Promise<DataConnection> promise = EventLoops.newPromise();
            promise.setFailure(new RuntimeException("未找到客户端:" + clientInfo.getClientName()));
            return promise;
        }
        return manager.getTargetChannel(clientInfo);
    }

}
