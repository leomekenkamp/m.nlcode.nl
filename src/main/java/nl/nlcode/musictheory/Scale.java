package nl.nlcode.musictheory;

/**
 *
 * @author leo
 */
public class Scale implements IntervalSequence {

    private String name;
    private int[] noteOffsetToTonic; // zero based!
    private int[] intervalsInSemitone;

    protected Scale(String name) {
        this.name = name;
    }

    protected Scale(String name, int[] intervalsInSemitone) {
        this(name);
        this.intervalsInSemitone = intervalsInSemitone.clone();
        noteOffsetToTonic = new int[intervalsInSemitone.length + 1];
        int offset = 0;
        noteOffsetToTonic[0] = 0;
        for (int i = 0; i < intervalsInSemitone.length; i++) {
            noteOffsetToTonic[i + 1] = intervalsInSemitone[i] + offset;
            offset = noteOffsetToTonic[i + 1];
        }
    }

    public String name() {
        return name;
    }

    public int toneCount() {
        return intervalsInSemitone.length;
    }

    /**
     * @return array with offsets (in semitones) to the tonic note
     */
    protected int[] noteOffsetToTonic() {
        return noteOffsetToTonic.clone();
    }

    /**
     * @return offsets (in semitones) to the tonic of the note with given {@code degree}
     */
    public int noteOffsetToTonicByDegree(int degree) {
        int octaveOffset = semitonesPerOctave() * ((degree - 1) / toneCount());
        return octaveOffset + noteOffsetToTonic[((degree - 1) % toneCount())];
    }

    public int noteOffsetToTonicByDegree(int degree, int modeNumber) {
        return noteOffsetToTonicByDegree(modeNumber + degree - 1) - noteOffsetToTonicByDegree(modeNumber);
//        int octaveOffset = semitonesPerOctave() * ((degree - modeNumber) / toneCount())
//        return octaveOffset + noteOffsetToTonic[((degree + modeNumber - 2) % toneCount())] - noteOffsetToTonic[modeNumber - 1];
    }

    public int noteOffsetToTonicByDegreeUp(int degree) {
        return noteOffsetToTonicByDegree(degree);
    }

    public int noteOffsetToTonicByDegreeDown(int degree) {
        return noteOffsetToTonicByDegree(degree);
    }

    /**
     * @return semitones from the given {@code degree} to the following degree
     */
    public int intervalUp(int degree) {
        return intervalUp(degree, 1);
    }

    /**
     * @return semitones from the given {@code degree} to the following degree
     */
    public int intervalUp(int degree, int modeNumber) {
        checkDegree(degree);
        checkModeNumber(modeNumber);
        int octaveOffset = semitonesPerOctave() * ((degree - modeNumber) / intervalsInSemitone.length);
        return octaveOffset + intervalsInSemitone[((degree + modeNumber - 2) % intervalsInSemitone.length)];
    }

    /**
     * @return semitones from the preceding degree to the given {@code degree}
     */
    public int intervalDown(int degree) {
        return intervalDown(degree, 1);
    }

    /**
     * @return semitones from the preceding degree to the given {@code degree}
     */
    public int intervalDown(int degree, int modeNumber) {
        checkDegree(degree);
        checkModeNumber(modeNumber);
        int octaveOffset = semitonesPerOctave() * ((degree - modeNumber) / intervalsInSemitone.length);
        return octaveOffset + intervalsInSemitone[((degree + modeNumber + intervalsInSemitone.length - 3) % intervalsInSemitone.length)];
    }

    public int semitonesPerOctave() {
        return 12;
    }

    protected void checkModeNumber(int modeNumber) {
        if (modeNumber < 1 || modeNumber > toneCount()) {
            throw new IllegalArgumentException("modeNumber must be in interval [1, " + toneCount() + "]");
        }
    }
}
