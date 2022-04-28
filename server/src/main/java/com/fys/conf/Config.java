package com.fys.conf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author hcy
 * @since 2022/4/28 17:56
 */
public class Config {

    private ServerInfo serverInfo;
    private List<ClientInfo> clientInfos;


    public static Config read(String configPath) {
        ObjectMapper mapper = new ObjectMapper();
        InputStream input = getResource(configPath);
        try {
            Config config = mapper.readValue(input, Config.class);
            config.checkConfig();
            return config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public List<ClientInfo> getClientInfos() {
        return clientInfos;
    }

    public void setClientInfos(List<ClientInfo> clientInfos) {
        this.clientInfos = clientInfos;
    }

    private void checkConfig() {


    }

    private static InputStream getResource(String resourceName) {
        if (resourceName == null) {
            return null;
        }
        InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName);
        if (input != null) {
            return input;
        }
        try {
            return new FileInputStream(resourceName);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

}
