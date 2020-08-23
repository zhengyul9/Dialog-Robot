package ai.hual.labrador.nlu.pinyin;

/**
 * Pinyin functionality is provided by classes who
 * implements this interface.
 *
 * @author Yuqi
 * @since 1.8
 */
public interface Pinyin {

    /**
     * Convert input string in Chinese character
     * to a string of pinyin split with space.
     *
     * @param input string of words in Chinese
     * @return string of pinyin
     */
    String getPinyin(String input);
}
