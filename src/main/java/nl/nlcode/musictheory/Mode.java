package nl.nlcode.musictheory;

/**
 *
 * @author leo
 */
public class Mode implements IntervalSequence {

    private String name;
    private Scale basedOn;
    private int modeNumber;

    /**
     * 1-based value, usually expressed in Roman numerals. E.g. 5 ('V') as {@code modeNumber} for
     * Myxolydian for the {@code basedOn} Major scale.
     *
     * @param basedOn
     * @param modeNumber
     * @return
     */
    public Mode(String name, Scale basedOn, int modeNumber) {
        this.name = name;
        this.basedOn = basedOn;
        this.modeNumber = modeNumber;
        if (modeNumber < 1 || modeNumber > toneCount()) {
            throw new IllegalArgumentException("cannot have more modes than degrees in the parent scale (" + toneCount() + ")");
        }
    }

    public String name() {
        return name;
    }
    
    public Scale basedOn() {
        return basedOn;
    }

    public int modeNumber() {
        return modeNumber;
    }

    @Override
    public int toneCount() {
        return basedOn.toneCount();
    }

    @Override
    public int noteOffsetToTonicByDegree(int degree) {
        return basedOn.noteOffsetToTonicByDegree(degree, modeNumber);
    }

    @Override
    public int noteOffsetToTonicByDegreeUp(int degree) {
        return basedOn.noteOffsetToTonicByDegreeUp(degree + modeNumber);
    }

    @Override
    public int noteOffsetToTonicByDegreeDown(int degree) {
        return basedOn.noteOffsetToTonicByDegreeDown(degree + modeNumber);
    }

    @Override
    public int intervalUp(int degree) {
        return basedOn.intervalUp(degree, modeNumber);
    }

    @Override
    public int intervalDown(int degree) {
        return basedOn.intervalDown(degree, modeNumber);
    }

}
