package com.fys;

import com.fys.cmd.clientToServer.Ping;
import com.fys.cmd.handler.CmdEncoder;
import com.fys.handler.ManagerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;
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
        ScheduledFuture<?> sf = scheduleConnection(4, TimeUnit.SECONDS);
        Channel managerConnection = createManagerConnection();

    }

    private static ScheduledFuture<?> scheduleConnection(int delay, TimeUnit unit) {
        return work.scheduleWithFixedDelay(new Runnable() {
            Channel managerConnection = createManagerConnection();

            @Override
            public void run() {
                if (managerConnection.isActive()) {
                    log.info("定时检测，管理连接正常");
                    managerConnection.writeAndFlush(new Ping());
                    return;
                }
                log.info("定时检测，管理连接断开了，重新连");
                managerConnection = createManagerConnection();
            }
        }, 1, delay, unit);
    }

    private static Channel createManagerConnection() {
        Bootstrap b = new Bootstrap();
        ChannelFuture connect = b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(Config.serverHost, Config.serverPort)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                .handler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                                 ch.pipeline().addLast(new CmdEncoder());
                                 ch.pipeline().addLast(new ManagerHandler());
                             }
                         }
                )
                .connect();
        return connect.channel();
    }

    private static void assertTrue(boolean c, String message) {
        if (!c) {
            throw new RuntimeException(message);
        }
    }

}
