package com.egg.integration.eggreactor.reactor.single.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * @author adan
 * 负责处理感兴趣事件OP_READ/OP_WRITE
 */
public class Handler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Handler.class);
    private static final int BUFFER_LENGTH = 1024;

    private SocketChannel socketChannel;
    private Selector selector;
    private SelectionKey key;
    private ByteBuffer byteBuffer;

    /**
     * 0: read
     * 1: write
     */
    private int state;

    public Handler(SocketChannel socketChannel, Selector selector) {
        this.socketChannel = socketChannel;
        this.selector = selector;
        this.byteBuffer = ByteBuffer.allocate(BUFFER_LENGTH);
        // init
        try {
            this.socketChannel.configureBlocking(false);
            key = socketChannel.register(this.selector, SelectionKey.OP_READ);
            key.attach(this);
            this.state = 0;

        } catch (IOException e) {
            logger.error("", e);
        }

    }

    @Override
    public void run() {
        if(state == 0) {
            read();
        } else if(state == 1) {
            write();
        } else {
            throw new RuntimeException("error state, not read nor write");
        }
    }

    private void read() {
        byteBuffer.clear();
        try {
            int length = socketChannel.read(byteBuffer);
            if(length < 0) {
                logger.info("client channel close, so ... close the client connect");
                key.cancel();
                socketChannel.close();
            } else {
                byteBuffer.flip();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                String body = new String(bytes, Charset.defaultCharset());
                logger.info("receive {}", body);

                key.interestOps(SelectionKey.OP_WRITE);
                state = 1;

            }

        } catch (IOException e) {
            logger.error("", e);
        }



    }

    private void write() {
        byteBuffer.flip();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        String response = "receive " + new String(bytes, Charset.defaultCharset());
        byteBuffer.clear();
        byteBuffer.put(response.getBytes());
        byteBuffer.flip();
        try {
            socketChannel.write(byteBuffer);
            key.interestOps(SelectionKey.OP_READ);
            state = 0;
        } catch (IOException e) {
            logger.error("", e);
        }

    }

}
