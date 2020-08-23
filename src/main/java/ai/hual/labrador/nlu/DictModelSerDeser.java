package ai.hual.labrador.nlu;

import ai.hual.labrador.exceptions.NLUException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * serialize {@link DictModel} to and deserialize {@link DictModel} from a byte array.
 * Created by Dai Wentao on 2017/6/28.
 */
public class DictModelSerDeser {

    /**
     * The separator between params of a line of serialized dict.
     */
    public static final String PARAM_SEPARATOR = "\t";
    public static final String PARAM_SEPARATOR_REGEX = "\t+";

    /**
     * The prefix of a comment line.
     */
    public static final String COMMENT_PREFIX = "#";

    /**
     * Deserialize dict model with given multimap model data.
     * The byte array should be in the same format as the result of {@link #serialize(DictModel)}.
     *
     * @param modelData A model data with a dict model in it.
     * @return The DictModel deserialized from byte array.
     */
    public DictModel deserialize(byte[] modelData) {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(modelData), StandardCharsets.UTF_8));
        List<Dict> dict = br.lines().map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith(COMMENT_PREFIX))
                .map(this::deserializeLine)
                .collect(Collectors.toList());
        return new DictModel(dict);
    }

    private Dict deserializeLine(String line) {
        // split with \t
        String[] params = line.split(PARAM_SEPARATOR_REGEX, 3);
        if (params.length < 2) {
            throw new NLUException(String.format("Error in dict deserialization for line: %s", line));
        }
        String label = params[0];
        String word = params[1];
        String aliases = (params.length == 2) ? null : params[2].trim();

        // no aliases
        if (aliases == null || aliases.isEmpty()) {
            return new Dict(label, word, null);
        }

        // invalid format of aliases
        if (!aliases.matches(Dict.ALIASES_VALIDATION_REGEX)) {
            throw new NLUException(String.format("Error in dict aliases deserialization for line: %s", line));
        }

        // dict with word and aliases
        return new Dict(label, word, aliases);
    }

    /**
     * Serialize the dict model into a byte array.
     * The array is encoded in UTF-8 charset, each line of which presents a word in the dict.
     * Each line consists of the label(type) of the word, the word and it's aliases,
     * separated with {@link #PARAM_SEPARATOR}.
     * <p>
     * <pre>
     * 演员   周杰伦     周董,杰伦
     * 歌手   周杰伦     周董,杰伦
     * 歌手   林俊杰
     * 歌手   蔡依林     淋淋
     * </pre>
     *
     * @param dictModel The DictModel to be serialized.
     * @return A byte array that contains the dict model.
     */
    public byte[] serialize(DictModel dictModel) {
        List<Dict> dict = dictModel.getDict();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteOut, StandardCharsets.UTF_8));
        dict.forEach(d -> serializeItem(writer, d));
        writer.close();
        return byteOut.toByteArray();
    }

    private void serializeItem(PrintWriter writer, Dict d) {
        writer.print(d.getLabel());
        writer.print(PARAM_SEPARATOR);
        writer.print(d.getWord());
        if (d.getAliases() != null) {
            writer.print(PARAM_SEPARATOR);
            writer.print(d.getAliases());
        }
        writer.println();
    }

}
