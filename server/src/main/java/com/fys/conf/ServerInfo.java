package com.fys.conf;

import java.util.List;

/**
 * hcy 2020/3/26
 */
public class ServerInfo {

    private String clientName;
    private List<ServerWorker> serverWorkers;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public List<ServerWorker> getServerWorkers() {
        return serverWorkers;
    }

    public void setServerWorkers(List<ServerWorker> serverWorkers) {
        this.serverWorkers = serverWorkers;
    }
}
