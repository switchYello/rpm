package com.fys;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fys.conf.ServerInfo;
import com.fys.conf.ServerWorker;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * hcy 2020/2/18
 */
public class Config {

    private ServerInfo serverInfo;
    private List<ServerWorker> serverWorkers = new ArrayList<>();

    public static Config INSTANCE = new Config();

    private Config() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream in = ClassLoader.getSystemResourceAsStream("conf.json");
            if (in == null) {
                in = new FileInputStream("conf.json");
            }
            //读取json配置
            JsonNode jsonNode = objectMapper.readTree(in);
            //服务器信息
            serverInfo = objectMapper.treeToValue(jsonNode.get("server"), ServerInfo.class);

            //work信息
            JsonNode server_work = jsonNode.get("server_work");
            for (JsonNode node : server_work) {
                ServerWorker serverWorker = objectMapper.treeToValue(node, ServerWorker.class);
                serverWorkers.add(serverWorker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public List<ServerWorker> getServerWorkers() {
        return serverWorkers;
    }
    
    @Override
    public String toString() {
        return "Config{" +
                "serverInfo=" + serverInfo +
                ", serverWorkers=" + serverWorkers +
                '}';
    }
}
