package nl.nlcode.m.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;

/**
 *
 * @author leo
 */
public class DynamicNoteNameStringConverter extends StringConverter<Integer> {

    private SimpleObjectProperty<NoteNamingConvention> noteNamingConvention;

    public DynamicNoteNameStringConverter() {
        noteNamingConvention = new SimpleObjectProperty<>();
        noteNamingConvention.setValue(NoteNamingConvention.ENGLISH_SHORT);
    }

    public SimpleObjectProperty<NoteNamingConvention> noteNamingConvention() {
        return noteNamingConvention;
    }

    public NoteNamingConvention getNoteNamingConvention() {
        return noteNamingConvention.get();
    }

    public void setNoteNamingConvention(NoteNamingConvention noteNamingConvention) {
        if (noteNamingConvention == null) {
            throw new IllegalArgumentException();
        }
        this.noteNamingConvention.set(noteNamingConvention);
    }

    @Override
    public String toString(Integer t) {
        return noteNamingConvention.get().getIntegerStringConverter().toString(t);
    }

    @Override
    public Integer fromString(String string) {
        return noteNamingConvention.get().getIntegerStringConverter().fromString(string);
    }

}
