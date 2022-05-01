package com.fys.conf;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.TimeUnit;

/**
 * @author hcy
 * @since 2022/5/1 20:58
 */
public class EventLoops {

    public static EventLoopGroup BOSS = new NioEventLoopGroup(1);
    public static EventLoopGroup WORKER = new NioEventLoopGroup(1);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BOSS.shutdownGracefully();
            WORKER.shutdownGracefully();
        }));
    }

    public static <T> Promise<T> newPromise() {
        return new DefaultPromise<>(WORKER.next());
    }

    public static void schedule(Runnable runnable, long delay, TimeUnit unit) {
        WORKER.schedule(runnable, delay, unit);
    }

}
