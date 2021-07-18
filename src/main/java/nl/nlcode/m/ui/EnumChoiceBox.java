package nl.nlcode.m.ui;

import javafx.beans.NamedArg;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;
import nl.nlcode.m.ui.App;

/**
 *
 * @author leo
 */
public class EnumChoiceBox<E extends Enum<E>> extends ChoiceBox<E> {

    public static class DefaultConverter<E extends Enum<E>> extends StringConverter<E> {

        @Override
        public String toString(E t) {
            return App.MESSAGES.getString(t.getClass().getName() + "." + t.name());
        }

        @Override
        public E fromString(String string) {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    
    public EnumChoiceBox(@NamedArg("enumClass") String enumClass) throws Exception {
        Class<E> e = (Class<E>) Class.forName(enumClass);
        getItems().setAll(e.getEnumConstants());
        setConverter(new DefaultConverter());
    }
    
}