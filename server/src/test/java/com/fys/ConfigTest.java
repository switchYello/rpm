package com.fys;

import com.fys.conf.ServerInfo;
import com.fys.conf.ServerWorker;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * hcy 2020/3/26
 */
public class ConfigTest {

    @Test
    public void testConf() throws IOException {
        Config.init("config.json");
        Assert.assertEquals("0.0.0.0", Config.bindHost);
        Assert.assertEquals(9050, Config.bindPort);
        ServerInfo hcy_home_pc = Config.getServerInfo("hcy_home_pc");
        System.out.println(hcy_home_pc.getClientName());
        for (ServerWorker serverWorker : hcy_home_pc.getServerWorkers()) {
            System.out.println("------");
            System.out.println(serverWorker.getServerPort());
            System.out.println(serverWorker.getLocalHost());
            System.out.println(serverWorker.getLocalPort());
        }

    }


}
