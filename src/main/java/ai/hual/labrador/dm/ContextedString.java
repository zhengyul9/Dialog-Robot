package ai.hual.labrador.dm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ai.hual.labrador.utils.StringUtils.replaceSubstring;

public class ContextedString {

    /**
     * String to be rendered.
     */
    private String str;

    public ContextedString(String input) {
        this.str = input;
    }

    public ContextedString() {
    }

    /**
     * Simple render to convert slot name to content in context.
     *
     * @param context context
     * @return rendered string
     */
    public String render(Context context) {
        if (str == null) {
            return null;
        }
        String renderedStr = this.str;
        Map<String, Object> slots = context.getSlots();
        Pattern pattern = Pattern.compile("\\{\\{.*?}}");
        Matcher matcher = pattern.matcher(renderedStr);

        int bias = 0;
        // replace slot placeholder to content in context.
        while (matcher.find()) {
            int start = matcher.start() + bias;
            int end = matcher.end() + bias;
            assert end - start > 4; // slot name is not empty
            String slotName = renderedStr.substring(start + 2, end - 2);    // removed "{{" and "}}"
            String contentStr = null;
            if (slots.containsKey(slotName)) {
                contentStr = Optional.ofNullable(slots.get(slotName)).map(Object::toString).orElse(null);
            } else if (slotName.equals("sys.date")) {
                contentStr = LocalDate.now().toString();
            } else if (slotName.equals("sys.time")) {
                contentStr = LocalTime.now().toString();
            }

            if (contentStr != null) {
                int lengthDiff = contentStr.length() - renderedStr.length();
                renderedStr = replaceSubstring(renderedStr, start, end, contentStr);
                bias += lengthDiff;
            } else
                return null;

        }
        return renderedStr;
    }

    /**
     * Render contextedString to list of string.
     *
     * @param context    context
     * @param splitRegex regex used to split string
     * @return splitted string
     */
    public List<String> renderStringToList(Context context, String splitRegex) {
        String renderedString = render(context);
        return Arrays.asList(renderedString.split(splitRegex));
    }

    /**
     * Render contextedString to list of string, where the original string is represented as list.
     *
     * @param context context
     * @return splitted string
     */
    public List<String> renderToList(Context context) {
        // TODO input format should be checked
        String rendered = render(context);
        if (rendered.isEmpty() || "[]".equals(rendered)) {
            return Collections.emptyList();
        }
        String renderedString = rendered.replaceAll("\"", "");    // "[a,b,c]"
        String subString = renderedString.substring(1, renderedString.length() - 1);    // "a,b,c"
        return Arrays.asList(subString.split(","));  // {"a", "b", "c"}
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public String toString() {
        return getStr();
    }
}
