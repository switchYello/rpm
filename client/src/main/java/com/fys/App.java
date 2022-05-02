package com.fys;

import com.fys.connection.ManagerConnection;
import io.netty.channel.ChannelFuture;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hcy
 * @since 2022/4/23 23:06
 */
public class App {

    private static Logger log = LoggerFactory.getLogger(App.class);

    private static AtomicInteger count = new AtomicInteger(1);
    private static ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
    private static Options OPTIONS = new Options();

    public static void main(String[] args) throws ParseException {
        Config config = parseConfig(args);
        ex.scheduleWithFixedDelay(() -> {
            log.info("启动客户端:{}", count.getAndIncrement());
            new App().start(config);
        }, 0, 10, TimeUnit.SECONDS);
    }

    private static Config parseConfig(String[] args) throws ParseException {
        OPTIONS.addOption(Option.builder("name").required().hasArg(true).desc("the name of client").build());
        OPTIONS.addOption(Option.builder("s").required().hasArg(true).desc("the host:port of server").build());
        OPTIONS.addOption(Option.builder("t").required().hasArg(true).desc("the token of server").build());
        CommandLine parse = new DefaultParser().parse(OPTIONS, args);
        String clientName = parse.getOptionValue("name");
        String serves = parse.getOptionValue("s");
        String token = parse.getOptionValue("t");
        String[] ipAndPort = serves.split(":");

        Config config = new Config();
        config.setClientName(clientName);
        config.setServerIp(ipAndPort[0]);
        config.setServerPort(Integer.parseInt(ipAndPort[1]));
        config.setToken(token);
        return config;
    }

    private void start(Config config) {
        ManagerConnection connection = new ManagerConnection(config.getClientName(), config.getToken(), config.getServerIp(), config.getServerPort());
        ChannelFuture future = connection.start();
        future.channel().closeFuture().awaitUninterruptibly();
    }

}
