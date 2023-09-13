package nl.nlcode.musictheory;

/**
 * A scale that has a set tonic note.
 *
 * @author leo
 */
public class NoteScale implements IntervalSequence {

    private String name;
    private Note tonic;
    private IntervalSequence basedOn;

    public NoteScale(String name, Note tonic, IntervalSequence basedOn) {
        this.name = name;
        this.tonic = tonic;
        this.basedOn = basedOn;
    }

    @Override
    public int toneCount() {
        return basedOn.toneCount();
    }

    @Override
    public int noteOffsetToTonicByDegree(int degree) {
        return basedOn.noteOffsetToTonicByDegree(degree);
    }

    @Override
    public int noteOffsetToTonicByDegreeUp(int degree) {
        return basedOn.noteOffsetToTonicByDegreeUp(degree);
    }

    @Override
    public int noteOffsetToTonicByDegreeDown(int degree) {
        return basedOn.noteOffsetToTonicByDegreeDown(degree);
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
        return name;
    }
}
