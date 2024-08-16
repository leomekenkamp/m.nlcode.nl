package nl.nlcode.musictheory;

/**
 *
 * @author leo
 */
public interface IntervalSequence {

    int degrees();

    /**
     * @return offsets (in semitones) to the tonic of the note with given {@code degree}
     */
    int semitonesFromTonicByDegree(int degree);

    /**
     * @return semitones from the given {@code degree} to the following degree
     */
    int intervalUpFrom(int degree);

    /**
     * @return semitones from the given {@code degree} to the preceding degree
     */
    int intervalDownFrom(int degree);

    default int semitonesPerOctave() {
        return 12; // keep it simple, yet we may want to expand on this in the future
    }

    String name();

    default boolean hasSameIntervalsAs(IntervalSequence other) {
        if (degrees() != other.degrees()) {
            return false;
        }
        for (int degree = 1; degree <= degrees(); degree++) {
            if (semitonesFromTonicByDegree(degree) != other.semitonesFromTonicByDegree(degree)) {
                return false;
            }
        }
        return true;
    }

    default void checkDegree(int degree) {
        if (degree < 0 || degree > degrees()) {
            throw new IllegalArgumentException("degree out of range");
        }
    }

    // *****
    // ***** Music theory terminology
    // *****
    /**
     * The method gives a faster way of computing the equivalent of
     * <pre>
     * {@code
     *     for (IntervalSequence candidate : ScalesAndModes.DIATONIC_SCALES) {
     *         if (candidate.hasSameIntervalsAs(intervalSequence)) {
     *             return true;
     *         }
     *     }
     * }
     * </pre>
     *
     * @param intervalSequence
     * @return
     */
    default boolean isDiatonic() {
        if (!isHeptatonic() || !isChromatic()) {
            return false;
        }
        int firstHalfOnDegree = 0;
        int secondHalfOnDegree = 0;
        for (int degree = 1; degree <= degrees(); degree++) {
            switch (intervalUpFrom(degree)) {
                case 1:
                    if (firstHalfOnDegree == 0) {
                        firstHalfOnDegree = degree;
                    } else if (secondHalfOnDegree == 0) {
                        secondHalfOnDegree = degree;
                    } else {
                        return false; // Diatonic scales have exactly two half intervals
                    }
                case 2:
                    break;
                default:
                    return false;
            }
        }
        if (secondHalfOnDegree == 0) {
            return false;
        } else {
            int distanceBetweenHalfs = secondHalfOnDegree - firstHalfOnDegree;
            // Halfs should be as far apart as possible. So e.g. in C Major (Ionian), the distance
            // between E(next degree: F, one semitone up) and the next higher B (next degree: C, one
            // semitone up) is 3 degrees. In F Lydian the distance between B and the and the higher
            // E is 4 degrees.
            return distanceBetweenHalfs == 3 || distanceBetweenHalfs == 4;
        }
    }

    default boolean isChromatic() {
        return semitonesPerOctave() == 12;
    }

    default boolean isHeptatonic() {
        return degrees() == 7;
    }

    default boolean isMajor() {
        return semitonesFromTonicByDegree(3) == Interval.MAJOR_THIRD.semitones();
    }

    default boolean isMinor() {
        return semitonesFromTonicByDegree(3) == Interval.MINOR_THIRD.semitones();
    }
}
