package ai.hual.labrador.utils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexUtils {


    public static String handelSlottedRegex(String regex) {
        return regex.replaceAll("\\{\\{", "\\\\\\{\\\\\\{");
    }

    public static boolean isSlottedRegex(String regex) {
        try {
            Pattern.compile(handelSlottedRegex(regex));
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }


}
