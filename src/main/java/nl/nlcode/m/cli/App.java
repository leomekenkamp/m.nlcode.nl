package nl.nlcode.m.cli;

import java.lang.invoke.MethodHandles;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;

public class App {
    
        private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        new App().run();
    }
    
    public App(){
    }
    
    public void run() {
        System.out.println("Java version from system properties: " + System.getProperty("java.version"));
        System.out.println("Java version from Runtime: " + Runtime.version());
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        //MidiDevice.Info[] infos = CoreMidiDeviceProvider.getMidiDeviceInfo();
        
        for (MidiDevice.Info info : infos) {
            System.out.println(info.getName());
        }
    }
}
