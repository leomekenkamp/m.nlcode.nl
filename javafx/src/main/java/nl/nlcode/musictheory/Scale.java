package nl.nlcode.musictheory;

/**
 *
 * @author leo
 */
public class Scale implements IntervalSequence {

    public enum Direction {
        UNIDIRECTIONAL,
        ASCENDING,
        DESCENDING;
    }
    
    private String name;

    /**
     * Index 0 holds the interval in semitones from the tonic to the second note
     * of the scale.Index n holds the interval in semitones to the n+2 note of
     * the scale.
     */
    private int[] intervals;

    /**
     * Index n holds the number of semitones between the tonic and note n of the
     * scale. Ergo, distanceToTonic[0] = 0 by definition.
     */
    private int[] distanceToTonic; // zero based!

    private Direction direction;

    protected Scale(String name) {
        this.name = name;
    }

    protected Scale(String name, int[] intervalsInSemitones, Direction direction) {
        this(name);
        this.intervals = intervalsInSemitones.clone();
        distanceToTonic = new int[intervals.length + 1];
        int offset = 0;
        distanceToTonic[0] = 0;
        for (int i = 0; i < intervals.length; i++) {
            distanceToTonic[i + 1] = intervals[i] + offset;
            offset = distanceToTonic[i + 1];
        }
        this.direction = direction;
    }

    protected Scale(String name, int[] intervalsInSemitones) {
        this(name, intervalsInSemitones, Direction.UNIDIRECTIONAL);
    }

    public Direction getDirection() {
        return direction;
    }

    public String name() {
        return name;
    }

    /**
     * @return the number of distinct chromatic notes in this scale.
     */
    public int degrees() {
        return intervals.length;
    }

    /**
     * @return array with offsets (in semitones) to the tonic note
     */
    protected int[] distanceToTonic() {
        return distanceToTonic.clone();
    }

    /**
     * @return offsets (in semitones) to the tonic of the note with given
     * {@code degree}
     */
    @Override
    public int semitonesFromTonicByDegree(int degree) {
        int octaveOffset = semitonesPerOctave() * (degree / degrees());
        return octaveOffset + distanceToTonic[(degree % degrees())];
    }

    public int semitonesFromTonicByDegree(int degree, int modeNumber) {
        return Scale.this.semitonesFromTonicByDegree(modeNumber - 1 + degree) - Scale.this.semitonesFromTonicByDegree(modeNumber - 1);
//        int octaveOffset = semitonesPerOctave() * ((degree - modeNumber) / toneCount())
//        return octaveOffset + noteOffsetToTonic[((degree + modeNumber - 2) % toneCount())] - noteOffsetToTonic[modeNumber - 1];
    }

    /**
     * @return semitones from the given {@code degree} to the following degree
     */
    public int intervalUpFrom(int degree) {
        return intervalUpFrom(degree, 1);
    }

    /**
     * @return semitones from the given {@code degree} to the following degree
     */
    public int intervalUpFrom(int degree, int modeNumber) {
        checkDegree(degree);
        checkModeNumber(modeNumber);
        int octaveOffset = semitonesPerOctave() * ((degree - modeNumber) / intervals.length);
        return octaveOffset + intervals[((degree + modeNumber - 2) % intervals.length)];
    }

    /**
     * @return semitones from the preceding degree to the given {@code degree}
     */
    public int intervalDownFrom(int degree) {
        return intervalDownFrom(degree, 1);
    }

    /**
     * @return semitones from the preceding degree to the given {@code degree}
     */
    public int intervalDownFrom(int degree, int modeNumber) {
        checkDegree(degree);
        checkModeNumber(modeNumber);
        int octaveOffset = semitonesPerOctave() * ((degree - modeNumber) / intervals.length);
        return octaveOffset + intervals[((degree + modeNumber + intervals.length - 3) % intervals.length)];
    }

    public int semitonesPerOctave() {
        return 12;
    }

    protected void checkModeNumber(int modeNumber) {
        if (modeNumber < 1 || modeNumber > degrees()) {
            throw new IllegalArgumentException("modeNumber must be in interval [1, " + degrees() + "]");
        }
    }
}
