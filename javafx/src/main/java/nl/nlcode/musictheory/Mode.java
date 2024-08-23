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
        if (modeNumber < 1 || modeNumber > degrees()) {
            throw new IllegalArgumentException("cannot have more modes than degrees in the parent scale (" + degrees() + ")");
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
    public int degrees() {
        return basedOn.degrees();
    }

    @Override
    public int semitonesFromTonicByDegree(int degree) {
        return basedOn.semitonesFromTonicByDegree(degree, modeNumber);
    }

    @Override
    public int intervalUpFrom(int degree) {
        return basedOn.intervalUpFrom(degree, modeNumber);
    }

    @Override
    public int intervalDownFrom(int degree) {
        return basedOn.intervalDownFrom(degree, modeNumber);
    }

}
