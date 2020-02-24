package com.fys;

import com.fys.cmd.message.clientToServer.WantManagerCmd;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.Promise;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * hcy 2020/2/24
 */
public class ServerManagerTest {

    private static int getAvaliablePort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        int localPort = serverSocket.getLocalPort();
        serverSocket.close();
        return localPort;
    }

    private static Channel getNewChannel() {
        return new EmbeddedChannel();
    }

    @Test
    public void startNewServer() throws IOException, InterruptedException {
        int avaliablePort = getAvaliablePort();
        WantManagerCmd msg = new WantManagerCmd(avaliablePort, "127.0.0.1", 80,"111");
        Channel channel = getNewChannel();
        //开启新server
        Promise<Server> promise = ServerManager.startNewServer(msg, channel);
        promise.await();
        if (!promise.isSuccess()) {
            Assert.fail(promise.cause().toString());
        }
        Server server = promise.getNow();
        Assert.assertEquals(Server.Status.start, server.getStatus());
        Assert.assertEquals(avaliablePort, server.getServerPort());
        Assert.assertEquals("127.0.0.1", server.getLocalHost());
        Assert.assertEquals(80, server.getLocalPort());

        //关闭Server
        channel.close().sync();
        Assert.assertTrue(!channel.isActive());
        Assert.assertEquals(Server.Status.pause, server.getStatus());
        Assert.assertEquals(server, ServerManager.getPauseServer().get(avaliablePort));

        //重启Server
        WantManagerCmd msg2 = new WantManagerCmd(avaliablePort, "127.0.0.2", 90,"123");
        Channel channel2 = getNewChannel();

        Promise<Server> promise2 = ServerManager.startNewServer(msg2, channel2);
        promise2.await();
        if (!promise2.isSuccess()) {
            Assert.fail(promise2.cause().toString());
        }
        Server server2 = promise.getNow();
        Assert.assertEquals(server, server2);
        Assert.assertEquals(Server.Status.start, server.getStatus());
        Assert.assertEquals(avaliablePort, server.getServerPort());
        Assert.assertEquals("127.0.0.2", server.getLocalHost());
        Assert.assertEquals(90, server.getLocalPort());
        Assert.assertTrue(ServerManager.getPauseServer().isEmpty());
    }

}