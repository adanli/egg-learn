package com.egg.integration.eggreactor.reactor.multiMulti.acceptor;

import com.egg.integration.eggreactor.reactor.multiMulti.SubReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * 接收到accept请求，并将通道绑定到subReactor的Selector上
 */
public class Acceptor implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Acceptor.class);
    private ServerSocketChannel serverSocketChannel;

    private int coreSize = 1;

    private SubReactor[] subReactors = new SubReactor[coreSize];


    public Acceptor(ServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
        for(int i=0; i<subReactors.length; i++) {
            subReactors[i] = new SubReactor(i);
            new Thread(subReactors[i]).start();
            logger.info("subReactor-{} start", i);
        }
    }

    @Override
    public void run() {
        try {
            SocketChannel channel = serverSocketChannel.accept();
            channel.configureBlocking(false);
            SubReactor reactor = randomSubReactor();
            reactor.register(channel);

        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private Random random = new Random();

    private SubReactor randomSubReactor() {
        return subReactors[random.nextInt(coreSize)];
    }

}
