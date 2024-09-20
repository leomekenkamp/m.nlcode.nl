package nl.nlcode.m.cli;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.IModelTransformer;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;

/**
 *
 * @author jq59bu
 */
@Command(name = "new", description = "create new MidiInOut instance", modelTransformer = NewCommand.ModelTransformer.class)
public class NewCommand extends WithProjectCommand<BaseCommand> {

    @Command
    public static class MidiInOutTypeCommand extends ChildCommand<NewCommand> implements Runnable {

        @Parameters(paramLabel = "<name>", description = "name for the new instance", arity = "0..1")
        private String name;

        private String midiInOutType;

        private MidiInOutTypeCommand(String midiInOutType) {
            this.midiInOutType = midiInOutType;
        }

        @Override
        public void run() {
            getParent().create(midiInOutType, name);
        }

    }

    public static class ModelTransformer implements IModelTransformer {

        public CommandSpec transform(CommandSpec commandSpec) {
            for (String midiInOutType : MidiInOutCliRegistry.getInstance().midiInOutTypes()) {
                commandSpec.addSubcommand(midiInOutType, CommandSpec.forAnnotatedObject(new MidiInOutTypeCommand(midiInOutType)));
            }
            return commandSpec;
        }
    }

    protected static class MidiInOutTypes implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            List<String> result = new ArrayList<>();
            for (String typeName : MidiInOutCliRegistry.getInstance().midiInOutTypes()) {
                result.add("\"" + typeName + "\"");
            }
            return result.iterator();
        }
    }

    public void create(String midiInOutType, String name) {
        withSelectedProjects(project -> {
            MidiInOutCli midiInOutCli = getControlCli().createMidiInOut(midiInOutType, name, project);
            if (name != null) {
                midiInOutCli.getMidiInOut().setName(name);
            }
        });
    }

}
