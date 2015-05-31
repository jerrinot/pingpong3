package info.jerrinot.pingpong3.client;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class BlockingNIOClient {
    private static final Logger log = LoggerFactory.getLogger(BlockingNIOClient.class);
    private ConnectionThread[] threads;

    public void connect(SocketAddress socketAddress, int noOfConnections, Histogram histogram) {
        checkNotThreadsAreRunning();

        log.info("Creating " + noOfConnections + " connections to " + socketAddress);
        threads = new ConnectionThread[noOfConnections];
        try {
            for (int i = 0; i < noOfConnections; i++) {
                SocketChannel channel = SocketChannel.open();
                channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
                channel.connect(socketAddress);
                channel.configureBlocking(Boolean.getBoolean("blocking"));

                while (!channel.isConnected());

                ConnectionThread thread = new ConnectionThread(channel, histogram);
                threads[i] = thread;
                thread.setDaemon(true);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkNotThreadsAreRunning() {
        if (threads == null) {
            return;
        }

        for (int i = 0; i < threads.length; i++) {
            if (threads[i].isAlive()) {
                throw new IllegalStateException("There are threads still running. ");
            }
        }
    }

    public void stop() {
        for (int i = 0; i < threads.length; i++) {
            threads[i].requestStop();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}
