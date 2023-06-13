package nl.nlcode.m.ui;

import javafx.util.converter.IntegerStringConverter;

/**
 *
 * @author leo
 */
public enum NoteNamingConvention {
    
    ENGLISH_SHORT(new EnglishShortNoteStringConverter()),
    ENGLISH_LONG(new EnglishLongNoteStringConverter()),
    GERMAN_SHORT(new GermanShortNoteStringConverter()),
    ROMANCE_SHORT(new RomanceShortNoteStringConverter()),
    ;
    
    private IntegerStringConverter integerStringConverter;

    private NoteNamingConvention(IntegerStringConverter integerStringConverter) {
        this.integerStringConverter = integerStringConverter;
    }
    
    public IntegerStringConverter getIntegerStringConverter() {
        return integerStringConverter;
    }
}
