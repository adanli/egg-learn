package com.egg.integration.eggreactor.reactor.single;

import com.egg.integration.eggreactor.reactor.single.acceptor.Acceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * @author
 * 单线程Reactor单线程Handler
 * 此处Reactor只负责接收客户端的请求，遇到accept，转发给Acceptor，遇到其他请求，转发给Handler
 */
public class Reactor implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Reactor.class);

    private Selector selector;
    private boolean open = true;
    private int port;
    private static Reactor reactor;

    private final static Object reactorLock = new Object();

    private Reactor(int port) {
        this.port = port;
        init();
    }

    public static Reactor provider(int port) {
        synchronized (reactorLock) {
            if(reactor != null) {
                return reactor;
            } else {
                reactor = new Reactor(port);
                return reactor;
            }
        }
    }

    @Override
    public void run() {

        while (open) {
            if(selector == null) {
                throw new RuntimeException("selector can not be null");
            }

            try {
                if(selector.select() == 0) {
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if(key.isValid()) {
                        dispatch(key);
                    }
                }

            } catch (IOException e) {
                logger.error("", e);
            }

        }

    }

    private void dispatch(SelectionKey key) {
        Runnable runnable = (Runnable) key.attachment();
        if(runnable != null) {
            runnable.run();
        }
    }

    private void init() {
        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.bind(new InetSocketAddress(port));
            channel.configureBlocking(false);

            selector = Selector.open();
            SelectionKey key = channel.register(selector, SelectionKey.OP_ACCEPT);
            key.attach(new Acceptor(channel, selector));

            logger.info("server bind {} success", port);

        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public void stop() {
        this.open = false;
    }

}
