package nl.nlcode.musictheory;

import java.util.ArrayList;
import java.util.List;
import nl.nlcode.musictheory.ChromaticNote.Accidental;

/**
 * A scale that has a set tonic note.
 *
 * @author leo
 */
public class PitchedScale implements IntervalSequence {

    private ChromaticNote tonic;
    private IntervalSequence basedOn;

    public PitchedScale(ChromaticNote tonic, IntervalSequence basedOn) {
        this.tonic = tonic;
        this.basedOn = basedOn;
    }

    @Override
    public int degrees() {
        return basedOn.degrees();
    }

    @Override
    public int semitonesFromTonicByDegree(int degree) {
        return basedOn.semitonesFromTonicByDegree(degree);
    }
            
    @Override
    public int intervalUpFrom(int degree) {
        return basedOn.intervalUpFrom(degree);
    }

    @Override
    public int intervalDownFrom(int degree) {
        return basedOn.intervalDownFrom(degree);
    }

    @Override
    public String name() {
        return tonic.toString() + " " + basedOn.name();
    }
    
    public SpnNote createSpnNote(int degree, int tonicOctave) {
        if (this.isDiatonic()) {
            int semitones = basedOn.semitonesFromTonicByDegree(degree);
            ChromaticNote note = tonic.getNatural().withSemitones(semitones, tonic.getAccidental());
            return new SpnNote(note, tonicOctave);
        }
        throw new UnsupportedOperationException();
    }
    
    public List<SpnNote> createOctaveNotesUp(int octave) {
        List<SpnNote> result = new ArrayList<>();
        for (int i = 0; i < basedOn.degrees(); i++) {
            result.add(createSpnNote(i, octave));
        }
        return result;
    }
    
    public List<SpnNote> createOctaveNotesDown(int octave) {
        List<SpnNote> result = new ArrayList<>();
        for (int i = 0; i < -basedOn.degrees(); i--) {
            result.add(createSpnNote(i, octave));
        }
        return result;
    }
    
    public List<SpnNote> createOctaveNotesUpAndDown(int octave) {
        List<SpnNote> result = new ArrayList<>();
        result.addAll(createOctaveNotesUp(octave));
        result.addAll(createOctaveNotesDown(octave + 1));
        return result;
    }
}
