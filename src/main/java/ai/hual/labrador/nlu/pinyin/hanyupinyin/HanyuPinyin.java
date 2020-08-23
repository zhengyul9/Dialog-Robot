/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either
 * in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and
 * by any means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors of this software dedicate
 * any and all copyright interest in the software to the public domain. We make this dedication for
 * the benefit of the public at large and to the detriment of our heirs and successors. We intend
 * this dedication to be an overt act of relinquishment in perpetuity of all present and future
 * rights to this software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */

package ai.hual.labrador.nlu.pinyin.hanyupinyin;

import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.trie.PlainOldTrie;
import ai.hual.labrador.nlu.trie.PlainOldTrieSerDeser;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version : 1.7
 * @name : PinyinFormat.java
 * @updated : 2015-11-03
 * @license : http://unlicense.org/ The Unlicense
 * @git : https://github.com/pffy/java-hanyupinyin
 * @notes : Converts Hanzi to Hanyu Pinyin with display options.
 */
public class HanyuPinyin {


    private static final String FILE_NOT_LOADED = "File not loaded: ";

    private String output = "";
    private String input = "";

    private Tone toneMode = Tone.TONE_NUMBERS;

    private JSONObject tmdx = new JSONObject();
    private JSONObject tndx = new JSONObject();
    private JSONObject trdx = new JSONObject();
    private JSONObject tfdx = new JSONObject();

    private PlainOldTrie hpTrie = new PlainOldTrie();

    /**
     * Builds this object.
     */
    public HanyuPinyin() {
        init();
    }

    /**
     * Builds this object with mode set.
     */
    public HanyuPinyin(Tone mode) {
        setMode(mode);
        init();
    }

    /**
     * Builds object and sets input string.
     *
     * @param str Chinese character or Hanyu Pinyin input
     */
    public HanyuPinyin(String str) {
        init();
        setInput(str);
    }

    /**
     * Builds object, sets input string, and sets tone mode
     *
     * @param str  - Chinese character or Hanyu Pinyin input
     * @param mode - tone mark display mode as Enum
     */
    public HanyuPinyin(String str, Tone mode) {
        init();
        setMode(mode);
        setInput(str);
    }

    /**
     * Builds object, sets input string, and sets tone mode
     *
     * @param str  - Chinese character or Hanyu Pinyin input
     * @param mode - tone mark display mode as n integer
     * @deprecated since 1.7
     */
    public HanyuPinyin(String str, int mode) {
        init();
        setMode(mode);
        setInput(str);
    }

    /**
     * Returns string implementation of this object
     *
     * @return str - Hanyu Pinyin in specified tone mode
     */
    @Override
    public String toString() {
        return this.output;
    }

    /**
     * Returns input as a string
     *
     * @return str - input string
     */
    public String getInput() {
        return this.input;
    }

    /**
     * Sets input string for conversion by the object
     *
     * @param str - input string for conversion
     * @return HanyuPinyin - this object
     */
    public final HanyuPinyin setInput(String str) {

        if (str == null) {
            this.input = "";
        } else {
            this.input = normalizeUmlaut(str);
        }

        convert();
        return this;
    }

    /**
     * Returns tone display mode with an enum type Tone
     *
     * @return Tone - The enum Type called Tone
     */
    public Tone getMode() {
        return this.toneMode;
    }

    /**
     * Sets the tone display mode with an Enum type
     *
     * @param mode - tone display mode of enum type Tone
     * @return HanyuPinyin - this object
     */
    public final HanyuPinyin setMode(Tone mode) {

        if (mode == null) {
            this.toneMode = Tone.TONE_NUMBERS;
        } else {
            this.toneMode = mode;
        }

        convert();
        return this;
    }

    /**
     * Sets the tone display mode with an integer.
     * <p>
     * 2 - TONE_MARKS; 3 - TONES_OFF; otherwise - TONE_NUMBERS
     *
     * @param mode - tone display mode as an integer
     * @return HanyuPinyin - this object
     * @deprecated since 1.7
     */
    public final HanyuPinyin setMode(int mode) {

        switch (mode) {
            case 2:
                this.toneMode = Tone.TONES_OFF;
                break;
            case 3:
                this.toneMode = Tone.TONE_MARKS;
                break;
            default:
                this.toneMode = Tone.TONE_NUMBERS;
                break;
        }

        convert();
        return this;
    }

    // converts input based on class properties
    private void convert() {

        String str = input;

        // convert Hanzi to Pinyin
        str = hanziToPinyin(str, this.hpTrie);

        // append a space after all non-word character,
        // or the non-word will be append to its next character's pinyin
        str = str.replaceAll("([^\\w\\s])", "$1 ");
        this.output = str;
    }

    // converts input based on class properties
    @Deprecated
    private void convert_full() {

        String str;
        Tone tone;

        Iterator<?> keys;
        String key;

        str = input;
        tone = toneMode;

        // convert Hanzi to Pinyin
        str = hanziToPinyin(str, this.hpTrie);

        // converts pinyin display based on tone mode setting
        switch (tone) {

            case TONE_MARKS:

                keys = tmdx.keys();

                // converts to tone marks
                while (keys.hasNext()) {
                    key = (String) keys.next();
                    str = str.replace(key, tmdx.getString(key) + " ");
                }

                keys = tfdx.keys();

                // safely removes tone5
                while (keys.hasNext()) {
                    key = (String) keys.next();
                    str = str.replace(key, tfdx.getString(key) + " ");
                }

                break;
            case TONES_OFF:

                keys = trdx.keys();

                // remove all tone numbers and marks
                while (keys.hasNext()) {
                    key = (String) keys.next();
                    str = str.replace(key, trdx.getString(key) + " ");
                }

                keys = tfdx.keys();

                // safely removes tone5
                while (keys.hasNext()) {
                    key = (String) keys.next();
                    str = str.replace(key, tfdx.getString(key) + " ");
                }

                break;

            default:

                keys = tndx.keys();

                // converts marks to numbers
                while (keys.hasNext()) {
                    key = (String) keys.next();
                    str = str.replace(key, tndx.getString(key) + " ");
                }

                break;
        }

        str = atomize(str);
        // append a space after all non-word character,
        // or the non-word will be append to its next character's pinyin
        str = str.replaceAll("([^\\w\\s])", "$1 ");
        this.output = str;
    }

    // atomizes pinyin, creating space between pinyin units
    @Deprecated
    private String atomize(String str) {

        Iterator<?> keys;
        String key;

        keys = tmdx.keys();

        // atomizes pin1yin1 -> pin1 yin1
        while (keys.hasNext()) {
            key = (String) keys.next();
            str = str.replace(key, key + " ");
        }

        return vacuum(str);
    }

    // removes excess space between pinyin units
    private String vacuum(String str) {
        return str.replaceAll("[^\\S\\n]{2,}", " ");
    }

    /**
     * Preprocess
     *
     * @param str input
     * @return processed input
     */
    private String normalizeUmlaut(String str) {
        // normalizes umlaut u to double-u (uu)
        return str.replaceAll("ü", "uu")
                .replaceAll("u:", "uu")
//              .replaceAll("\\s", "  ")
                .replaceAll("(\\w)", "$1 ");
    }


    private void init() {

        // Fetch Json file path
        new Config();
        PlainOldTrieSerDeser plainOldTrieSerDeser = new PlainOldTrieSerDeser();
        plainOldTrieSerDeser.fileToTrie("hpTrie", hpTrie);
    }

    /**
     * loads JSON idx files into JSONObjects.
     */
    private JSONObject loadIdx(String str) {

        JSONObject jo;
        InputStream is;

        is = Config.getLoader().getResourceAsStream(str);
        jo = new JSONObject(new JSONTokener(is));

        return jo;
    }

    /**
     * Construct a trie from json object, where key and value are
     * expected to correspond to chinese and pinyin.
     *
     * @param jsonObject json object to be iterated
     * @param trie       result trie
     */
    private void jsonObjectToTrie(JSONObject jsonObject, PlainOldTrie trie) {

        Iterator<?> keys = jsonObject.keys();  // chinese character string
        String chinese;
        String pinyin;
        while (keys.hasNext()) {
            chinese = (String) keys.next();
            pinyin = jsonObject.getString(chinese);
            ChinesePinyinTuple tuple = new ChinesePinyinTuple(chinese, pinyin);
            trie.insert(chinese, tuple);
        }
    }

    /**
     * Convert input string of Chinese to pinyin.
     *
     * @param input string in chinese
     * @param trie  trie of pinyin
     * @return string in pinyin, split by space
     */
    private String hanziToPinyin(String input, PlainOldTrie trie) {

        int textPos = 0;

        while (textPos < input.length()) {
            String subInput = input.substring(textPos);
            // search in trie
            ChinesePinyinTuple tuple = (ChinesePinyinTuple) trie.search(subInput);
            if (tuple != null) {
                // replace
                String replace = tuple.pinyin + " ";
                input = input.replaceFirst(tuple.chinese, replace);
                textPos += replace.length() - tuple.chinese.length() + 1;
            } else {
                Pattern p = Pattern.compile("[^\\p{InCJK_UNIFIED_IDEOGRAPHS}]");  // not chinese character
                Matcher m = p.matcher("" + input.charAt(textPos));
                if (!m.find())  // not a punctuation mark
                    System.out.println("Can not change Chinese " + input.charAt(textPos) + " to pinyin");
                textPos++;
            }
        }

        return input;
    }
}
