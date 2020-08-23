package ai.hual.labrador.nlu.trie;

import java.util.List;

/**
 * Trie interface.
 */
public interface Trie {

    /**
     * Insert a word(in string) into trie.
     *
     * @param word word in string
     */
    void insert(String word, Object content);

    /**
     * Search from a string input. Return the last found leaf
     * if exist.
     *
     * @param word word(in string) of interest
     * @return content of the node found
     */
    Object search(String word);

    /**
     * Parse a whole text.
     *
     * @param text input text
     * @return emits of found words
     */
    List<Emit> parse(String text);
}
