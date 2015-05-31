package info.jerrinot.pingpong3;

import info.jerrinot.pingpong3.client.BlockingNIOClient;
import org.HdrHistogram.AtomicHistogram;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.net.InetSocketAddress;

public class ClientMain {
    private static final Logger log = LoggerFactory.getLogger(ClientMain.class);

    private Integer noOfConnections;
    private Integer port;
    private int durationMs;
    private int warmupMs;
    private InetSocketAddress address;
    private BlockingNIOClient client;
    private Histogram histogram;


    public static void main(String[] args) throws InterruptedException {
        new ClientMain().run();
    }

    private void run() throws InterruptedException {
        String ip = System.getProperty("ip", "127.0.0.1");
        noOfConnections = Integer.getInteger("connections", 1);
        port = Integer.getInteger("port", Constants.PORT);
        durationMs = Integer.getInteger("duration.seconds", 10) * 1000;
        warmupMs = Integer.getInteger("warmup.seconds", 10) * 1000;


        client = new BlockingNIOClient();
        address = new InetSocketAddress(ip, port);

        histogram = noOfConnections > 1 ? new AtomicHistogram(1000000000, 3) : new Histogram(1000000000, 3);


        warmUp();
        histogram.reset();

        test();
        long totalCount = histogram.getTotalCount();
        long opsPerSecond = totalCount / (durationMs / 1000);
        long opsPerSecondPerThread = opsPerSecond / noOfConnections;

        log.info("Total Operations: " + totalCount);
        log.info("Operations per Second: " + opsPerSecond);
        log.info("Operations per Second and Thread: " + opsPerSecondPerThread);

        PrintStream stream = System.out;
        histogram.outputPercentileDistribution(stream, 1.0);
    }

    private void test() throws InterruptedException {
        log.info("Starting Test");
        client.connect(address, noOfConnections, histogram);
        Thread.sleep(durationMs);
        client.stop();
        log.info("Test done");
    }

    private void warmUp() throws InterruptedException {
        log.info("Starting Warm-Up");

        client.connect(address, noOfConnections, histogram);
        Thread.sleep(warmupMs);
        client.stop();

        log.info("Warm-up done");
    }
}
