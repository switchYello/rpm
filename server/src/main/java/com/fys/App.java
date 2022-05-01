package com.fys;

import com.fys.conf.ClientInfo;
import com.fys.conf.Config;
import com.fys.conf.ServerInfo;
import com.fys.server.DataServer;
import com.fys.server.ManagerServer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    /*
     * -c config.properties
     * */
    public static void main(String[] args) {
        ClientManager clientManager = new ClientManager();
        Config config = Config.read("config.json");

        //创建控制端口监听
        ServerInfo serverInfo = config.getServerInfo();
        new ManagerServer(clientManager, serverInfo).start();

        //创建用户端口监听
        List<ClientInfo> clientInfoList = config.getClientInfos();
        for (ClientInfo clientInfo : clientInfoList) {
            new DataServer(clientManager, clientInfo).start();
        }
    }

}
