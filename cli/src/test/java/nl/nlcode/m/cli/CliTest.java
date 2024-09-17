package nl.nlcode.m.cli;

import java.io.File;
import java.io.StringWriter;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import nl.nlcode.m.engine.Control;

/**
 *
 * @author jq59bu
 */
public abstract class CliTest {

    protected static final String EMPTY_RESPONSE = "\n";
    
    ControlCli controlCli;
    SynchronousQueue<String> toTerminal;
    Thread instanceThread;

    public CliTest() {
        createInstance();
    }

    protected String filename(String sequence) {
        return controlCli.getControl().getDefaultFileName() + sequence + Control.FILE_EXTENTION;
    }

 
    protected void createInstance() {
        controlCli = ControlCli.createUnitTestInstance();
        controlCli.testCommandCompleterBarrier = new CyclicBarrier(2);
        controlCli.stdoutTestEcho = new StringWriter();
        toTerminal = new SynchronousQueue<>();
        controlCli.commandTestSupplier = (() -> {
            try {
                return toTerminal.poll(2500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.interrupted();
                e.printStackTrace(System.err);
                throw new RuntimeException(e);
            }
        });
        instanceThread = new Thread(() -> controlCli.run());
        instanceThread.setName("instanceThread");
        instanceThread.start();
    }

    protected String execute(String s) {
        writeToTerminal(s);
        return readFromTerminal();
    }

    protected void writeToTerminal(String s) {
        try {
            toTerminal.offer(s, 2500, TimeUnit.MILLISECONDS);
            try {
                controlCli.testCommandCompleterBarrier.await();
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
        String result = controlCli.stdoutTestEcho.toString();
        controlCli.stdoutTestEcho.getBuffer().setLength(0);
        return result;
    }

    protected String pwd() {
        //return Paths.get("").toAbsolutePath().toString();
        return controlCli.getControl().getProjectDirectory().toString() + File.separator;
    }

    protected boolean waitFor(BooleanSupplier stopCondition) {
        return waitFor(stopCondition, 1000);
    }

    protected boolean waitFor(BooleanSupplier stopCondition, long millisec) {
        if (millisec < 1) {
            throw new IllegalArgumentException();
        }
        long startTime = System.currentTimeMillis();
        boolean result = true;
        while (!stopCondition.getAsBoolean() && millisec > System.currentTimeMillis() - startTime) {
            result = false;
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) {
                Thread.interrupted();
            }
        }
        return result;
    }

}
