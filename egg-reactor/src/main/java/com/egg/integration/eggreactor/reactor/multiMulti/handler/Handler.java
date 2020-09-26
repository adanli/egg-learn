package com.egg.integration.eggreactor.reactor.multiMulti.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 处理实际的业务逻辑
 */
public class Handler implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Handler.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    private enum State{READ, WRITE, IN_PROCESS}

    private State state;

    private ByteBuffer buffer;
    private SocketChannel channel;

    public Handler(SocketChannel channel) {
        this.channel = channel;
        buffer = ByteBuffer.allocate(1024);
        state = State.READ;
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
        state(State.IN_PROCESS);

        executorService.submit(() -> {
            process();
            state(State.READ);
        });

    }

    private void process() {
        try {
            buffer.clear();
            int read = channel.read(buffer);
            if(read < 0) {
                channel.close();
            } else {
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
            }

        } catch (IOException e) {
            logger.error("", e);
        }

    }

    private void write() {

    }

    private void state(State state) {
        this.state = state;
    }

}
