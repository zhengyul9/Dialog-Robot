package ai.hual.labrador.dialog;

import ai.hual.labrador.dm.DMModelSerDeser;
import ai.hual.labrador.dm.DMModelType;
import ai.hual.labrador.exceptions.DialogException;
import ai.hual.labrador.nlg.TemplateModelSerDeser;
import ai.hual.labrador.nlu.DictModelSerDeser;
import ai.hual.labrador.nlu.GrammarModelSerDeser;
import ai.hual.labrador.utils.ZipUtil;
import com.google.common.collect.ImmutableMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * serialize {@link DialogModel} to and deserialize {@link DialogModel} from a byte array.
 * Created by Dai Wentao on 2017/6/28.
 */
public class DialogModelSerDeser {

    // file names of config and model
    public static final String NAME_CONFIG = "labrador.config";
    public static final String NAME_DICT = "dict";
    public static final String NAME_GRAMMAR = "grammar";
    public static final String NAME_TEMPLATE = "template";
    public static final String NAME_DM = "dm";

    /**
     * Deserialize dialog model with given byte array.
     *
     * @param modelData A model data with a dict model in it.
     * @return The DialogModel deserialized from byte array.
     */
    public DialogModel deserialize(byte[] modelData) throws IOException {
        // serializers and deserializers
        DictModelSerDeser dictModelSerDeser = new DictModelSerDeser();
        GrammarModelSerDeser grammarModelSerDeser = new GrammarModelSerDeser();
        TemplateModelSerDeser templateModelSerDeser = new TemplateModelSerDeser();
        DMModelSerDeser dmModelSerDeser = new DMModelSerDeser();

        // read zip
        Map<String, byte[]> models = ZipUtil.readAllEntries(modelData);

        // dm and its config
        Properties config = new Properties();
        config.load(new InputStreamReader(new ByteArrayInputStream(models.get(NAME_CONFIG)), StandardCharsets.UTF_8));
        DMModelType dmModelType;
        try {
            dmModelType = DMModelType.valueOf(config.getProperty(Config.KEY_DM_MODEL_TYPE));
        } catch (Exception e) {
            throw new DialogException("Unable to read configuration of dialog model");
        }

        return new DialogModel(
                dictModelSerDeser.deserialize(models.get(NAME_DICT)),
                grammarModelSerDeser.deserialize(models.get(NAME_GRAMMAR)),
                dmModelSerDeser.deserialize(dmModelType, models.get(NAME_DM)),
                templateModelSerDeser.deserialize(models.get(NAME_TEMPLATE)));
    }

    /**
     * Serialize the dialog model into a byte array.
     *
     * @param dialogModel The DialogModel to be serialized.
     * @return A byte array that contains the dict model.
     */
    public byte[] serialize(DialogModel dialogModel) throws IOException {
        // serializers and deserializers
        DictModelSerDeser dictModelSerDeser = new DictModelSerDeser();
        GrammarModelSerDeser grammarModelSerDeser = new GrammarModelSerDeser();
        TemplateModelSerDeser templateModelSerDeser = new TemplateModelSerDeser();
        DMModelSerDeser dmModelSerDeser = new DMModelSerDeser();

        // config
        Properties config = new Properties();
        config.setProperty(Config.KEY_DM_MODEL_TYPE, dialogModel.getDMModel().getType().toString());
        ByteArrayOutputStream configOut = new ByteArrayOutputStream();
        config.store(new OutputStreamWriter(configOut, StandardCharsets.UTF_8), "");

        // zip
        return ZipUtil.putAllEntries(ImmutableMap.<String, byte[]>builder()
                .put(NAME_CONFIG, configOut.toByteArray())
                .put(NAME_DICT, dictModelSerDeser.serialize(dialogModel.getDictModel()))
                .put(NAME_GRAMMAR, grammarModelSerDeser.serialize(dialogModel.getGrammarModel()))
                .put(NAME_TEMPLATE, templateModelSerDeser.serialize(dialogModel.getTemplateModel()))
                .put(NAME_DM, dmModelSerDeser.serialize(dialogModel.getDMModel()))
                .build());
    }

}
