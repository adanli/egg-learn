package com.egg.integration.eggnio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * 在读取内容过程中可能会遇到沾包、断包的情况
 *
 */
@Component
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 8098;
    private static final int BUFFER_LENGTH = 1024;
    private static final int SERVER_SELECT_INTEVAL = 1000;

    private ServerSocketChannel ssc;
    private ByteBuffer buffer;

    @PostConstruct
    public void init() {
        try {
            ssc = ServerSocketChannel.open();
            SocketAddress socketAddress = new InetSocketAddress(PORT);
            ssc.bind(socketAddress);
            ssc.configureBlocking(false);
            logger.info("create server socket channel success, bind port {}", PORT);

            // register selectable channel into selector
            registerChannelIntoSelector(ssc);

        } catch (IOException e) {
            logger.error("create server socket channel error", e);
        }

    }

    private void registerChannelIntoSelector(ServerSocketChannel ssc) {
        try {
            Selector selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("register ssc into selector success");

            while (true) {
                if(selector.select(SERVER_SELECT_INTEVAL) == 0) {
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if(!key.isValid()) continue;

                    if(key.isReadable()) {
                        handleReadKey(key);
                    } else if(key.isWritable()) {
                        handleWriteKey(key);
                    } else if(key.isConnectable()) {
                        handleConnectKey(key);
                    } else if(key.isAcceptable()) {
                        handleAcceptKey(key);
                    }

                    iterator.remove();
                }
            }

        }catch (IOException e) {
            logger.error("", e);
        }
    }

    /**
     * deal read key
     * assemble package, to get the real message content
     */
    private void handleReadKey(SelectionKey key) {
        logger.info("deal read key: {}", key);
        SocketChannel channel = (SocketChannel) key.channel();
        if(key.isValid()) {
            buffer = ByteBuffer.allocate(BUFFER_LENGTH);
            buffer.clear();
            try {
                int count = channel.read(buffer);
                if(count > 0) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    String msg = new String(bytes, Charset.defaultCharset());
                    logger.info("read message: {}", msg);
                    key.interestOps(SelectionKey.OP_WRITE);

                } else {
                    logger.info("client disconnect, so ... close the client channel");
                    key.cancel();
                    channel.close();
                }



            }catch (IOException e) {
                logger.error("read from channel error", e);
            }

        }
    }

    /**
     * deal write key
     */
    private void handleWriteKey(SelectionKey key) {
        logger.info("deal write key: {}", key);
        if(key.isValid()) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String msg = new String(bytes, Charset.defaultCharset());

            if(msg.trim().equals("quit")) {
                key.cancel();
                logger.info("finish connect: {}", ((SocketChannel)(key.channel())).isConnected());
            } else {
                if(!msg.trim().equals(""))
                    sendReceiveMsgFromReadKey(msg, key);
            }

            buffer.clear();

        }
        key.interestOps(SelectionKey.OP_READ);

    }

    /**
     * deal connect key
     */
    private void handleConnectKey(SelectionKey key) {
        logger.info("deal connect key: {}", key);
    }

    /**
     * deal connect key
     */
    private void handleAcceptKey(SelectionKey key) {
        logger.info("deal accept key: {}", key);

        try {
            SocketChannel channel = ((ServerSocketChannel)(key.channel())).accept();
            // set client channel unblock
            channel.configureBlocking(false);
            channel.register(key.selector(), SelectionKey.OP_READ);

        }catch (IOException e) {
            logger.error("register client socket error", e);
        }

    }


    private void sendReceiveMsgFromReadKey(String msg, SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        buffer = ByteBuffer.allocate(BUFFER_LENGTH);

        String backMsg = msg == null?"receive":"receive " + msg;
        logger.info("backMsg: {}", backMsg);
        buffer.put(backMsg.getBytes());
        buffer.flip();

        try {
            channel.write(buffer);
        }catch (IOException e) {
            logger.info("response msg error", e);
        }

    }

    public static void main(String[] args) {
        Thread t = new Thread() {
            public void run() {
                try {
                    long start = System.currentTimeMillis();
                    Thread.sleep(30000);
                    long end = System.currentTimeMillis();
                    logger.info("{} wake up in {} seconds", Thread.currentThread().getName(), (end-start)/1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        t.start();
        try {
            TimeUnit.SECONDS.sleep(3);
            t.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static int length(int a) {
        if(a == 0) return 0;
        return length(a/10)+1;
    }

}
