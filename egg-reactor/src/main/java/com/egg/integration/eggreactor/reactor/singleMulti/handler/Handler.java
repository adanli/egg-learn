package com.egg.integration.eggreactor.reactor.singleMulti.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.*;

public class Handler implements Runnable{
    private static final int INIT_SIZE = 10;
    private static ExecutorService executorService = Executors.newFixedThreadPool(INIT_SIZE);
    private static final Logger logger = LoggerFactory.getLogger(Handler.class);

    private SocketChannel channel;
    private Selector selector;
    private SelectionKey key;

    private ByteBuffer buffer;

    /**
     * 0: read
     * 1: write
     * 2: in process
     */
    private enum State {READ, WRITE, IN_PROCESS};

    private State state;

    public Handler(SocketChannel channel, Selector selector) {
        this.channel = channel;
        this.selector = selector;
        buffer = ByteBuffer.allocate(1024);

        try {
            channel.configureBlocking(false);
            key = channel.register(this.selector, SelectionKey.OP_READ);
            state = State.READ;
            key.attach(this);
        } catch (IOException e) {
            logger.error("", e);
        }

    }

    @Override
    public void run() {
        switch (state) {
            case READ:
                read();
                break;
            case WRITE:
                write();
                break;
            default:
                break;
        }
    }

    private void read() {
        logger.info("{} read", Thread.currentThread().getName());
        state(State.IN_PROCESS);
        executorService.submit(() -> {
            process();
        });

        logger.info("after read");

//        key.interestOps(SelectionKey.OP_WRITE);
//        state = 1;
    }

    private void write() {

        executorService.submit(() -> {
            process();
        });

        key.interestOps(SelectionKey.OP_READ);
        state(State.READ);

    }

    private void process() {
        buffer.clear();
        try {
            channel.read(buffer);
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String body = new String(bytes, Charset.defaultCharset());
            logger.info("receive: {}", body);

            String response = "response: " + body;
            buffer.clear();
            buffer.put(response.getBytes());
            buffer.flip();
            channel.write(buffer);

            state(State.READ);

        } catch (IOException e) {
            logger.error("", e);
        }

    }

    private void state(State state) {
        this.state = state;
    }

}
