package com.fys;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * hcy 2020/2/18
 */
public class Config {

    private String serverIp;
    private int serverPort;
    private String clientName;
    private String token;

    public static Config INSTANCE = new Config();

    private Config() {
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream("config.properties");
            if (in == null) {
                if (Files.notExists(Paths.get("config.properties"))) {
                    throw new RuntimeException("找不到配置文件: config.properties");
                }
                in = new FileInputStream("config.properties");
            }

            Properties prop = new Properties();
            prop.load(in);
            serverIp = prop.getProperty("serverIp");
            clientName = prop.getProperty("clientName");
            token = prop.getProperty("token");
            serverPort = Integer.parseInt(prop.getProperty("serverPort"));
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getClientName() {
        return clientName;
    }

    public String getToken() {
        return token;
    }
}
