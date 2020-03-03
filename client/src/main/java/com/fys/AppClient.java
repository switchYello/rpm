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
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 客户端启动类
 */
public class AppClient {

    private static Logger log = LoggerFactory.getLogger(AppClient.class);
    private static EventLoopGroup work = GuiStart.work;

    //服务器信息
    private ServerInfo serverInfo;
    //需要创建的serverWorks
    private List<ServerWorker> works;
    //管理连接
    private volatile Channel managerChannel;

    public AppClient(Config config) {
        serverInfo = config.getServerInfo();
        works = config.getServerWorkers();
    }


    public Future<?> start() {
        if (isActive()) {
            throw new RuntimeException("已经处于启动状态，无法重复启动");
        }
        ChannelFuture managerConnectionFuture = createManagerConnection(serverInfo);
        managerConnectionFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("连接成功{}:{}等待服务端验证", serverInfo.getServerIp(), serverInfo.getServerPort());
                //开启配置文件中的映射
                for (ServerWorker sw : works) {
                    future.channel().writeAndFlush(new WantManagerCmd(sw.getServerPort(), sw.getLocalHost(), sw.getLocalPort(), serverInfo.getAutoToken()))
                            .addListener(ErrorLogListener.INSTANCE);
                }
                managerChannel = future.channel();
            } else {
                log.error("连接失败:{}", future.cause().toString());
            }
        });
        return managerConnectionFuture;
    }

    public void stop() {
        if (managerChannel != null) {
            managerChannel.close();
        }
    }

    public boolean isActive() {
        return managerChannel != null && managerChannel.isActive();
    }

    private static ChannelFuture createManagerConnection(ServerInfo serverInfo) {
        Bootstrap b = new Bootstrap();
        return b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(serverInfo.getServerIp(), serverInfo.getServerPort())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                                 ch.pipeline().addLast(new CmdEncoder());

                                 ch.pipeline().addLast(new CmdDecoder());
                                 ch.pipeline().addLast(new PingHandler());
                                 ch.pipeline().addLast(new ServerStartSuccessHandler());
                                 ch.pipeline().addLast(new ServerStartFailHandler());
                                 ch.pipeline().addLast(new DataConnectionHandler(serverInfo));
                                 ch.pipeline().addLast(ExceptionHandler.INSTANCE);
                             }
                         }
                )
                .connect();
    }

}
