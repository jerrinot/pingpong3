package info.jerrinot.pingpong3.server;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static info.jerrinot.pingpong3.Constants.PACKET_SIZE_BYTES;
import static info.jerrinot.pingpong3.Utils.createBuffer;

public class ConnectionThread extends Thread {
    private final SocketChannel channel;
    private boolean stopRequested = false;

    public ConnectionThread(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        ByteBuffer buffer = createBuffer(PACKET_SIZE_BYTES);

        try {
            while (!stopRequested) {
                readAndWrite(buffer);
            }
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAndWrite(ByteBuffer buffer) throws IOException {
        readPacket(buffer);
        buffer.flip();
        long code = buffer.get(0);
        if (code == -1) {
            stopRequested = true;
            return;
        }

        writePacket(buffer);
        buffer.flip();
    }

    private void writePacket(ByteBuffer buffer) throws IOException {
        int wrote = 0;
        do {
        } while ( (wrote += channel.write(buffer)) != PACKET_SIZE_BYTES);
    }

    private void readPacket(ByteBuffer buffer) throws IOException {
        int read = 0;
        do {
        } while ( (read += channel.read(buffer)) != PACKET_SIZE_BYTES);
    }
}
