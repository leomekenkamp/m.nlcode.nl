package nl.nlcode.m.cli;

import java.util.StringJoiner;

/**
 * TODO: overrides 'parse' and does not use 'matches'. Refactor?
 *
 * @author jq59bu
 */
public class EnumValue<E extends Enum<E>> extends Token {

    public interface Parent<E> {

        void setValue(E value);

    }
    
    private Class<E> enumType;

    public EnumValue(Token parent, Class<E> enumType) {
        super("enumToken", parent);
      //  super("todo" /*allValuesDescription(enumType),*/ parent);
        this.enumType = enumType;
    }

    private static <E extends Enum<E>> String allValuesDescription(Class<E> type) {
        StringJoiner result = new StringJoiner("|");
        E[] types = type.getEnumConstants();

        for (E enumConst : type.getEnumConstants()) {
            result.add(enumConst.name());
        }
        return result.toString();
    }

    @Override
    protected boolean matches(String token) {
        try {
//            getParent().setValue(Enum.valueOf(enumType, token));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    protected String getHelpId() {
        return super.getHelpId() + "." + getMatch();
    }

}
