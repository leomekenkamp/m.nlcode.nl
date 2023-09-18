package nl.nlcode.musictheory;

/**
 * A scale that has a set tonic note.
 *
 * @author leo
 */
public class NoteScale implements IntervalSequence {

    private ChromaticNote tonic;
    private IntervalSequence basedOn;

    public NoteScale(ChromaticNote tonic, IntervalSequence basedOn) {
        this.tonic = tonic;
        this.basedOn = basedOn;
    }

    @Override
    public int toneCount() {
        return basedOn.toneCount();
    }

    @Override
    public int semitonesFromTonicByDegree(int degree) {
        return basedOn.semitonesFromTonicByDegree(degree);
    }

    @Override
    public int semitonesFromTonicByDegreeUp(int degree) {
        return basedOn.semitonesFromTonicByDegreeUp(degree);
    }

    @Override
    public int semitonesFromTonicByDegreeDown(int degree) {
        return basedOn.semitonesFromTonicByDegreeDown(degree);
    }

    @Override
    public int intervalUp(int degree) {
        return basedOn.intervalUp(degree);
    }

    @Override
    public int intervalDown(int degree) {
        return basedOn.intervalDown(degree);
    }

    @Override
    public String name() {
        return tonic.toString() + " " + basedOn.name();
    }
    
    public PitchedNote createPitchedNote(int degree, int tonicOctave) {
        int semis = tonic.getSemitonesFromC() + basedOn.semitonesFromTonicByDegree(degree);
        int pitchedOctave = tonicOctave + semis / PitchedNote.SEMITONES_PER_OCTAVE;
        ChromaticNote resultChromatic;
        if (tonic.getAccidental() == ChromaticNote.Accidental.FLAT) {
            resultChromatic = ChromaticNote.fromSemitoneFromCPreferFlat(semis % PitchedNote.SEMITONES_PER_OCTAVE);
        } else {
            resultChromatic = ChromaticNote.fromSemitoneFromCPreferSharp(semis % PitchedNote.SEMITONES_PER_OCTAVE);
        }
        return new PitchedNote(resultChromatic, tonicOctave);
    }
}
