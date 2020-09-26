package com.egg.integration.eggreactor.reactor.multiMulti;

import com.egg.integration.eggreactor.reactor.multiMulti.acceptor.Acceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * @author adan
 * 多MainReactor多线程Handler
 * MainReactor只负责接收accept请求，并将请求发送给Acceptor
 */
public class MainReactor implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(MainReactor.class);
    private Selector selector;
    private boolean open;

    private ServerSocketChannel serverSocketChannel;

    private static MainReactor mainReactor;

    private static final Object mainReactorLock = new Object();

    private MainReactor(int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            open = true;

            selector = Selector.open();
            SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            key.attach(new Acceptor(serverSocketChannel));

            logger.info("mainReactor start success");

        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public static MainReactor provider(int port) {
        synchronized (mainReactorLock) {
            if(mainReactor != null) {
                return mainReactor;
            }
            return new MainReactor(port);
        }
    }

    @Override
    public void run() {
        while (open) {
            try {
                if(selector.select() == 0) {
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if(key.isValid())
                        accept(key);
                }


            } catch (IOException e) {
                logger.error("", e);
            }
        }
    }

    public void stop() {
        this.open = false;
    }

    private void accept(SelectionKey key) {
        Runnable runnable = (Runnable) key.attachment();
        if(runnable != null) {
            runnable.run();
        }

    }

}
