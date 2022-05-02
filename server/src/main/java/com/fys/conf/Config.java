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
        InputStream input = getResource(configPath);
        if (input == null) {
            throw new IllegalArgumentException("配置文件不存在:" + configPath);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(input, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public List<ClientInfo> getClientInfos() {
        return clientInfos;
    }

    private static InputStream getResource(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }
        try {
            return new FileInputStream(resourcePath);
        } catch (FileNotFoundException ignored) {
        }
        InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
        if (input != null) {
            return input;
        }
        return null;
    }

}
