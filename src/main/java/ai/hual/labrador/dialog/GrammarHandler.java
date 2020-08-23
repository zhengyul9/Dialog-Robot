package ai.hual.labrador.dialog;

import ai.hual.labrador.exceptions.DialogException;
import ai.hual.labrador.nlu.Grammar;
import ai.hual.labrador.nlu.GrammarModel;
import ai.hual.labrador.nlu.GrammarModelSerDeser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

/**
 * A grammar handler that handle corpus generated grammar model and grammar data in database.
 * Created by Dai Wentao on 2017/6/28.
 */
public class GrammarHandler {

    private static final GrammarModelSerDeser grammarSerDeser = new GrammarModelSerDeser();

    private final List<Grammar> grammars;

    public GrammarHandler(List<Grammar> grammars) {
        this.grammars = grammars;
    }

    /**
     * Generate model based on a given {@link GrammarModel}, modifying that base model and return it.
     *
     * @param base The model that grammars handler generation bases on
     * @return The modified base model
     */
    public GrammarModel handleGrammar(GrammarModel base) {
        base.getGrammars().addAll(grammars);
        return base;
    }

    /**
     * Add system maintained grammars.
     */
    public void addSystemGrammars(GrammarModel grammarModel) {
        ClassLoader classLoader = getClass().getClassLoader();
        byte[] grammars;
        try {
            grammars = IOUtils.toByteArray(classLoader.getResourceAsStream("system_grammars"));
        } catch (IOException e) {
            throw new DialogException("Can not find system_grammars configuration file");
        }
        if (grammarModel == null)
            grammarModel = new GrammarModel();
        grammarModel.getGrammars().addAll(grammarSerDeser.deserialize(grammars).getGrammars());
    }


}
