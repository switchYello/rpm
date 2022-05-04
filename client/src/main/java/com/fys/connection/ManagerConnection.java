package com.fys.connection;

import com.fys.Debug;
import com.fys.InnerConnectionFactory;
import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.handler.ErrorLogHandler;
import com.fys.cmd.handler.PingHandler;
import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.LoginCmd;
import com.fys.cmd.message.LoginFailCmd;
import com.fys.cmd.message.NeedDataConnectionCmd;
import com.fys.handler.CmdDecoder;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/4/27 23:07
 */
public class ManagerConnection {

    private static final Logger log = LoggerFactory.getLogger(ManagerConnection.class);

    private String clientName;
    private String serverToken;
    private String serverHost;
    private int serverPort;


    /**
     * 创建控制连接
     *
     * @param clientName
     * @param serverHost
     * @param serverPort
     */
    public ManagerConnection(String clientName, String serverToken, String serverHost, int serverPort) {
        this.clientName = clientName;
        this.serverToken = serverToken;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    /**
     * 创建控制连接，并添加合适的handler。
     * 注:添加handler要在监听器中添加，否则handler的active可能在连接前就触发过了，channelActive方法不一定能触发
     * 而放在监听器中能保证触发，因为监听器的回调在active回调之前执行
     */
    public ChannelFuture start() {
        ChannelFuture future = InnerConnectionFactory.createChannel(serverHost, serverPort, true);
        future.addListener((ChannelFutureListener) f -> {
            ChannelPipeline pipeline = f.channel().pipeline();
            if (Debug.isDebug) {
                pipeline.addLast(new LoggingHandler());
                pipeline.addLast(new CmdEncoder());
                pipeline.addLast(new CmdDecoder());
                pipeline.addLast(new ManagerHandler());
                pipeline.addLast(new ErrorLogHandler());
            } else {
                pipeline.addLast(new CmdEncoder());
                pipeline.addLast(new CmdDecoder());
                pipeline.addLast(new PingHandler()); //定时发ping
                pipeline.addLast(new ManagerHandler());
                pipeline.addLast(new ErrorLogHandler());
            }
        });
        return future;
    }

    private class ManagerHandler extends SimpleChannelInboundHandler<Cmd> {
        /**
         * 连接服务器成功后，发送登录指令
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(new LoginCmd(clientName, serverToken)).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }

        /**
         * 读取到数据后，开始处理
         */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Cmd msg) {
            if (msg instanceof NeedDataConnectionCmd) {
                log.debug("收到服务端 NeedDataConnectionCmd");
                NeedDataConnectionCmd cmd = (NeedDataConnectionCmd) msg;
                DataConnection dataConnection = new DataConnection(serverHost, serverPort, cmd.getLocalHost(), cmd.getLocalPort(), cmd);
                dataConnection.startConnection();
                return;
            }
            if (msg instanceof LoginFailCmd) {
                LoginFailCmd cmd = (LoginFailCmd) msg;
                log.error("登录认证失败:{}", cmd);
                ctx.close();
                return;
            }
            log.error("收到未识别的消息:{}", msg);
            ctx.close();
        }
    }


}
