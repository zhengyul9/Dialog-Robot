package ai.hual.labrador.utils;

import java.util.List;

public interface Tokenizer {

    /**
     * Tokenize a string into a list of tokens.
     *
     * @param word string
     * @return list of token
     */
    List<String> tokenize(String word);
}
