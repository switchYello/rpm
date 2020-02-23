package com.fys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * hcy 2020/2/18
 */
public class Config {

    private static Logger log = LoggerFactory.getLogger(Config.class);
    public static int bindPort;
    public static String bindHost = "0.0.0.0";
    public static int timeOut = 5;
    public static String auto_token;


    public static void init(String configPath) throws IOException {
        log.info("准备读取配置文件");
        if (configPath == null) {
            configPath = "config.properties";
            log.info("配置文件:{}", configPath);
        }
        InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream(configPath);
        if (input == null) {
            input = new FileInputStream(configPath);
        }
        try (InputStream in = input) {
            Properties prop = new Properties();
            prop.load(in);
            auto_token = toString(prop.getProperty("auto_token"), "auto_token");
            String bindAt = toString(prop.getProperty("bindAt"), "bindAt");
            String[] split = bindAt.trim().split(":");
            bindHost = split[0].trim();
            bindPort = Integer.valueOf(split[1].trim());
        }
        log.info("bindHost:{}", bindHost);
        log.info("bindPort:{}", bindPort);
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