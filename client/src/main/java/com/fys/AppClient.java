package com.fys;

import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.ExceptionHandler;
import com.fys.cmd.handler.PingPongHandler;
import com.fys.cmd.handler.Rc4Md5Handler;
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
    public static AttributeKey<Config> key = AttributeKey.newInstance("config");

    private Config config = Config.INSTANCE;

    public static void main(String[] args) {
        AppClient appClient = new AppClient();
        appClient.start();
    }

    private void start() {
        createManagerConnection()
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("连接成功{}:{}等待服务端验证", config.getServerIp(), config.getServerPort());
                        //断开连接10s后会重试
                        future.channel().closeFuture().addListener(f -> {
                            log.info("检测到连接断开了10后重连", f.cause());
                            work.schedule(this::start, 10, TimeUnit.SECONDS);
                        });
                    } else {
                        log.error("连接失败,5秒后重试:{}", future.cause().toString());
                        work.schedule(this::start, 5, TimeUnit.SECONDS);
                    }
                });
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
                                 ch.pipeline().addLast(new Rc4Md5Handler(config.getToken()));
                                 ch.pipeline().addLast(new CmdEncoder());

                                 ch.pipeline().addLast(new CmdDecoder());
                                 ch.pipeline().addLast(new PingPongHandler());
                                 ch.pipeline().addLast(new LoginHandler());
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
