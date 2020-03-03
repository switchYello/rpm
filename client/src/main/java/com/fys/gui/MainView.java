package com.fys.gui;

import com.fys.GuiStart;
import com.fys.conf.ServerInfo;
import com.fys.conf.ServerWorker;
import com.fys.dao.ConfigDao;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * hcy 2020/3/3
 */
public class MainView extends JFrame {

    private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(1024);
    private static EventLoopGroup work = GuiStart.work;
    private static final int width = 800;
    private static final int height = 400;
    private ConfigDao configDao = new ConfigDao();
    private Box leftBox = Box.createVerticalBox();
    private JPanel rightPanel = new JPanel(new GridLayout(2, 1));
    private JTextArea logLab = new JTextArea();
    private DefaultListModel<ServerWorker> items = new DefaultListModel<>();
    private JList<ServerWorker> jList = new JList<>(items);

    //全局server，work信息输入框
    private ServerGui serverGui;
    private WorkGui workGui;
    //当前选择的worker
    private ServerWorker current = new ServerWorker();

    private GuiStart gs;

    {
        setSize(width, height);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));
        add(leftBox);
        add(rightPanel);
    }

    public MainView(GuiStart gs) {
        this.gs = gs;
        //左边部分
        leftBox.add(new JScrollPane(jList));
        jList.addListSelectionListener(listListener);
        Box leftButtons = Box.createHorizontalBox();
        JButton add = new JButton("添加");
        add.addActionListener(onAdd);
        leftButtons.add(add);
        JButton delete = new JButton("删除");
        delete.addActionListener(onDelete);
        leftButtons.add(delete);
        leftBox.add(leftButtons);

        //右边部分
        Box up = Box.createVerticalBox();
        this.serverGui = new ServerGui();
        up.add(serverGui);

        this.workGui = new WorkGui();
        up.add(workGui);

        //右边按钮部分
        Box buttonGroup = Box.createHorizontalBox();
        JButton save = new JButton("保存");
        buttonGroup.add(save);
        save.addActionListener(onSave);
        JButton startConnection = new JButton("连接");
        buttonGroup.add(startConnection);
        startConnection.addActionListener(onConnection);
        up.add(buttonGroup);
        this.rightPanel.add(up);

        //右边log部分
        this.rightPanel.add(new JScrollPane(logLab));

        //赋值数据
        try {
            serverGui.setData(configDao.getServerInfo());
            reloadWorkers();
        } catch (Exception e) {
            tip(e.toString());
        }

        new Thread(() -> {
            while (true) {
                try {
                    String take = queue.take();
                    logLab.insert(take, 0);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();
    }

    public static void tip(String str) {
        invokeLater(() -> {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            queue.offer(format.format(new Date()) + ": " + str + "\n");
        });
    }

    //将当前选择选项，同步到右边输入框内
    private void currentToWork() {
        if (current == null) {
            workGui.setData(new ServerWorker());
        } else {
            workGui.setData(current);
        }
    }

    //加载worker数据
    private void reloadWorkers() {
        current = null;
        currentToWork();
        items.clear();
        for (ServerWorker serverWorker : configDao.getServerWorks()) {
            items.addElement(serverWorker);
        }
    }

    //添加按钮，添加空白worker
    private ActionListener onAdd = e -> invokeLater(() -> {
        current = new ServerWorker();
        currentToWork();
        items.addElement(current);
    });

    //删除按钮，删除选择的选项，如果是数据库存在的，则连同数据库内的一同删除
    private ActionListener onDelete = e -> invokeLater(() -> {
        java.util.List<ServerWorker> selects = jList.getSelectedValuesList();
        current = null;
        currentToWork();
        for (ServerWorker select : selects) {
            if (select.getId() != null) {
                configDao.deleteWork(select.getId());
            }
            items.removeElement(select);
            tip("删除成功");
        }
    });

    private ListSelectionListener listListener = e -> {
        if (e.getValueIsAdjusting()) {
            return;
        }
        java.util.List<ServerWorker> list = jList.getSelectedValuesList();
        if (list.size() == 1) {
            current = list.get(0);
            currentToWork();
        }
    };

    private ActionListener onSave = event -> {
        try {
            work.submit(() -> {
                ServerInfo serverinfo = serverGui.getConf();
                configDao.addServer(serverinfo);
            }).addListener((GenericFutureListener<Future<Object>>) future -> {
                if (!future.isSuccess()) {
                    tip("更新Server信息失败:" + future.cause().toString());
                }
            });

            work.submit(() -> {
                ServerWorker conf = workGui.getConf();
                if (current != null) {
                    conf.setId(current.getId());
                }
                if (conf.getId() == null) {
                    configDao.insertWork(conf);
                } else {
                    configDao.updateWork(conf);
                }
            }).addListener((GenericFutureListener<Future<Object>>) future -> {
                if (future.isSuccess()) {
                    invokeLater(this::reloadWorkers);
                } else {
                    tip("更新ServerWork信息失败:" + future.cause().toString());
                }
            });

        } catch (Exception e) {
            tip(e.getMessage());
        }
    };

    private ActionListener onConnection = ae -> {
        JButton source = (JButton) ae.getSource();
        invokeLater(() -> source.removeActionListener(this.onConnection));
        tip("正在连接");
        work.submit(() -> gs.startConnection()).addListener((GenericFutureListener<Future<Object>>) future -> {
            if (future.isSuccess()) {
                invokeLater(() -> {
                    source.setText("取消连接");
                    source.addActionListener(MainView.this.onUnConnection);
                });
            } else {
                invokeLater(() -> source.addActionListener(MainView.this.onConnection));
            }
        });
    };

    private ActionListener onUnConnection = ae -> {
        JButton source = (JButton) ae.getSource();
        invokeLater(() -> source.removeActionListener(this.onUnConnection));
        tip("正在断开连接");
        work.submit(() -> gs.stopConnection()).addListener((GenericFutureListener<Future<Object>>) future -> {
            if (future.isSuccess()) {
                invokeLater(() -> {
                    source.addActionListener(this.onConnection);
                    source.setText("连接");
                });
            } else {
                tip("断开失败");
                invokeLater(() -> source.addActionListener(this.onUnConnection));
            }
        });
    };


    private static class ServerGui extends JPanel {
        private JTextField serverIp = new JTextField(10);
        private JTextField serverPort = new JTextField(5);
        private JTextField autoToken = new JTextField(10);


        ServerGui() {
            setLayout(new GridLayout(1, 1));
            serverIp.setMaximumSize(serverIp.getPreferredSize());
            serverPort.setMaximumSize(serverPort.getPreferredSize());
            autoToken.setMaximumSize(autoToken.getPreferredSize());

            Box box = Box.createVerticalBox();

            {
                Box b = Box.createHorizontalBox();

                b.add(new JLabel("Server :"));
                b.add(serverIp);
                b.add(Box.createHorizontalGlue());
                box.add(b);
            }
            {
                Box b = Box.createHorizontalBox();
                b.add(new JLabel("Port   :"));
                b.add(serverPort);
                b.add(Box.createHorizontalGlue());
                box.add(b);
            }
            {
                Box b = Box.createHorizontalBox();
                b.add(new JLabel("AutoToken:"));
                b.add(autoToken);
                b.add(Box.createHorizontalGlue());
                box.add(b);
            }
            add(box);
        }

        void setData(ServerInfo serverInfo) {
            serverIp.setText(serverInfo.getServerIp());
            serverPort.setText(serverInfo.getServerPort() == null ? "" : String.valueOf(serverInfo.getServerPort()));
            autoToken.setText(serverInfo.getAutoToken());
        }

        ServerInfo getConf() {
            ServerInfo s = new ServerInfo();
            s.setAutoToken(getToken());
            s.setServerIp(getServerIp());
            s.setServerPort(getServerPort());
            return s;
        }

        private String getServerIp() {
            return serverIp.getText();
        }

        private String getToken() {
            return autoToken.getText();
        }

        private int getServerPort() {
            String text = serverPort.getText();
            if (text == null) {
                throw new RuntimeException("服务器端口不能为空");
            }
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException e) {
                throw new RuntimeException("服务器端口不正确:" + text);
            }
        }

    }


    private static class WorkGui extends JPanel {
        private JTextField serverPort = new JTextField(5);
        private JTextField localHost = new JTextField(10);
        private JTextField localPort = new JTextField(4);

        {
            setLayout(new GridLayout(1, 1));
            serverPort.setMaximumSize(serverPort.getPreferredSize());
            localHost.setMaximumSize(localHost.getPreferredSize());
            localPort.setMaximumSize(localPort.getPreferredSize());
        }

        WorkGui() {
            Box box = Box.createVerticalBox();
            {
                Box b = Box.createHorizontalBox();
                b.add(new JLabel("ServerPort:"));
                b.add(serverPort);
                b.add(Box.createHorizontalGlue());
                box.add(b);
            }
            {
                Box b = Box.createHorizontalBox();
                b.add(new JLabel("Local:"));
                b.add(localHost);
                b.add(new JLabel(":"));
                b.add(localPort);
                b.add(Box.createHorizontalGlue());
                box.add(b);
            }
            add(box);
        }

        public ServerWorker getConf() {
            ServerWorker s = new ServerWorker();
            s.setServerPort(getServerPort());
            s.setLocalHost(getLocalHost());
            s.setLocalPort(getLocalPort());
            return s;
        }

        void setData(ServerWorker sw) {
            serverPort.setText(sw.getServerPort() == null ? "" : String.valueOf(sw.getServerPort()));
            localHost.setText(sw.getLocalHost());
            localPort.setText(sw.getLocalPort() == null ? "" : String.valueOf(sw.getLocalPort()));
        }

        private String getLocalHost() {
            return localHost.getText();
        }

        private int getLocalPort() {
            String text = localPort.getText();
            if (text == null) {
                throw new RuntimeException("本地端口不能为空");
            }
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException e) {
                throw new RuntimeException("本地端口不正确:" + text);
            }
        }

        private int getServerPort() {
            String text = serverPort.getText();
            if (text == null || text.length() == 0) {
                throw new RuntimeException("服务器端口不能为空");
            }
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException e) {
                throw new RuntimeException("服务器端口不正确:" + text);
            }
        }
    }

}
