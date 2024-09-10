package nl.nlcode.m.engine;

import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

/**
 * Needed to make sure that calls to {@code open()} and {@code close()} are
 * relayed to the {@code MidiDeviceMgr}.
 * 
 * FIXME: integrate this class? Maybe too much of a hassle, since the getMidiDevice(Info) need to 
 * do a lookup first too.
 * 
 * @author jq59bu
 */
public class MidiDeviceProxy implements MidiDevice {

    private MidiDeviceMgr midiDeviceMgr;
    private MidiDevice proxied;
    
    public MidiDeviceProxy(MidiDeviceMgr midiDeviceMgr, MidiDevice proxied) {
        if (midiDeviceMgr == null || proxied == null) {
            throw new IllegalArgumentException("no nulls please");
        }
        this.midiDeviceMgr = midiDeviceMgr;
        this.proxied = proxied;
    }
    
    @Override
    public Info getDeviceInfo() {
        return proxied.getDeviceInfo();
    }

    /**
     * Intercepts the opening of this device by relaying to the {@code MidiDeviceMgr}.
     * 
     * @throws MidiUnavailableException 
     */    
    @Override
    public void open() throws MidiUnavailableException {
        midiDeviceMgr.open(this);
    }

    /**
     * Delegates the opening of this device to the proxy.
     * 
     * @throws MidiUnavailableException 
     */    
    void openInternal() throws MidiUnavailableException {
        proxied.open();
    }

    /**
     * Intercepts the closing of this device by relaying to the {@code MidiDeviceMgr}.
     */
    @Override
    public void close() {
        midiDeviceMgr.close(this);
    }

    /**
     * Delegates the actual closing of this device to the proxy.
     */    
    void closeInternal() {
        proxied.close();
    }

    @Override
    public boolean isOpen() {
        return proxied.isOpen();
    }

    @Override
    public long getMicrosecondPosition() {
        return proxied.getMicrosecondPosition();
    }

    @Override
    public int getMaxReceivers() {
        return proxied.getMaxReceivers();
    }

    @Override
    public int getMaxTransmitters() {
        return proxied.getMaxTransmitters();
    }

    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
        return proxied.getReceiver();
    }

    @Override
    public List<Receiver> getReceivers() {
        return proxied.getReceivers();
    }

    @Override
    public Transmitter getTransmitter() throws MidiUnavailableException {
        return proxied.getTransmitter();
    }

    @Override
    public List<Transmitter> getTransmitters() {
        return proxied.getTransmitters();
    }
    
}
