package com.fys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * hcy 2020/2/18
 */
public class Config {

    private static Logger log = LoggerFactory.getLogger(Config.class);
    //服务器的地址和端口
    public static String serverHost;
    public static int serverPort;
    //自动验证token
    public static String auto_token;
    //服务器端口->本地地址 的映射
    public static Map<Integer, InetSocketAddress> works = new HashMap<>();


    public static void init(String configPath) throws IOException {
        if (configPath == null) {
            configPath = "config.properties";
        }
        InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream(configPath);
        if (input == null) {
            input = new FileInputStream(configPath);
        }
        log.info("读取配置文件:{}", configPath);
        try (InputStream in = input) {
            Properties prop = new Properties();
            prop.load(in);
            auto_token = toString(prop.getProperty("auto_token"), "auto_token");
            log.info("aut_token:{}", auto_token);

            InetSocketAddress inet = toInetSocket(prop.getProperty("serverBase"), "serverBase");
            serverHost = inet.getHostName();
            serverPort = inet.getPort();
            log.info("服务器{}:{}", serverHost, serverPort);

            String serverWorks = toString(prop.getProperty("serverWork"), "serverWork");
            for (String s : serverWorks.split(",")) {
                log.info("端口映射:{}", s);
                String[] split = s.split("->");
                Integer serverWorkPort = Integer.valueOf(split[0].trim());
                InetSocketAddress localClient = toInetSocket(split[1], "serverWork");
                if (serverWorkPort == serverPort) {
                    throw new IllegalArgumentException("映射:" + s + "不正确,服务器端口已被管理使用，请切换其他端口");
                }
                InetSocketAddress put = works.put(serverWorkPort, localClient);
                if (put != null) {
                    throw new IllegalArgumentException("映射:" + s + "不正确,服务器端口重复");
                }
            }
        }
    }

    private static int toInt(String str, String name) {
        if (str == null) {
            throw new RuntimeException(name + "不能为空");
        }
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
            throw new RuntimeException("参数" + name + "必须是整数类型的，这里不能转换成整数");
        }
    }

    private static String toString(String str, String name) {
        if (str == null) {
            throw new RuntimeException(name + "不能为空");
        }
        return str.trim();
    }

    private static InetSocketAddress toInetSocket(String str, String msg) {
        try {
            String[] split = str.trim().split(":");
            String host = split[0].trim();
            int port = Integer.valueOf(split[1].trim());
            return InetSocketAddress.createUnresolved(host, port);
        } catch (Exception e) {
            log.error("读取配置" + msg + "错误", e);
            throw e;
        }
    }

}
