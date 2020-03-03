package com.fys;

import com.fys.conf.ServerInfo;
import com.fys.conf.ServerWorker;
import com.fys.dao.ConfigDao;
import com.fys.gui.MainView;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * hcy 2020/3/2
 */
public class GuiStart {

    private static Logger log = LoggerFactory.getLogger(GuiStart.class);
    public static EventLoopGroup work = new NioEventLoopGroup(1);
    private ConfigDao configDao = new ConfigDao();
    private AppClient appClient;
    private ScheduledFuture<?> scheduledFuture;

    public static void main(String[] args) {
        GuiStart gs = new GuiStart();
        invokeLater(() -> {
            MainView jFrame = new MainView(gs);
            jFrame.setTitle("RemotePortMapping");
        });
    }

    //开始连接
    public void startConnection() {
        Config config = new Config();
        config.setServerInfo(configDao.getServerInfo());
        config.setServerWorkers(configDao.getServerWorks());
        ServerInfo serverInfo = config.getServerInfo();
        List<ServerWorker> serverWorks = config.getServerWorkers();
        if (serverInfo.getServerIp() == null) {
            MainView.tip("缺少服务器地址，无法连接");
            return;
        }

        log.info("开始连接服务器：{},客户端:{}", serverInfo, serverWorks);
        appClient = new AppClient(config);

        scheduledFuture = work.scheduleWithFixedDelay(() -> {
            if (appClient.isActive()) {
                return;
            }
            appClient.start().addListener((GenericFutureListener<Future<Object>>) future -> {
                if (future.isSuccess()) {
                    MainView.tip("重连成功");
                } else {
                    MainView.tip("重连失败，准备重试:" + future.cause().getMessage());
                }
            });
        }, 0, 10, TimeUnit.SECONDS);
    }

    //断开连接
    public void stopConnection() {
        if (appClient == null) {
            MainView.tip("尚未启动，无需断开");
        }
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        appClient.stop();
        MainView.tip("断开成功");
    }

}
