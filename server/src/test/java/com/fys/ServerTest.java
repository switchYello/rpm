package com.fys;

import com.fys.cmd.message.clientToServer.WantManagerCmd;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.DefaultPromise;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * hcy 2020/2/24
 */
public class ServerTest {
    private static int getAvaliablePort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        int localPort = serverSocket.getLocalPort();
        serverSocket.close();
        return localPort;
    }

    private static Channel getNewChannel() {
        return new EmbeddedChannel();
    }

    //@Test
    public void start() throws Throwable {
        int avaliablePort = getAvaliablePort();
        WantManagerCmd msg = new WantManagerCmd(avaliablePort, "127.0.0.1", 80, "111");
        Channel channel = getNewChannel();
        EventLoop eventExecutors = channel.eventLoop();
        DefaultPromise<Server> promise = new DefaultPromise<>(eventExecutors);

        Server server = new Server(msg, channel);
        server.start(promise);

        promise.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("第一次创建成功");
            } else {
                System.out.println("第一次创建失败");
                try {
                    throw future.cause();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
        Thread.sleep(1000);

        promise = new DefaultPromise<>(eventExecutors);
        server = new Server(msg, channel);
        server.start(promise);
        promise.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("第二次创建成功");
            } else {
                System.out.println("第二次创建失败");
                try {
                    throw future.cause();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });

        Thread.sleep(2000);
    }
}