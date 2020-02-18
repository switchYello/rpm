package com.fys.handler;

import com.fys.cmd.handler.CmdEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * hcy 2020/2/18
 */
public class ClientHandlerInit extends ChannelInitializer<Channel> {

    private ClientHandler clientHandler = new ClientHandler();

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new CmdEncoder());
        ch.pipeline().addLast(clientHandler);
    }


}
