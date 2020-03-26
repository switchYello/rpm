package com.fys.conf;

/**
 * hcy 2020/3/2
 */
public class ServerWorker {

    private Integer serverPort;
    private String localHost;
    private Integer localPort;

    public ServerWorker() {
    }

    public ServerWorker(Integer serverPort, String localHost, Integer localPort) {
        this.serverPort = serverPort;
        this.localHost = localHost;
        this.localPort = localPort;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }


    @Override
    public String toString() {
        if (serverPort == null) {
            return "尚未配置的服务映射";
        }
        return serverPort + " ---> " + localHost + ":" + localPort;
    }
}
