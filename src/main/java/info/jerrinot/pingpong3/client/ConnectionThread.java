package info.jerrinot.pingpong3.client;

import info.jerrinot.pingpong3.Utils;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

import static info.jerrinot.pingpong3.Constants.PACKET_SIZE_BYTES;

public class ConnectionThread extends Thread {
    private static final Logger log = LoggerFactory.getLogger(ConnectionThread.class);

    private final SocketChannel channel;
    private final Histogram histogram;

    private volatile boolean stopped = false;


    public ConnectionThread(SocketChannel channel, Histogram histogram) {
        this.channel = channel;
        this.histogram = histogram;
    }

    @Override
    public void run() {
        ByteBuffer buffer = Utils.createBuffer(PACKET_SIZE_BYTES);
        initRandom(buffer);
        buffer.flip();

        try {
            while (!stopped) {
                writeAndRead(buffer);
            }
            buffer.putLong(0, -1);
            writePacket(buffer);
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestStop() {
        stopped = true;
    }

    private void writeAndRead(ByteBuffer buffer) throws IOException {
        updateTimestampInBuffer(buffer);

        writePacket(buffer);
        buffer.flip();

        readPacket(buffer);
        recordLatency(buffer);

        buffer.flip();
    }

    private void recordLatency(ByteBuffer buffer) {
        long start = buffer.getLong(0);
        long now = System.nanoTime();
        long latencyNs = now - start;
        histogram.recordValue(latencyNs);
    }

    private void updateTimestampInBuffer(ByteBuffer buffer) {
        long currentTime = System.nanoTime();
        buffer.putLong(0, currentTime);
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

    private void initRandom(ByteBuffer buffer) {
        Random random = new Random();
        for (int i = 0, len = buffer.remaining(); i < len; )
            for (int rnd = random.nextInt(),
                 n = Math.min(len - i, Integer.SIZE/Byte.SIZE);
                 n-- > 0; rnd >>= Byte.SIZE) {
                buffer.put((byte) rnd);
                i++;
            }
    }
}
