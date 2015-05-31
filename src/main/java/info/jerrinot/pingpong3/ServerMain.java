package info.jerrinot.pingpong3;

import info.jerrinot.pingpong3.server.Server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ServerMain {
    public static void main(String[] args) {
        String ip = System.getProperty("ip", "127.0.0.1");
        int port = Integer.getInteger("port", Constants.PORT);

        Server server = new Server();
        SocketAddress address = new InetSocketAddress(ip, port);
        server.listen(address);
    }
}
