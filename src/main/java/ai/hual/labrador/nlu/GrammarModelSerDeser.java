package ai.hual.labrador.nlu;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.utils.RegexUtils;
import com.google.common.base.Joiner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * serialize {@link GrammarModel} to and deserialize {@link GrammarModel} from a byte array.
 * Created by Dai Wentao on 2017/6/28.
 */
public class GrammarModelSerDeser {

    /**
     * The separator between params of a line of serialized dict.
     */
    public static final String PARAM_SEPARATOR = "\t";
    public static final String PARAM_SEPARATOR_REGEX = "\\s+";

    /**
     * The prefix of a comment line.
     */
    public static final String COMMENT_PREFIX = "#";

    /**
     * Deserialize grammar model with given byte array model data.
     * The byte array should be in the same format as the result of {@link #serialize(GrammarModel)}.
     *
     * @param modelData A model data with a dict model in it.
     * @return The GrammarModel deserialized from byte array.
     */
    public GrammarModel deserialize(byte[] modelData) {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(modelData), StandardCharsets.UTF_8));
        List<Grammar> grammars = br.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith(COMMENT_PREFIX))
                .map(this::deserializeLine)
                .collect(Collectors.toCollection(ArrayList::new));
        return new GrammarModel(grammars);
    }

    private Grammar deserializeLine(String line) {
        String[] params = line.split(PARAM_SEPARATOR_REGEX, 4);
        if (params.length < 4) {
            throw new NLUException(String.format("Error in grammar deserialization for line: %s", line));
        }
        GrammarType type = GrammarType.valueOf(params[0]);
        String intent = params[1];
        double score;
        if (params[2].contains("MIN_VALUE")) {
            String scoreStr = params[2];
            String[] scoreStrSplit = scoreStr.split("\\*");
            if (scoreStrSplit.length == 2)
                score = Double.MIN_VALUE * Double.parseDouble(scoreStrSplit[1].trim());
            else if (scoreStrSplit.length == 1)
                score = Double.MIN_VALUE;
            else
                throw new NLUException("Grammar " + line + " malformatted in score");
        } else
            score = Double.parseDouble(params[2]);
        String content = params[3];
        if (type == GrammarType.INTENT_REGEX && !RegexUtils.isSlottedRegex(content)) {
            throw new NLUException(String.format("Illegal intent regex %s", content));
        }
        if (type == GrammarType.PHRASE_REGEX && !RegexUtils.isSlottedRegex(content)) {
            throw new NLUException(String.format("Illegal phrase regex %s", content));
        }
        return new Grammar(type, intent, content, score);
    }

    /**
     * Serialize the grammar model into a byte array.
     * The array is encoded in UTF-8 charset, each line of which presents a piece of grammar.
     * Each piece of grammar consists of it's type, intent, score and content, separated with {@link #PARAM_SEPARATOR_REGEX}.
     * <p>
     * <pre>
     * REGEX_PATTERN set_timer 1.0 a*b+
     * </pre>
     *
     * @param grammarModel The GrammarModel to be serialized.
     * @return A byte array that contains the grammar model.
     */
    public byte[] serialize(GrammarModel grammarModel) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteOut, StandardCharsets.UTF_8));
        grammarModel.getGrammars().forEach(g -> serializeItem(writer, g));
        writer.close();
        return byteOut.toByteArray();
    }

    private void serializeItem(PrintWriter writer, Grammar g) {
        writer.println(Joiner.on(PARAM_SEPARATOR).join(g.getType(), g.getLabel(), g.getScore(), g.getContent()));
    }

}
