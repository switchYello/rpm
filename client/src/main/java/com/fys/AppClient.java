package com.fys;

import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.listener.ErrorLogListener;
import com.fys.cmd.message.clientToServer.WantManagerCmd;
import com.fys.conf.ServerInfo;
import com.fys.conf.ServerWorker;
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

    private Config config = Config.INSTANCE;
    //管理连接
    private volatile Channel managerChannel;

    public static void main(String[] args) {
        AppClient appClient = new AppClient();
        work.scheduleWithFixedDelay(() -> {
            if (appClient.isActive()) {
                return;
            }
            log.info("监测到连接断开，准备重连");
            appClient.start();
        }, 0, 10, TimeUnit.SECONDS);
    }


    private void start() {
        ServerInfo serverInfo = config.getServerInfo();
        createManagerConnection()
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("连接成功{}:{}等待服务端验证", serverInfo.getServerIp(), serverInfo.getServerPort());
                        //开启配置文件中的映射
                        for (ServerWorker sw : config.getServerWorkers()) {
                            future.channel().writeAndFlush(new WantManagerCmd(sw.getServerPort(), sw.getLocalHost(), sw.getLocalPort(), serverInfo.getAutoToken()))
                                    .addListener(ErrorLogListener.INSTANCE);
                        }
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
        ServerInfo serverInfo = config.getServerInfo();
        Bootstrap b = new Bootstrap();
        return b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(serverInfo.getServerIp(), serverInfo.getServerPort())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                .option(ChannelOption.TCP_NODELAY, true)
                //将配置类存入channel
                .attr(AttributeKey.newInstance("conf"), config)
                .handler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                                 ch.pipeline().addLast(new CmdEncoder());

                                 ch.pipeline().addLast(new CmdDecoder());
                                 ch.pipeline().addLast(new PingHandler());
                                 ch.pipeline().addLast(new ServerStartSuccessHandler());
                                 ch.pipeline().addLast(new ServerStartFailHandler());
                                 ch.pipeline().addLast(new DataConnectionHandler());
                                 ch.pipeline().addLast(ExceptionHandler.INSTANCE);
                             }
                         }
                )
                .connect();
    }

}
