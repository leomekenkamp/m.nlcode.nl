package nl.nlcode.m.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Supplier;
import static nl.nlcode.m.cli.Verbosity.informative;
import static nl.nlcode.m.cli.Verbosity.minimal;
import static nl.nlcode.m.cli.Verbosity.newbie;
import nl.nlcode.m.engine.Control;
import nl.nlcode.m.engine.Project;
import org.jline.builtins.Completers.TreeCompleter;
import static org.jline.builtins.Completers.TreeCompleter.node;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jq59bu
 */
public class ControlCli implements Runnable, Control.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ControlCli instance;

    private final Control control;

    private ApplicationStatus applicationStatus;

    private Terminal terminal;

    private Verbosity verbosity = Verbosity.newbie;

    private SortedMap<Integer, Project> idToProject = new TreeMap<>();

    // to be set/get by unit test only
    StringWriter stdoutTestEcho;

    // to be set/get by unit test only
    Supplier<String> commandTestSupplier;

    // to be set/get by unit test only
    CyclicBarrier testCommandCompleterBarrier;

    private PrintWriter stdout;

    private ControlCli(Control control) {
        applicationStatus = ApplicationStatus.STARTUP;
        this.control = control;
    }

    private Completer createCompleter(RootCommand rootCommand) {
        TreeCompleter result = new TreeCompleter(
                node("exit",
                        node("--force")),
                node("help",
                        node("Option1",
                                node("Param1", "Param2")),
                        node("Option2"),
                        node("Option3")));
        return result;
    }

    @Override
    public void run() {
        applicationStatus = ApplicationStatus.RUNNING;
        try (Terminal term = TerminalBuilder.builder()
                .name("m.nlcode.nl terminal")
                .build();) {
            terminal = term;
            if (stdoutTestEcho == null) {
                stdout = terminal.writer();
            } else {
                stdout = new PrintWriter(new MultiWriter(new Writer[]{terminal.writer(), stdoutTestEcho}));
            }
            RootCommand rootCommand = RootCommand.createRootCommand();
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(createCompleter(rootCommand))
                    .build();
            while (applicationStatus == ApplicationStatus.RUNNING) {
                String prompt = "m.nlcode.nl> ";
                commandLoop(commandTestSupplier == null ? () -> lineReader.readLine(prompt) : commandTestSupplier);
                if (testCommandCompleterBarrier != null) {
                    try {
                        testCommandCompleterBarrier.await();
                    } catch (InterruptedException |BrokenBarrierException e) {
                        Thread.interrupted();
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            terminal = null;
            stdout = null;
        }
    }

    void commandLoop(Supplier<String> commandSupplier) {
        RootCommand rootCommand = RootCommand.createRootCommand();
        try {
            String rawCommand = commandSupplier.get();
            String[] tokens = CommandSplitter.splitCommand(rawCommand).toArray(String[]::new);
            if (tokens.length > 0) {
                rootCommand.process(tokens, this);
            }
        } catch (Token.TokenException e) {
            stdout().println(e.getMessage());
        } catch (UserInterruptException | EndOfFileException e) {
            e.printStackTrace(stdout());
        } catch (RuntimeException e) {
            e.printStackTrace(stdout());
        }
    }

    public PrintWriter stdout() {
        return stdout;
    }

    public void exit(boolean force) {
        if (force || control.getProjectCount() == 0) {
            applicationStatus = ApplicationStatus.EXIT;
        }
    }

    public static ControlCli createMainInstance(Control control) {
        if (instance != null) {
            throw new IllegalStateException("cannot have two main ControlCli instances...");
        }
        instance = createInstance(control);
        return instance;
    }

    public static ControlCli createInstance(Control control) {
        instance = new ControlCli(control);
        control.addUi(instance);
        return instance;
    }

    public static ControlCli createUnitTestInstance() {
        Control control = new Control(true);
        instance = new ControlCli(control);
        control.addUi(instance);
        return instance;
    }

    public Verbosity getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(Verbosity verbosity) {
        this.verbosity = verbosity;
    }

    public SortedMap<Integer, Project> getIdToProject() {
        return new TreeMap(idToProject);
    }

    public Control getControl() {
        return control;
    }

    public Project getDefaultProject() {
        return idToProject.get(0);
    }

    public synchronized void renumProjects() {
        SortedSet<Integer> oldIds = new TreeSet(idToProject.keySet());
        int newId = 0;
        for (int oldId : oldIds) {
            idToProject.put(newId, idToProject.remove(oldId));
            newId++;
        }
    }

    @Override
    public synchronized void added(Project project) {
        int i;
        for (i = 0; idToProject.containsKey(i); i++);
        idToProject.put(i, project);
        switch (getVerbosity()) {
            case minimal:
                stdout().println("+ " + project.getPath());
                break;
            case informative:
                stdout().println("A project has been opened: <" + project.getPath() + ">.");
                break;
            case newbie:
                stdout().println("A project has been opened: <" + project.getPath() + ">.");
                stdout().println("You will receive opened and closed messages also for projects not opened/closed from this command prompt.");
                break;
        }
    }

    @Override
    public synchronized void removed(Project project) {
        idToProject.values().remove(project);
        switch (getVerbosity()) {
            case minimal:
                stdout().println("- " + project.getPath());
                break;
            case informative:
                stdout().println("A project has been closed: <" + project.getPath() + ">.");
                break;
            case newbie:
                stdout().println("A project has been closed: <" + project.getPath() + ">.");
                stdout().println("You will receive opened and closed messages also for projects not opened/closed from this command prompt.");
                break;
        }
    }

    ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }
}
