package com.fys;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fys.conf.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * hcy 2020/2/18
 */
public class Config {

    private static Logger log = LoggerFactory.getLogger(Config.class);
    public static String bindHost = "0.0.0.0";
    public static int bindPort;
    public static String token;
    //等待客户端数据连接的超时时间
    public static int timeOut = 5;

    private static List<ServerInfo> serverInfos = new ArrayList<>();

    public static void init(String configPath) throws IOException {
        log.info("准备读取配置文件");
        ObjectMapper mapper = new ObjectMapper();
        InputStream input = getResource(configPath);
        JsonNode config = mapper.readTree(input);
        bindPort = config.at("/server/bindHost").intValue();
        token = config.at("/server/token").textValue();

        for (JsonNode node : config.get("clients")) {
            ServerInfo serverInfo = mapper.treeToValue(node, ServerInfo.class);
            serverInfos.add(serverInfo);
        }
    }

    public static ServerInfo getServerInfo(String clientName) {
        for (ServerInfo serverInfo : serverInfos) {
            if (clientName.equals(serverInfo.getClientName())) {
                return serverInfo;
            }
        }
        return null;
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
