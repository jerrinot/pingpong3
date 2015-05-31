package info.jerrinot.pingpong3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Utils {
    public static void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static ByteBuffer createBuffer(int packetSizeBytes) {
        String type = System.getProperty("buffer", "direct");
        if ("direct".equals(type)) {
            return ByteBuffer.allocateDirect(packetSizeBytes);
        } else if ("heap".equals(type)) {
            return ByteBuffer.allocate(packetSizeBytes);
        } else if ("mm".equals(type)) {
            return createMMBuffer(packetSizeBytes);
        } else {
            throw new IllegalArgumentException("Unsupported Buffer Type: " + type);
        }
    }

    private static ByteBuffer createMMBuffer(int packetSizeBytes) {
        try {
            File file = File.createTempFile("pingpong", ".tmp", new File("/dev/shm"));
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            MappedByteBuffer map = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, packetSizeBytes);
            return map;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
