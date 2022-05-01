package com.fys;

import com.fys.connection.ManagerConnection;
import io.netty.channel.ChannelFuture;
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

    public static void main(String[] args) {
        AtomicInteger count = new AtomicInteger(1);
        ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
        ex.scheduleWithFixedDelay(() -> {
            log.info("启动客户端:{}", count.getAndIncrement());
            new App().start();
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void start() {
        ManagerConnection connection = new ManagerConnection("hcy_home_pc", "123456", "0.0.0.0", 9050);
        ChannelFuture future = connection.start();
        future.channel().closeFuture().awaitUninterruptibly();
    }

}
