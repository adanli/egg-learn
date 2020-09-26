package com.egg.integration.eggnio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * nio client
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final String remoteServerAddress = "localhost";
    private static final int remoteServerPort = 8098;
    private static final int BUFFER_LENGTH = 1024;
    private static final byte[] PACKAGE_LENGTH = "Length: ".getBytes(Charset.defaultCharset());

    public static void main(String[] args) {
        int index = 0;

            SocketChannel client = null;
            try {
                client = SocketChannel.open();
                client.configureBlocking(false);
                logger.info("client channel is register: {}", client.isRegistered());

                SocketAddress remoteAddress = new InetSocketAddress(remoteServerAddress, remoteServerPort);
                boolean connect = client.connect(remoteAddress);
                logger.info("connect {}:{} {}", remoteServerAddress, remoteServerPort, connect);

            } catch (IOException e) {
                logger.error("create client error", e);
            }

            /*while (true) {
                if (client != null) {
                    try {
                        client.close();
                        client = null;
                    }catch (IOException e) {

                    }
                }
            }*/

            try {
                while (client!=null && client.finishConnect()) {
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);
                    String msg = "hello, i am client"+(index++);
                    buffer.put(msg.getBytes());
                    buffer.flip();

                    try {
                        client.write(buffer);
                    }catch (IOException e) {
                        logger.error("", e);
                    }


                    // receive message from server
                    buffer.clear();
                    try {
                        client.read(buffer);
                    } catch (IOException e) {
                        logger.info("read from server error", e);
                    }
                    buffer.flip();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    logger.info("receive from server: {}", new String(bytes, Charset.defaultCharset()));

                    try {
                        Thread.sleep(5000);

                    }catch (Exception e) {
                        logger.error("", e);
                    }

                }

            } catch (IOException e) {
                logger.error("", e);
            } finally {
                if(client != null)
                    try {
                        client.close();
                        logger.info("close client channel success");
                    } catch (IOException e) {
                        logger.error("close client channel error", e);
                    }
            }

    }

    /**
     * like
     * Length: 1\r\n`${content}\r\n
     * @param buffer
     * @param bytes
     */
    private static void write(SocketChannel channel, ByteBuffer buffer, byte[] bytes) {
        int byteLength = bytes.length;
        int lengthOfContentLength = length(byteLength);
        byte[] bs = new byte[PACKAGE_LENGTH.length + 2 + lengthOfContentLength + bytes.length + 2];
        int i = 0;
        for (; i< PACKAGE_LENGTH.length; i++) {
            bs[i] = PACKAGE_LENGTH[i];
        }

        byte[] bytesOfLengthOfContentLength = new byte[lengthOfContentLength];
        convertLengthOfByteLengthIntoBytes(bytes, byteLength);
        for(int j=0; j<bytesOfLengthOfContentLength.length; j++) {
            bs[i++] = bytesOfLengthOfContentLength[j];
        }

        bs[i++] = '\r';
        bs[i++] = '\n';
        for(int j=0; j< bytes.length; j++) {
            bs[i++] = bytes[j];
        }
        bs[i++] = '\r';
        bs[i] = '\n';

        buffer.put(bs);
        buffer.flip();

        try {
            channel.write(buffer);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    /**
     * convert byte.length.length into byte[]
     * like 12345 into byte[]{1, 2, 3, 4, 5}, then the length of byte is 5
     * @param a
     * @return
     */
    private static void convertLengthOfByteLengthIntoBytes(byte[] bytes, int a) {
        for(int i= bytes.length-1; i>=0; i--) {
            bytes[i] = (byte) (a%10);
            a = a - a%10;
        }
    }

    private static int length(int a) {
        if(a == 0) return 0;
        return length(a/10)+1;
    }

}
