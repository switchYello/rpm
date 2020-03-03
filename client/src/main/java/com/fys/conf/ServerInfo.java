package com.fys.conf;

/**
 * hcy 2020/3/2
 */
public class ServerInfo {

    private String serverIp;
    private Integer serverPort;
    private String autoToken;

    public ServerInfo() {
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getAutoToken() {
        return autoToken;
    }

    public void setAutoToken(String autoToken) {
        this.autoToken = autoToken;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "serverIp='" + serverIp + '\'' +
                ", serverPort=" + serverPort +
                ", autoToken='" + autoToken + '\'' +
                '}';
    }
}
