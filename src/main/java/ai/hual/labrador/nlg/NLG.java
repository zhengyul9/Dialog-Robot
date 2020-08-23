package ai.hual.labrador.nlg;

/**
 * Natural language generating interface of dialog.
 * Created by Dai Wentao on 2017/7/5.
 */
public interface NLG {

    /**
     * Generate a nature language TODO should this be some kind of rich text?
     *
     * @param act The output response dialog act
     * @return A string that represent the response. Basically it's a natural
     * language string.
     */
    String generate(ResponseAct act);

}
