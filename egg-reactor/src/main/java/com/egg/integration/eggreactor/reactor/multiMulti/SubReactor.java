package com.egg.integration.eggreactor.reactor.multiMulti;

import com.egg.integration.eggreactor.reactor.multiMulti.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 接收read和write请求，并调度给实际的Handler处理
 */
public class SubReactor implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(SubReactor.class);
    private Selector selector;
    private boolean open;
    private int subReactorNum;

    public SubReactor(int subReactorNum) {
        this.subReactorNum = subReactorNum;
        try {
            selector = Selector.open();
            open = true;

        } catch (IOException e) {
            logger.error("", e);
        }
    }

    /**
     * 由于SubReactor这里是用线程启动的，因此run在register的时候已经执行了，此处由于selector.select()速度太大，synchronized(this.publicKeys)会一直拿着锁，导致channel.register(selector, ops)时候selector拿不到锁，导致卡住
     * @param channel
     */
    public void register(SocketChannel channel) {
        try {
            stop();
            selector.wakeup();
            SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
            selector.wakeup();
            start();
            logger.info("register key: {}", key);
            logger.info("channel register into selector-{}", subReactorNum);
            key.attach(new Handler(channel));
        } catch (ClosedChannelException e) {
            logger.error("", e);
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            while (!Thread.interrupted() && open) {
                try {
                    if(selector.select() == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        dispatch(key);
                    }
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable runnable = (Runnable) key.attachment();
        if(runnable != null) {
            runnable.run();
        }
    }


    private void start() {
        this.open = true;
    }
    private void stop() {
        this.open = false;
    }

}
