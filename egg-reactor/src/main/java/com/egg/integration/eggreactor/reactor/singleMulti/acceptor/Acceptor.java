package com.egg.integration.eggreactor.reactor.singleMulti.acceptor;

import com.egg.integration.eggreactor.reactor.singleMulti.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

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
                logger.info("{} connect success", channel.getRemoteAddress());
                new Handler(channel, selector);
            }

        } catch (IOException e) {
            logger.error("", e);
        }
    }
}
