package com.fys;

import com.fys.conf.ServerInfo;
import com.fys.conf.ServerWorker;

import java.util.List;

/**
 * hcy 2020/2/18
 */
public class Config {

    private ServerInfo serverInfo;
    private List<ServerWorker> serverWorkers;

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public List<ServerWorker> getServerWorkers() {
        return serverWorkers;
    }

    public void setServerWorkers(List<ServerWorker> serverWorkers) {
        this.serverWorkers = serverWorkers;
    }

    @Override
    public String toString() {
        return "Config{" +
                "serverInfo=" + serverInfo +
                ", serverWorkers=" + serverWorkers +
                '}';
    }
}
