package com.fys.connection;

import com.fys.InnerConnectionFactory;
import com.fys.cmd.handler.CmdEncoder;
import com.fys.cmd.listener.ErrorLogListener;
import com.fys.cmd.message.Cmd;
import com.fys.cmd.message.DataConnectionCmd;
import com.fys.cmd.message.clientToServer.LoginCmd;
import com.fys.handler.CmdDecoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/4/27 23:07
 */
public class ManagerConnection {

    private static final Logger log = LoggerFactory.getLogger(ManagerConnection.class);

    private String clientName;
    private String serverHost;
    private int serverPort;

    /**
     * 创建控制连接
     *
     * @param clientName
     * @param serverHost
     * @param serverPort
     */
    public ManagerConnection(String clientName, String serverHost, int serverPort) {
        this.clientName = clientName;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    /**
     * 创建控制连接，并添加合适的handler。
     * 注:添加handler要在监听器中添加，否则handler的active可能在连接前就触发过了，channelActive方法不一定能触发
     * 而放在监听器中能保证触发，因为监听器的回调在active回调之前执行
     */
    public void start() {
        ChannelFuture future = InnerConnectionFactory.createChannel(serverHost, serverPort, true);
        future.addListener((ChannelFutureListener) f -> {
            ChannelPipeline pipeline = f.channel().pipeline();
            pipeline.addLast(new CmdEncoder());

            pipeline.addLast(new CmdDecoder());
            pipeline.addLast(new ManagerHandler());
        });
    }

    private class ManagerHandler extends SimpleChannelInboundHandler<Cmd> {

        /**
         * 连接服务器成功后，发送登录指令
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(new LoginCmd(clientName)).addListener(ErrorLogListener.INSTANCE);
        }

        /**
         * 读取到数据后，开始处理
         */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Cmd msg) {
            if (msg instanceof DataConnectionCmd) {
                DataConnectionCmd cmd = (DataConnectionCmd) msg;
                DataConnection dataConnection = new DataConnection(cmd.getLocalHost(), cmd.getLocalPort(), serverHost, serverPort, cmd);
                dataConnection.startConnection();
                return;
            }
            log.info("收到未识别的消息:{}", msg);
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("连接报错", cause);
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }


}
