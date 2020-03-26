package com.fys;

import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.message.clientToServer.LoginCmd;
import com.fys.handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 客户端启动类
 */
public class AppClient {

    private static Logger log = LoggerFactory.getLogger(AppClient.class);
    public static EventLoopGroup work = new NioEventLoopGroup(1);
    private static AttributeKey<Config> key = AttributeKey.newInstance("config");

    private Config config = Config.INSTANCE;
    //管理连接
    private volatile Channel managerChannel;

    private static class Watch {

        long lastStart = 0;
        long lastStop = 0;
        int stopCount = 0;
        boolean start = false;

        //保存最后启动时间
        public void start() {
            lastStart = System.currentTimeMillis();
            start = true;
        }

        //保存最后停止时间和总停止次数
        //如果当前是启动状态，则输出持续时间
        public void stop() {
            stopCount++;
            lastStop = System.currentTimeMillis();
            if (start) {
                start = false;
                log.info("上次坚持:{}分钟,共停止{}次", TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastStart), stopCount);
            } else {
                log.info("上次连接没成功,共停止{}次", stopCount);
            }
        }

    }

    public static void main(String[] args) {
        AppClient appClient = new AppClient();
        Watch watch = new Watch();
        work.scheduleWithFixedDelay(() -> {
            if (appClient.isActive()) {
                return;
            }
            try {
                log.info("监测到连接断开，准备重连");
                watch.stop();
                appClient.start().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        watch.start();
                    }
                });
            } catch (Exception e) {
                log.error("重连时报错", e);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }


    private ChannelFuture start() {
        return createManagerConnection()
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("连接成功{}:{}等待服务端验证", config.getServerIp(), config.getServerPort());
                        //登录
                        future.channel().writeAndFlush(new LoginCmd(config.getClientName(), config.getToken()));
                        managerChannel = future.channel();
                    } else {
                        log.error("连接失败:{}", future.cause().toString());
                    }
                });
    }


    private boolean isActive() {
        return managerChannel != null && managerChannel.isActive();
    }

    private ChannelFuture createManagerConnection() {
        Bootstrap b = new Bootstrap();
        return b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(config.getServerIp(), config.getServerPort())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                .option(ChannelOption.TCP_NODELAY, true)
                //将配置类存入channel
                .attr(key, config)
                .handler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                                 ch.pipeline().addLast(new CmdEncoder());

                                 ch.pipeline().addLast(new CmdDecoder());
                                 ch.pipeline().addLast(new PingHandler());
                                 ch.pipeline().addLast(new DataConnectionHandler());
                                 ch.pipeline().addLast(new ServerStartSuccessHandler());
                                 ch.pipeline().addLast(new ServerStartFailHandler());
                                 ch.pipeline().addLast(new LoginFailHandler());
                                 ch.pipeline().addLast(ExceptionHandler.INSTANCE);
                             }
                         }
                )
                .connect();
    }

}
