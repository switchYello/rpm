package com.fys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    //等待客户端数据连接的超时时间
    public static int timeOut = 5;
    public static String auto_token;


    public static void init(String configPath) throws IOException {
        log.info("准备读取配置文件");

        InputStream input = getResource(configPath);
        if (input == null) {
            configPath = "config.properties";
            input = getResource(configPath);
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
        if (!"0.0.0.0".equals(bindHost)) {
            log.warn("bindHost 最好绑定在0.0.0.0上,否则linux上可能绑定不成功");
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
        return str;
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
