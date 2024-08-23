package nl.nlcode.m.cli;

import java.lang.invoke.MethodHandles;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import nl.nlcode.m.engine.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jq59bu
 */
public class CliApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static void main(String... args) {
//        System.out.println("Java version from system properties: " + System.getProperty("java.version"));
//        System.out.println("Java version from Runtime: " + Runtime.version());
//        //MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
//        MidiDevice.Info[] infos = CoreMidiDeviceProvider.getMidiDeviceInfo();
//        for (MidiDevice.Info info : infos) {
//            System.out.println(info.getName());
//        }
        
        
        Control control = new Control(false);
        control.preInitMidiSystem();
        ControlCli controlCli = ControlCli.createInstance(Control.getInstance());
        controlCli.run();
    }

}
