package nl.nlcode.m.cli;

import java.io.File;
import java.io.StringWriter;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jq59bu
 */
public abstract class CliTest {

    ControlCli instance;
    SynchronousQueue<String> toTerminal;
    Thread instanceThread;

    public CliTest() {
        createInstance();
    }
    
    protected void createInstance() {
        instance = ControlCli.createUnitTestInstance();
        instance.testCommandCompleterBarrier = new CyclicBarrier(2);
        instance.stdoutTestEcho = new StringWriter();
        toTerminal = new SynchronousQueue<>();
        instance.commandTestSupplier = (() -> {
            try {
                return toTerminal.poll(25000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.interrupted();
                e.printStackTrace(System.err);
                throw new RuntimeException(e);
            }
        });
        instanceThread = new Thread(() -> instance.run());
        instanceThread.setName("instanceThread");
        instanceThread.start();
    }

    protected String execute(String s) {
        writeToTerminal(s);
        return readFromTerminal();
    }
    
    protected void writeToTerminal(String s) {
        try {
            toTerminal.offer(s, 25000, TimeUnit.MILLISECONDS);
            try {
                instance.testCommandCompleterBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.interrupted();
                e.printStackTrace(System.err);
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    protected String readFromTerminal() {
        String result = instance.stdoutTestEcho.toString();
        instance.stdoutTestEcho.getBuffer().setLength(0);
        return result;
    }

    protected String pwd() {
        //return Paths.get("").toAbsolutePath().toString();
        return instance.getControl().getProjectDirectory().toString() + File.separator;
    }
    
 
}
