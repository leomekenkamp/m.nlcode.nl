package nl.nlcode.m.engine;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Nested;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.CoreMatchers.is;

/**
 *
 * @author leo
 */
public class ArpeggiatorTest extends DefaultMidiInOutTest<Arpeggiator> {

    @Override
    protected Arpeggiator createInstance() {
        return new Arpeggiator();
    }

    public ArpeggiatorTest() {
    }

    @Nested
    class RecalculateNoteLength {

        @Nested
        class for_note {

            @Test
            public void should_be_equal_to_length() {
                ArpeggiatorLengthPer lengthPer = ArpeggiatorLengthPer.NOTE;
                int length = 27;
                List<Integer> receivedNotesOn = List.of(50, 55, 57);
                int currentNote = 2;
                int octaveDown = 1;
                int octaveUp = 2;
                int currentOctave = 1;
                int result;

                result = Arpeggiator.recalculateNoteLength(lengthPer, length, receivedNotesOn, currentNote, octaveDown, octaveUp, currentOctave);
                assertEquals(27, result);

                currentNote = 3;
                currentOctave = 2;
                result = Arpeggiator.recalculateNoteLength(lengthPer, length, receivedNotesOn, currentNote, octaveDown, octaveUp, currentOctave);
                assertEquals(27, result);
            }
        }

        @Nested
        class for_chord {

            @Test
            public void should_be_length_div_note_count() {
                ArpeggiatorLengthPer lengthPer = ArpeggiatorLengthPer.CHORD;
                int length = 21;
                List<Integer> receivedNotesOn = List.of(50, 55, 57);
                int currentNote = 2;
                int octaveDown = 1;
                int octaveUp = 2;
                int currentOctave = 1;
                int result;

                result = Arpeggiator.recalculateNoteLength(lengthPer, length, receivedNotesOn, currentNote, octaveDown, octaveUp, currentOctave);
                assertEquals(7, result);

                currentNote = 3;
                currentOctave = 2;
                result = Arpeggiator.recalculateNoteLength(lengthPer, length, receivedNotesOn, currentNote, octaveDown, octaveUp, currentOctave);
                assertEquals(7, result);
            }

            @Test
            public void should_handle_remainder_one() {
                ArpeggiatorLengthPer lengthPer = ArpeggiatorLengthPer.CHORD;
                List<Integer> receivedNotesOn = List.of(50, 55, 57, 62, 86);
                int length = receivedNotesOn.size() * 3 + 1;
                int octaveDown = 1;
                int octaveUp = 2;
                int currentOctave = 1;
                int result[] = new int[receivedNotesOn.size()];
                int total = 0;
                
                for (int currentNote = 0; currentNote < receivedNotesOn.size(); currentNote++) {
                    result[currentNote] = Arpeggiator.recalculateNoteLength(lengthPer, length, receivedNotesOn, currentNote, octaveDown, octaveUp, currentOctave);
                    assertThat(result[currentNote], is(greaterThanOrEqualTo(3)));
                    assertThat(result[currentNote], is(lessThanOrEqualTo(4)));
                    total += result[currentNote];
                }

                assertThat(total, is(length));
            }

            @Test
            public void should_handle_remainder_two() {
                ArpeggiatorLengthPer lengthPer = ArpeggiatorLengthPer.CHORD;
                List<Integer> receivedNotesOn = List.of(50, 55, 57, 62, 86);
                int length = receivedNotesOn.size() * 2 + 2;
                int octaveDown = 1;
                int octaveUp = 2;
                int currentOctave = 1;
                int result[] = new int[receivedNotesOn.size()];
                int total = 0;
                
                for (int currentNote = 0; currentNote < receivedNotesOn.size(); currentNote++) {
                    result[currentNote] = Arpeggiator.recalculateNoteLength(lengthPer, length, receivedNotesOn, currentNote, octaveDown, octaveUp, currentOctave);
                    assertThat(result[currentNote], is(greaterThanOrEqualTo(2)));
                    assertThat(result[currentNote], is(lessThanOrEqualTo(3)));
                    total += result[currentNote];
                }

                assertThat(total, is(length));
            }
            
            @Test
            public void should_handle_large_div_and_remainder() {
                ArpeggiatorLengthPer lengthPer = ArpeggiatorLengthPer.CHORD;
                List<Integer> receivedNotesOn = List.of(50, 55, 57, 62, 86, 88, 92, 93, 95, 98, 100);
                int length = receivedNotesOn.size() * 6 + 7;
                int octaveDown = 1;
                int octaveUp = 2;
                int currentOctave = 1;
                int result[] = new int[receivedNotesOn.size()];
                int total = 0;
                
                for (int currentNote = 0; currentNote < receivedNotesOn.size(); currentNote++) {
                    result[currentNote] = Arpeggiator.recalculateNoteLength(lengthPer, length, receivedNotesOn, currentNote, octaveDown, octaveUp, currentOctave);
                    assertThat(result[currentNote], is(greaterThanOrEqualTo(6)));
                    assertThat(result[currentNote], is(lessThanOrEqualTo(7)));
                    total += result[currentNote];
                }

                assertThat(total, is(length));
            }
        }
    }

}
