package com.fys.cmd.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hcy
 * @since 2022/5/1 22:13
 */
public class Listeners {

    public static ChannelFutureListener ERROR_LOG = new ErrorLogListener();


    private static class ErrorLogListener implements ChannelFutureListener {

        private static final Logger log = LoggerFactory.getLogger(ErrorLogListener.class);

        @Override
        public void operationComplete(ChannelFuture future) {
            if (!future.isSuccess()) {
                log.error("", future.cause());
            }
        }
    }

}
