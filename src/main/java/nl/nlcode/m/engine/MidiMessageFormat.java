package nl.nlcode.m.engine;

import java.util.ResourceBundle;
import java.util.StringJoiner;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

/**
 *
 * @author leo
 */
public class MidiMessageFormat {

    private static final String STAT_HI_PREFIX = "STAT_HI_";

    private static final String STAT_PREFIX = "STAT_";
    
    private static final String CONTROLLER_PREFIX = "CONTROLLER_";

    private final static String[] NOTE_DESC = new String[] {
        "C", "C♯/D♭", "D", "E♭/D♯", "E", "F", "F♯/G♭", "G", "A♭/G♯", "A", "B♭/A♯", "B"
    };

    public static ResourceBundle MIDI = ResourceBundle.getBundle("nl.nlcode.m.MIDI");

    public String toDec(MidiMessage msg, int maxLength) {
        StringJoiner result = new StringJoiner(" ");
        result.add(Integer.toString(msg.getStatus()));
        for (int i = 1; i < msg.getMessage().length; i++) {
            if (i >= maxLength) {
                result.add("\u2026 (+" + (msg.getMessage().length - maxLength) + ")");
                break;
            }
            result.add(Integer.toString(msg.getMessage()[i]));
        }
        return result.toString();
    }

    public String formatNote(int note) {
        if (note < 0 || note > 127) {
            throw new IllegalArgumentException("note value " + note + " out of range");
        }
        return NOTE_DESC[note % NOTE_DESC.length] + ((note / 12) - 1);
    }

    public String format(MidiMessage message) {
        if (message instanceof ShortMessage) {
            return format((ShortMessage) message);
        } else if (message instanceof SysexMessage) {
            return format((SysexMessage) message);
        } else if (message instanceof MetaMessage) {
            return format((MetaMessage) message);
        } else {
            return MIDI.getString("unknownMessageType");
        }
    }

    public String format(ShortMessage shortMsg) {
        if (shortMsg.getStatus() < 0x80) {
            return "computer says 'no'...";
        } else if (shortMsg.getStatus() >= 0xF0) {
            return formatSystemMessage(shortMsg);
        } else {
            return formatChannelMessage(shortMsg);
        }
    }
    
    public String format(SysexMessage sysexMsg) {
        return format(sysexMsg, 7);
    }
    
    public String format(SysexMessage sysexMsg, int maxBytes) {
        StringBuffer result = new StringBuffer(MIDI.getString(STAT_PREFIX + Integer.toHexString(sysexMsg.getStatus())));
        if (maxBytes >= 0) {
            StringJoiner dataString = new StringJoiner(",");
            byte[] data = sysexMsg.getData();
            for (int i = 0; i < data.length; i++) {
                if (i >= maxBytes) {
                    dataString.add("...(+" + (data.length - i) + ")");
                    break;
                }
                dataString.add(Integer.toHexString(data[i]));
            }
            result.append(dataString);
        }
        return result.toString();
    }
    
    public String format(MetaMessage shortMsg) {
        return "not implemented";
    }

    private int decode14bit(ShortMessage shortMessage) {
        return ((shortMessage.getData2() & 0x7F) << 7) | (shortMessage.getData1() & 0x7F);
    }
     
    private StringJoiner stringJoiner() {
        return new StringJoiner(MIDI.getString("stringJoiner"));
    }
    
    private String formatSystemMessage(ShortMessage shortMsg) {
        StringJoiner result = stringJoiner();
        result.add(MIDI.getString(STAT_PREFIX + Integer.toHexString(shortMsg.getStatus())));
        switch (shortMsg.getStatus()) {
            case 0xF1:
                result.add("" + shortMsg.getData1());
                break;
            case 0xF2:
                result.add(MIDI.getString("") + " " + decode14bit(shortMsg));
                break;
            case 0xF3:
                result.add("" + shortMsg.getData1());
                break;
            default:
                break;
        }
        return result.toString();
    }

    private String formatChannelMessage(ShortMessage shortMsg) {
        StringJoiner result = stringJoiner();
        int statusHi = shortMsg.getStatus() >>> 4;
        result.add(MIDI.getString(STAT_HI_PREFIX + Integer.toHexString(statusHi)));
        result.add(MIDI.getString("channel") + " " + (shortMsg.getChannel() + 1));
        if (statusHi <= 0xA) {
            result.add(MIDI.getString("note") + " " + formatNote(shortMsg.getData1()));
        } else {
            switch (statusHi) {
                case 0x8:
                case 0x9:
                    result.add(MIDI.getString("velocity") + " " + shortMsg.getData2());
                    break;
                case 0xA:
                    result.add(MIDI.getString("pressure") + " " + shortMsg.getData2());
                    break;
                case 0xB:
                    result.add(MIDI.getString(CONTROLLER_PREFIX + shortMsg.getData1()) + " " + shortMsg.getData2());
                    break;
                case 0xC:
                    result.add(MIDI.getString("program") + " " + shortMsg.getData1());
                    break;
                case 0xD:
                    result.add(MIDI.getString("pressure") + " " + shortMsg.getData1());
                    break;
                case 0xE:
                    result.add("" + decode14bit(shortMsg));
                    break;
            }
        }
        return result.toString();
    }
}
