package info.jerrinot.pingpong3.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    public void listen(SocketAddress address) {
        log.info("Listening on " + address);

        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(true);
            serverChannel.bind(address);


            for (;;) {
                SocketChannel newChannel = serverChannel.accept();
                newChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
                newChannel.configureBlocking(Boolean.getBoolean("blocking"));
                log.info("Accepting a new connection.");
                ConnectionThread connectionThread = new ConnectionThread(newChannel);
                connectionThread.setDaemon(true);
                connectionThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
