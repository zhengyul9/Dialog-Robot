package ai.hual.labrador.dialog;

import ai.hual.labrador.dm.DM;
import ai.hual.labrador.dm.DMResult;
import ai.hual.labrador.exceptions.DMException;
import ai.hual.labrador.nlg.NLG;
import ai.hual.labrador.nlg.NLGImpl;
import ai.hual.labrador.nlg.ResponseAct;
import ai.hual.labrador.nlu.NLU;
import ai.hual.labrador.nlu.NLUImpl;
import ai.hual.labrador.nlu.NLUResult;
import ai.hual.labrador.nlu.QueryAct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 对话系统的主要实现类型
 */
public class DialogSystem {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * NLU模块
     */
    private NLU nlu;

    /**
     * DM
     */
    private DM dm;

    /**
     * 答案生成模块
     */
    private NLG nlg;

    /**
     * 初始化
     */
    public DialogSystem(NLU nlu, DM dm, NLG nlg, AccessorRepository accessorRepository) {
        this.nlu = nlu;
        this.dm = dm;
        this.nlg = nlg;
    }

    public DialogSystem(DialogModel model, Properties properties, AccessorRepository accessorRepository) {
        logger.debug("DialogSystem的参数: {}", properties);

        // 初始化NLU模块，这里使用基于模板的NLU，指定分句的方法
        nlu = new NLUImpl(model.getDictModel(), model.getGrammarModel(), accessorRepository, properties);

        // initialize DM
        dm = model.getDMModel().make(accessorRepository, properties);

        // 初始化NLG模块，这里使用基于模板的NLG
        nlg = new NLGImpl(model.getTemplateModel());
    }

    @Deprecated
    public DialogResult process(String sessionID, String input, String state) {
        return process(sessionID, input, null, state);
    }

    /**
     * 进行一轮对话，处理一句自然语言输入，更新系统状态并产生相应动作，然后产生系统输出
     *
     * @param sessionID  session的id，用于记录日志等
     * @param input      自然语言输出
     * @param turnParams 当轮对话的参数，以字符串表示，在对话中反序列化
     * @param state      当前session的状态，以字符串表示
     * @return 系统输出
     */
    public DialogResult process(String sessionID, String input, @Nullable String turnParams, String state) {
        logger.debug("Input {}, {}", sessionID, input);

        // 分句子
        NLUResult nluResult = nlu.understand(input);
        logger.debug("InputAct {}, {}", sessionID, nluResult);

        // 调用状态管理的方法，更新系统状态
        DMResult dmResult = dm.process(input, nluResult, turnParams, state);

        ResponseAct responseAct = dmResult.getAct();
        state = dmResult.getState();

        if (responseAct == null) {
            throw new DMException("No response act returned");
        }
        logger.debug("ResponseAct {}, {}", sessionID, responseAct);
        logger.debug("State {}, {}", sessionID, state);

        // 通过回答DA产生自然语言答案并返回
        String answer = nlg.generate(responseAct);
        logger.debug("Answer {}, {}", sessionID, answer);
        return new DialogResult(answer, state, dmResult.getInstructions(), nluResult, responseAct);
    }

    public void close() {
        if (dm != null) {
            dm.close();
        }
    }

    /**
     * Reorder queryActs by score in mlNLU.
     *
     * @param hypsCandidates candidate list from hyps
     * @param mlCandidates   candidate list from ML
     * @return list of reordered candidates
     */
    private static List<QueryAct> reorderHyps(List<QueryAct> hypsCandidates, List<QueryAct> mlCandidates) {
        float MLNLU_THRESHOLD = 0.7f;
        ArrayList<QueryAct> reorderedList = new ArrayList<>(hypsCandidates);
        // return first ML result if nlu fails to recognize intent
        if (reorderedList.size() == 1 && reorderedList.get(0).getPQuery().equals(reorderedList.get(0).getQuery())) {
            if (mlCandidates.get(0).getScore() > MLNLU_THRESHOLD)
                return Collections.singletonList(mlCandidates.get(0));
            else
                return hypsCandidates;
        }
        Map<String, Double> mlIntentScoreMap = new HashMap<>();
        mlCandidates.forEach(mlAct -> mlIntentScoreMap.put(mlAct.getPQuery(), mlAct.getScore()));
        // reward hypsAct score with mlAct score
        reorderedList.forEach(hypsAct -> hypsAct.setScore(hypsAct.getScore() *
                (1 + mlIntentScoreMap.getOrDefault(hypsAct.getPQuery(), 0d))));
        Collections.sort(reorderedList);
        return reorderedList;
    }

    /**
     * 获取系统的NLU模块
     *
     * @return The NLU object of this DialogSystem.
     */
    public NLU getNLU() {
        return nlu;
    }

    /**
     * 获取系统的DM模块
     *
     * @return The DM object of this DialogSystem
     */
    public DM getDM() {
        return dm;
    }

    /**
     * 获取系统的NLG模块
     *
     * @return The NLG object of this DialogSystem
     */
    public NLG getNLG() {
        return nlg;
    }

}