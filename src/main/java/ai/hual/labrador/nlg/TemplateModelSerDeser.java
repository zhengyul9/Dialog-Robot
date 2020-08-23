package ai.hual.labrador.nlg;

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
 * serialize {@link TemplateModel} to and deserialize {@link TemplateModel} from a byte array.
 * Created by Dai Wentao on 2017/7/5.
 */
public class TemplateModelSerDeser {

    /**
     * The separator between params of a line of serialized dict.
     */
    public static final String PARAM_SEPARATOR = "\t";

    /**
     * Deserialize template model with given byte array model data.
     * The byte array should be in the same format as the result of {@link #serialize(TemplateModel)}.
     *
     * @param modelData A model data with a template model in it.
     * @return The TemplateModel deserialized from byte array.
     */
    public TemplateModel deserialize(byte[] modelData) {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(modelData), StandardCharsets.UTF_8));
        List<Template> templates = br.lines()
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.startsWith("//"))
                .map(line -> line.split(PARAM_SEPARATOR, 2))
                .map(params -> {
                    String label = params[0];
                    String content = params[1];
                    return new Template(label, content);
                })
                .collect(Collectors.toCollection(ArrayList::new));
        return new TemplateModel(templates);
    }

    /**
     * Serialize the template model into a byte array.
     * The array is encoded in UTF-8 charset, each line of which presents a piece of template.
     * Each piece of template consists of it's label and content, separated with {@link #PARAM_SEPARATOR}.
     * <p>
     * <pre>
     * Greeting Hi,{{name}}
     * </pre>
     *
     * @param templateModel The TemplateModel to be serialized.
     * @return A byte array that contains the template model.
     */
    public byte[] serialize(TemplateModel templateModel) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteOut, StandardCharsets.UTF_8));

        for (Template g : templateModel.getTemplates()) {
            writer.println(Joiner.on(PARAM_SEPARATOR).join(g.getLabel(), g.getContent()));
        }

        writer.close();
        return byteOut.toByteArray();
    }

}
