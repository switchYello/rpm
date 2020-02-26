package com.fys;

import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.listener.ErrorLogListener;
import com.fys.cmd.message.clientToServer.WantManagerCmd;
import com.fys.handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 客户端启动类
 */
public class AppClient {

    private static Logger log = LoggerFactory.getLogger(AppClient.class);
    public static EventLoopGroup work = new NioEventLoopGroup(1);
    private long startTime = System.currentTimeMillis();

    /*
     * -c conf.properties
     * */
    public static void main(String[] args) throws IOException {
        log.info(Arrays.toString(args));
        String confPath = null;
        Iterator<String> iterator = Arrays.asList(args).iterator();
        while (iterator.hasNext()) {
            if ("-c".equals(iterator.next().trim())) {
                confPath = iterator.hasNext() ? iterator.next().trim() : null;
                break;
            }
        }

        Config.init(confPath);
        new AppClient().start();
    }

    private static void schedule(Runnable run, int time, TimeUnit timeUnit) {
        work.schedule(run, time, timeUnit);
    }

    public void start() {
        log.info("上次坚持了:{}分钟", (System.currentTimeMillis() - startTime) / 1000 / 60.0);
        startTime = System.currentTimeMillis();

        ChannelFuture managerConnectionFuture = createManagerConnection();
        //并为future添加事件,无论连接成功失败都在10秒后检查连接
        managerConnectionFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("连接成功{}:{}等待服务端验证", Config.serverHost, Config.serverPort);

                //开启配置文件中的映射
                for (Map.Entry<Integer, InetSocketAddress> entry : Config.works.entrySet()) {
                    Integer serverPort = entry.getKey();
                    InetSocketAddress localClient = entry.getValue();
                    future.channel().writeAndFlush(new WantManagerCmd(serverPort, localClient.getHostString(), localClient.getPort(), Config.auto_token))
                            .addListener(ErrorLogListener.INSTANCE);
                }
            } else {
                log.error("连接失败:{}", future.cause().toString());
            }
            schedule(new ScheduleCheck(future.channel()), 10, TimeUnit.SECONDS);
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
                log.debug("定时检测，管理连接正常");
                schedule(this, 15, TimeUnit.SECONDS);
                return;
            }
            log.info("定时检测，管理连接断开了，准备重新连..");
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
                                 ch.pipeline().addLast(DataConnectionHandler.INSTANCE);
                                 ch.pipeline().addLast(ExceptionHandler.INSTANCE);
                             }
                         }
                )
                .connect();
    }

}
