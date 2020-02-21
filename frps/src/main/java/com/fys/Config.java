package com.fys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * hcy 2020/2/18
 */
public class Config {

    private static Logger log = LoggerFactory.getLogger(Config.class);
    public static int bindPort;
    public static String bindHost = "0.0.0.0";
    public static int timeOut = 5;


    public static void init(String configPath) throws IOException {
        if (configPath == null) {
            configPath = "config.properties";
        }
        InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream(configPath);
        if (input == null) {
            input = new FileInputStream(configPath);
        }
        try (InputStream in = input) {
            Properties prop = new Properties();
            prop.load(in);
            bindHost = Optional.ofNullable(prop.getProperty("bindHost")).orElse(bindHost);
            bindPort = toInt(prop.getProperty("bindPort"), "bindPort");
        }
        log.info("读取配置文件bindPort:{}", bindPort);
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
