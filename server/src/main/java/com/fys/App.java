package com.fys;

import com.fys.conf.ClientInfo;
import com.fys.conf.Config;
import com.fys.conf.ServerInfo;
import com.fys.server.DataServer;
import com.fys.server.ManagerServer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static Options OPTIONS = new Options();

    /*
     * -c config.properties
     * */
    public static void main(String[] args) throws ParseException {
        Config config = parseConfig(args);
        ClientManager clientManager = new ClientManager();

        //创建控制端口监听
        ServerInfo serverInfo = config.getServerInfo();
        new ManagerServer(clientManager, serverInfo).start();

        //创建用户端口监听
        List<ClientInfo> clientInfoList = config.getClientInfos();
        for (ClientInfo clientInfo : clientInfoList) {
            new DataServer(clientManager, clientInfo).start();
        }
    }

    private static Config parseConfig(String[] args) throws ParseException {
        OPTIONS.addOption(Option.builder("config").required().hasArg(true).type(File.class).desc("the path of config").build());
        CommandLine parse = new DefaultParser().parse(OPTIONS, args);
        String configFile = parse.getOptionValue("config");
        return Config.read(configFile);
    }

}
