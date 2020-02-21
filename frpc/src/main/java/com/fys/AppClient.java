package com.fys;

import com.fys.cmd.handler.CmdEncoder;
import com.fys.handler.CmdDecoder;
import com.fys.handler.PingHandler;
import com.fys.handler.ServerStartFailHandler;
import com.fys.handler.ServerStartSuccessHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 客户端启动类
 */
public class AppClient {

    private static Logger log = LoggerFactory.getLogger(AppClient.class);
    public static EventLoopGroup work = new NioEventLoopGroup(1);

    /*
     * -c=conf.properties
     * */
    public static void main(String[] args) throws IOException {
        log.info(Arrays.toString(args));
        String confPath = null;
        for (String arg : args) {
            String[] split = arg.split("=");
            assertTrue(split.length == 2, "参数不正确，无法解析:" + arg);
            if ("-c".equals(split[0])) {
                confPath = split[1];
            }
        }

        Config.init(confPath);
        new AppClient().start();
    }

    private static void schedule(Runnable run, int time, TimeUnit timeUnit) {
        work.schedule(run, time, timeUnit);
    }

    public void start() {
        ChannelFuture managerConnectionFuture = createManagerConnection();
        //并为future添加事件,无论连接成功失败都在10秒后检查连接
        managerConnectionFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                schedule(new ScheduleCheck(future.channel()), 10, TimeUnit.SECONDS);
            }
        });
    }


    private class ScheduleCheck implements Runnable {

        Channel managerConnection;

        ScheduleCheck(Channel managerConnection) {
            this.managerConnection = managerConnection;
        }

        /*
         * 如果连接正常，则20秒后再检查
         * 如果连接断开了，则重新启动
         * */
        @Override
        public void run() {
            if (managerConnection.isActive()) {
                log.info("定时检测，管理连接正常");
                schedule(this, 20, TimeUnit.SECONDS);
                return;
            }
            log.info("定时检测，管理连接断开了，重新连");
            start();
        }
    }

    private static ChannelFuture createManagerConnection() {
        Bootstrap b = new Bootstrap();
        return b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(Config.serverHost, Config.serverPort)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                                 ch.pipeline().addLast(new CmdEncoder());

                                 ch.pipeline().addLast(new CmdDecoder());
                                 ch.pipeline().addLast(PingHandler.INSTANCE);
                                 ch.pipeline().addLast(ServerStartSuccessHandler.INSTANCE);
                                 ch.pipeline().addLast(ServerStartFailHandler.INSTANCE);
                             }
                         }
                )
                .connect();
    }

    private static void assertTrue(boolean c, String message) {
        if (!c) {
            throw new RuntimeException(message);
        }
    }

}
