package ai.hual.labrador.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * test {@link StringUtils}
 * Created by Dai Wentao on 2017/7/12.
 */
public class StringUtilsTest {

    @Test
    void escapeRegexTest() {
        String input = "ab***c{d}and{{tts}}";
        input = StringUtils.escapeRegex(input);
        assertEquals("ab\\*\\*\\*c\\{d}and\\{\\{tts}}", input);
    }

    @Test
    void testBinaryPermutation3bits() {
        List<String> result = StringUtils.getBinaries(10);
        assertEquals(1024, result.size());
    }

    @Test
    void testBinaryPermutation2bits() {
        List<String> result = StringUtils.getBinaries(2);
        assertEquals(4, result.size());
    }

    @Test
    void testBinaryPermutation1bit() {
        List<String> result = StringUtils.getBinaries(1);
        assertEquals(2, result.size());
    }

    @Test
    void testAnnotate() {
        assertEquals("axxde",
                StringUtils.replaceSubstring("abcde", 1, 3, "xx"));
        assertThrows(StringIndexOutOfBoundsException.class,
                () -> StringUtils.replaceSubstring("abcde", 1, 8, "xx"));
    }
}
