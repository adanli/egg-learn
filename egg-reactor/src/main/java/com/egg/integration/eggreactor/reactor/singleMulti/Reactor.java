package com.egg.integration.eggreactor.reactor.singleMulti;

import com.egg.integration.eggreactor.reactor.singleMulti.acceptor.Acceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单线程Reactor多线程Handler
 * Reactor接收客户端请求，如果acceptor请求，交给Acceptor，其他交给Handler
 * 此处Handler内部使用连接池
 */
public class Reactor implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Reactor.class);

    private static final Object reactorLock = new Object();
    private static Reactor reactor;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private boolean open;


    private Reactor(int port) {
        open = true;
        try {
            serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();

            SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            key.attach(new Acceptor(serverSocketChannel, selector));

            logger.info("server bind {} success", port);

        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public static Reactor provider(int port) {
        synchronized (reactorLock) {
            if(reactor != null) {
                return reactor;
            }
            reactor = new Reactor(port);
            return reactor;
        }
    }

    @Override
    public void run() {
        try {
            while (open) {
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
            }
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable runnable = (Runnable) key.attachment();
        if(runnable != null) {
            runnable.run();
        }

    }

    public void stop() {
        this.open = false;
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            logger.info("start {} - ok", Thread.currentThread().getName());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            logger.info("end {} - ok", Thread.currentThread().getName());
        });
        executorService.execute(() -> {
            logger.info("start {} - ok", Thread.currentThread().getName());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            logger.info("end {} - ok", Thread.currentThread().getName());
        });
        logger.info("{} - ok", Thread.currentThread().getName());
        /*new Thread(() -> {
            logger.info("start {} - ok", Thread.currentThread().getName());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            logger.info("end {} - ok", Thread.currentThread().getName());
        }).start();*/

        /*try {
            Thread.sleep(10000);
        }catch (InterruptedException e) {
            logger.error("", e);
        }*/

        logger.info("thread count: {}", Thread.activeCount());
        Thread.currentThread().getThreadGroup().list();

    }

}
