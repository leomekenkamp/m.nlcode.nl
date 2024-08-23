package nl.nlcode.m.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author leo
 */
public class JvmStuff {
    
    private static JvmStuff instance = new JvmStuff();
    
    private ScheduledExecutorService scheduledExecutorService;
    
    private JvmStuff() {
        scheduledExecutorService = Executors.newScheduledThreadPool(nonBlockingExecutorService().getParallelism());
    }
    
    public static JvmStuff getInstance() {
        return instance;
    }
    
    public ForkJoinPool nonBlockingExecutorService() {
        return ForkJoinPool.commonPool();
    }
    
    public ScheduledExecutorService scheduledExecutorService() {
        return scheduledExecutorService;
    }
            
}
