package ai.hual.labrador.nlg.answer;

import java.io.Serializable;
import java.util.List;

/**
 * 答案  渠道组
 *
 * @author xiao he
 * @create 2018-04-28 11:51
 * @modified 2018-05-17 17:35 daiwt migrate from backend to algo
 */
public class Answer implements Serializable {

    private String channel;

    private List<String> answerList;

    public Answer() {
    }

    public Answer(String channel, List<String> answerList) {
        this.channel = channel;
        this.answerList = answerList;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public List<String> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<String> answerList) {
        this.answerList = answerList;
    }
}
