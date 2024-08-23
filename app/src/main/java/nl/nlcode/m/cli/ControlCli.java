package nl.nlcode.m.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
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
import nl.nlcode.m.engine.Lights;
import nl.nlcode.m.engine.MidiDeviceLink;
import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.Project;
import org.jline.jansi.AnsiConsole;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import static org.jline.jansi.Ansi.*;
import static org.jline.jansi.Ansi.Color.*;
/**
 *
 * @author jq59bu
 */
public class ControlCli implements Runnable, Control.Ui {

    static {
        MidiInOutCliRegistry.register("Lights", (controlCli) -> new LightsCli(new Lights(), controlCli));
        MidiInOutCliRegistry.register("MidiDeviceLink", (controlCli) -> new MidiDeviceLinkCli(new MidiDeviceLink(), controlCli));
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String COMMAND_MESSAGES_BUNDLE = "nl.nlcode.m.cli.CommandMessages";

    private static ResourceBundle COMMAND_MESSAGES = ResourceBundle.getBundle(COMMAND_MESSAGES_BUNDLE);

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

    @Override
    public void run() {
        applicationStatus = ApplicationStatus.RUNNING;
        System.setProperty("org.jline.terminal.dumb", "true");
        try (Terminal term = TerminalBuilder.builder()
                .name("m.nlcode.nl terminal")
                .build();) {
            terminal = term;
            if (stdoutTestEcho == null) {
                stdout = terminal.writer();
            } else {
                stdout = new PrintWriter(new MultiWriter(new Writer[]{terminal.writer(), stdoutTestEcho}));
            }
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            while (applicationStatus == ApplicationStatus.RUNNING) {
                String prompt = getVerbosity() == minimal ? "> " : "m.nlcode.nl> ";
                commandLoop(commandTestSupplier == null ? () -> lineReader.readLine(prompt) : commandTestSupplier);
                if (testCommandCompleterBarrier != null) {
                    try {
                        testCommandCompleterBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
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
        try {
            String rawCommand = commandSupplier.get();
            String[] tokens = CommandSplitter.splitCommand(rawCommand).toArray(String[]::new);
            if (tokens.length > 0) {
                CommandLine commandLine = new CommandLine(new BaseCommand(this));
                commandLine.registerConverter(Class.class, s -> Class.forName(MidiInOut.class.getPackageName() + "." + s));
                commandLine.registerConverter(MidiInOut.class, s -> getDefaultProject().getMidiInOutLookup().get(s));
                commandLine.registerConverter(Project.class, new ConvNameToProject(this));
                commandLine.execute(tokens);
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

    public Project getCurrentProject() {
        return getDefaultProject();
    }

    public synchronized void renumProjects(Project newDefault) {
        SortedMap<Integer, Project> idToProjectCopy = new TreeMap<>(idToProject);
        idToProject.clear();
        int newId = newDefault == null ? 0 : 1;
        for (Map.Entry<Integer, Project> entry : idToProjectCopy.entrySet()) {
            if (entry.getValue() == newDefault) {
                idToProject.put(0, entry.getValue());    
            } else {
                idToProject.put(newId, entry.getValue());
            }
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

    public void commandOutput(String key, Object... params) {
        stdout().println(commandMessage(key, params));
    }

    public String commandMessage(String key, Object... params) {
        String formatString = null;
        for (Verbosity verbosity : getVerbosity().withFallback()) {
            try {
                formatString = COMMAND_MESSAGES.getString(key + "." + verbosity);
                break;
            } catch (MissingResourceException e) {
                // ignore
            }
        }
        if (formatString == null) {
            formatString = "???<" + key + ">??? ";
        }
        return String.format(formatString, params);
    }
}
