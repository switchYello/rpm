package com.fys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * hcy 2020/2/18
 */
public class Config {

    private static Logger log = LoggerFactory.getLogger(Config.class);
    public static String serverHost;
    public static int serverPort;
    public static int serverWorkPort;
    public static int localPort;


    public static void init() throws IOException {
        final InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream("config.properties");
        try (InputStream in = input) {
            Properties prop = new Properties();
            prop.load(in);
            serverHost = toString(prop.getProperty("serverHost"), "serverHost");
            serverPort = toInt(prop.getProperty("serverPort"), "serverPort");
            serverWorkPort = toInt(prop.getProperty("serverWorkPort"), "serverWorkPort");
            localPort = toInt(prop.getProperty("localPort"), "localPort");
        }
        log.info("读取配置文件serverHost:{},serverPort:{},serverWorkPort:{},localPort{}", serverHost, serverPort, serverWorkPort, localPort);
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
        return str;
    }

}
