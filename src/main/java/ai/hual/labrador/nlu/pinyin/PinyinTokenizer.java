package ai.hual.labrador.nlu.pinyin;

import ai.hual.labrador.utils.Tokenizer;

import java.util.Arrays;
import java.util.List;

/**
 * Tokenizer for tokenize a string of pinyin split
 * by space, into a list of tokens.
 *
 * @author Yuqi
 * @see Tokenizer
 * @since 1.8
 */
public class PinyinTokenizer implements Tokenizer {

    /**
     * Tokenize a string of pinyin to a list of token.
     *
     * @param input string of multiple pinyin split by space
     * @return list of pinyin token
     */
    @Override
    public List<String> tokenize(String input) {
        List<String> output = Arrays.asList((String[]) input.split(" "));
        return output;
    }
}
