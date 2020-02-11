package bg.sofia.uni.fmi.mjt.spotify.server.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorUtilTest {

    private static final int SLEEP = 1000;
    private static final int TIMEOUT = 2500;

    @Test(timeout = TIMEOUT)
    public void testExecutorShutdown() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        ExecutorUtil.shutdown(executorService);

        Thread.sleep(SLEEP);

        Assert.assertTrue(executorService.isShutdown());
    }
}
