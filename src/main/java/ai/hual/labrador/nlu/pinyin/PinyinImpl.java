package ai.hual.labrador.nlu.pinyin;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.pinyin.hanyupinyin.HanyuPinyin;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * {@link HanyuPinyin} based implementation of the <tt>Pinyin</tt> interface.
 *
 * @author Yuqi
 * @see Pinyin
 * @since 1.8
 */
public class PinyinImpl implements Pinyin {

    private HanyuPinyin hp;

    public PinyinImpl() {
        this.hp = new HanyuPinyin();
    }

    @Override
    public String getPinyin(String input) {

        hp.setInput(input);
        return hp.toString().trim();
    }

    /**
     * Construct a map from pinyin to its robust pair from file.
     *
     * @param file file name in config.properties
     * @return multi map, from pinyin to its pair
     */
    public static ListMultimap<String, PinyinScoreTuple> getPinyinRobustMap(String file) {
        ListMultimap<String, PinyinScoreTuple> pinyinMap = ArrayListMultimap.create();

        // read from file
        String PINYIN_PROB_FILE = Config.get(file);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Config.getLoader().getResourceAsStream(PINYIN_PROB_FILE), StandardCharsets.UTF_8))) {
            br.lines().forEach(line -> {
                // skip comment line
                if (line.equals("") || line.charAt(0) == '#')
                    return;
                // construct pinyin tuple
                String[] pinyinSplit = line.split(":");
                String pinyin = pinyinSplit[0];
                // put pinyin itself first
                if (pinyinMap.get(pinyin).size() == 0)
                    pinyinMap.put(pinyin, new PinyinScoreTuple(pinyin, 1f));
                String[] robustPinyinSplit = pinyinSplit[1].split("=");
                String robustPinyin = robustPinyinSplit[0];
                double score = Double.parseDouble(robustPinyinSplit[1]);

                pinyinMap.put(pinyin, new PinyinScoreTuple(robustPinyin, score));
            });
        } catch (IOException e) {
            throw new NLUException("Error reading pinyin prob file.", e);
        }

        return pinyinMap;
    }
}
