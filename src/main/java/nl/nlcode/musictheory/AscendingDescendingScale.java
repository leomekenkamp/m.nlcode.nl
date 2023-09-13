package nl.nlcode.musictheory;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author leo
 */
public class AscendingDescendingScale implements IntervalSequence {

    private String name;
    private Scale ascendingScale;
    private Scale descendingScale;
    private int toneCount;

    AscendingDescendingScale(String name, Scale ascendingScale, Scale descendingScale) {
        this.name = name;
        this.ascendingScale = ascendingScale;
        this.descendingScale = descendingScale;
        Set<Integer> offsets = new HashSet<>();
        for (int degree = 1; degree <= ascendingScale.toneCount(); degree++) {
            offsets.add(noteOffsetToTonicByDegreeUp(degree));
            offsets.add(noteOffsetToTonicByDegreeDown(degree));
        }
        toneCount = offsets.size();
    }

    @Override
    public int intervalUp(int degree) {
        return ascendingScale.intervalUp(degree);
    }

    @Override
    public int intervalDown(int degree) {
        return descendingScale.intervalUp(degree);
    }

    @Override
    public int noteOffsetToTonicByDegreeUp(int degree) {
        return ascendingScale.noteOffsetToTonicByDegree(degree);
    }

    @Override
    public int noteOffsetToTonicByDegreeDown(int degree) {
        return descendingScale.noteOffsetToTonicByDegree(degree);
    }

    @Override
    public int toneCount() {
        return toneCount;
    }

    @Override
    public int noteOffsetToTonicByDegree(int degree) {
        return noteOffsetToTonicByDegreeUp(degree);
    }

    @Override
    public String name() {
        return name;
    }

}
