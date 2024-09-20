package nl.nlcode.m.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Function;
import java.util.function.Supplier;
import static nl.nlcode.m.cli.Verbosity.informative;
import static nl.nlcode.m.cli.Verbosity.minimal;
import static nl.nlcode.m.cli.Verbosity.newbie;
import nl.nlcode.m.cli.picoclijline3.PicocliCommands;
import nl.nlcode.m.cli.picoclijline3.PicocliCommands.PicocliCommandsFactory;
import nl.nlcode.m.engine.Control;
import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.Project;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Model.CommandSpec;

/**
 *
 * @author jq59bu
 */
public class ControlCli implements Runnable, Control.Ui {

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

    private void welcome() {

    }

    @Override
    public void run() {
        applicationStatus = ApplicationStatus.RUNNING;
        Attributes attributes = new Attributes();
        try (Terminal term = TerminalBuilder.builder()
                .name("m.nlcode.nl terminal")
                .attributes(attributes)
                .dumb(true) // only suppresses warning, does not force use of a dumb terminal
                .jansi(true)
                .build();) {
            //AnsiConsole.setTerminal(terminal);

            welcome();
            terminal = term;
            if (stdoutTestEcho == null) {
                stdout = terminal.writer();
            } else {
                stdout = new PrintWriter(new MultiWriter(new Writer[]{terminal.writer(), stdoutTestEcho}));
            }

            Supplier<Path> workDir = () -> Paths.get(System.getProperty("user.dir"));
            while (applicationStatus == ApplicationStatus.RUNNING) {

                CommandSpec baseSpec = BaseCommand.createCommandSpec(this);
                PicocliCommandsFactory factory = new PicocliCommandsFactory();
                CommandLine commandLine = new CommandLine(baseSpec, factory);
//                commandLine.setExecutionExceptionHandler(new IExecutionExceptionHandler() {
//                    @Override
//                    public int handleExecutionException(Exception e, CommandLine commandLine, CommandLine.ParseResult pr) throws Exception {
//                        stdout().println(commandLine.getColorScheme().errorText(e.getMessage()));
//                        return commandLine.getExitCodeExceptionMapper() != null
//                                ? commandLine.getExitCodeExceptionMapper().getExitCode(e)
//                                : commandLine.getCommandSpec().exitCodeOnExecutionException();
//                    }
//                });

                // CommandLine commandLine = new CommandLine(baseSpec);
                commandLine.registerConverter(Class.class, s -> Class.forName(MidiInOut.class.getPackageName() + "." + s));
                commandLine.registerConverter(Project.class, new ConvNameToProject(this));

                PicocliCommands picocliCommands = new PicocliCommands(commandLine);

                Parser parser = new DefaultParser();
                SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, workDir, null);
                systemRegistry.setCommandRegistries(picocliCommands);
                //systemRegistry.register("help", picocliCommands);

                LineReader lineReader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .completer(systemRegistry.completer())
                        .build();

                String prompt = getVerbosity() == minimal ? "> " : Ansi.AUTO.string("@|yellow m|@.nlcode.nl> "); // "m.nlcode.nl> "; //
                commandLoop(commandTestSupplier == null ? () -> lineReader.readLine(prompt) : commandTestSupplier, commandLine);
                if (testCommandCompleterBarrier != null) {
                    try {
                        testCommandCompleterBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        Thread.interrupted();
                        throw new RuntimeException(e);
                    }
                }
            }
            getControl().getMidiDeviceMgr().close();
            try {
                Thread.sleep(1); // need this to prevent hanging and not stopping the JVM; if we don't we do not get the command prompt back...
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            terminal = null;
            stdout = null;
        }
    }

    void commandLoop(Supplier<String> commandSupplier, CommandLine commandLine) {
        try {
            String rawCommand = commandSupplier.get();
            String[] tokens = CommandSplitter.splitCommand(rawCommand).toArray(String[]::new);
            if (tokens.length > 0) {
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

    /**
     * For use within Picocli command completion ONLY!
     */
    static ControlCli getInstance() {
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

    public Project getProjectByName(String name) {
        for (Map.Entry<Integer, Project> entry : getIdToProject().entrySet()) {
            if (entry.getValue().getPath().toString().equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Project getProjectById(int id) {
        return getIdToProject().get(id);
    }

    public Control getControl() {
        return control;
    }

    public Project getDefaultProject() {
        return idToProject.get(0);
    }

    public Project getCurrentProject() {
        return getCurrentProject(true);
    }

    public Project getCurrentProject(boolean showErrorOnNoProject) {
        Project result = getDefaultProject();
        if (result == null && showErrorOnNoProject) {
            printMessage("project.none");
        }
        return result;
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
                newId++;
            }
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
                break;
        }
    }

    ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void printMessage(String key, List<Object> params) {
        stdout().println(commandMessage(key, params));
    }

    public void printMessage(String key, Object... params) {
        stdout().println(commandMessage(key, params));
    }

    public boolean hasMessageKey(String key) {
        return COMMAND_MESSAGES.containsKey(key);
    }

    public String messageFallback(String key) {
        for (Verbosity verbosity : getVerbosity().withFallback()) {
            try {
                return COMMAND_MESSAGES.getString(key + "." + verbosity);
            } catch (MissingResourceException e) {
                // ignore
            }
        }
        return null;
    }

    public String commandMessage(String key, List<Object> params) {
        String formatString = messageFallback(key);
        if (formatString == null) {
            formatString = "???<" + key + ">??? " + params;
        }
        return String.format(formatString, params.toArray());
    }

    public String commandMessage(String key, Object... params) {
        return commandMessage(key, Arrays.asList(params));
    }

    /**
     * Expects both a {@code listPropKey} as well as a key with same name and
     * ".item" appended.
     *
     * @param listPropKey
     */
    public <I> void printList(String listPropKey, List<Object> overallParameters, Iterable<I> items, Function<I, List<Object>> perItemParameters) {
        String s = commandList(listPropKey, overallParameters, items, perItemParameters);
        if (!s.isEmpty()) {
            stdout().println(s);
        }
    }

    /**
     * Expects both a {@code listPropKey} as well as a key with same name and
     * ".item" appended.
     *
     * @param listPropKey
     */
    public <I> String commandList(String listPropKey, List<Object> overallParameters, Iterable<I> items, Function<I, List<Object>> perItemParameters) {
        StringBuilder itemsBuffer = new StringBuilder();
        if (items.iterator().hasNext()) {
            for (I item : items) {
                itemsBuffer.append(commandMessage(listPropKey + ".item", perItemParameters.apply(item)));
            }
        } else {
            String emptyKey = listPropKey + ".empty";
            if (messageFallback(emptyKey) != null) {
                return commandMessage(emptyKey, overallParameters);
            }
        }
        List<Object> combinedParameters = new ArrayList();
        combinedParameters.add(itemsBuffer.toString());
        combinedParameters.addAll(overallParameters);
        return commandMessage(listPropKey, combinedParameters);
    }

    public boolean saveAllDirtyProjects() {
        boolean result = true;
        for (Project project : idToProject.values()) {
            try {
                project.saveIfDirty();
            } catch (IOException e) {
                printMessage("save.error", project.getPath(), e.getClass().getSimpleName(), e.getMessage());
                result = false;
            }
        }
        return result;
    }

    public List<MidiInOut<?>> getMidiInOutsForTypeName(String typeName) {
        return getCurrentProject().getMidiInOutList().stream()
                .filter(midiInOut -> midiInOut.getClass().getSimpleName().equalsIgnoreCase(typeName))
                .toList();
    }

    public MidiInOutCli createMidiInOut(String midiInOutType, String name, Project project) {
        return MidiInOutCliRegistry.getInstance().create(midiInOutType, name, this, project);
    }

}
