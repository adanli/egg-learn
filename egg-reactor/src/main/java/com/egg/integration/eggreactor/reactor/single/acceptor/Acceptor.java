package com.egg.integration.eggreactor.reactor.single.acceptor;

import com.egg.integration.eggreactor.reactor.single.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author adan
 * 负责处理acceptor请求，并将感兴趣事件注册到OP_READ
 */
public class Acceptor implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Acceptor.class);

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public Acceptor(ServerSocketChannel serverSocketChannel, Selector selector) {
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            SocketChannel channel = serverSocketChannel.accept();
            if(channel != null) {

                logger.info("remote {} connect success", channel.getRemoteAddress());

                new Handler(channel, selector);
            }

        } catch (IOException e) {
            logger.error("", e);
        }

    }
}
