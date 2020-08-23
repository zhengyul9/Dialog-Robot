package ai.hual.labrador.nlg.answer;

import ai.hual.labrador.constants.Channel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ChannelRandomAnswerPostprocessor implements AnswerPostprocessor {

    private static final Logger logger = LoggerFactory.getLogger(ChannelRandomAnswerPostprocessor.class);

    private static final Random RANDOM = new Random();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Answer>> ANSWER_LIST_TYPE = new TypeReference<List<Answer>>() {
    };

    private String channel;

    public ChannelRandomAnswerPostprocessor(String channel) {
        this.channel = channel;
    }

    @Override
    public String process(String answer) {
        try {
            List<Answer> answerList = MAPPER.readValue(answer, ANSWER_LIST_TYPE);
            if (answerList == null || answerList.isEmpty()) {
                logger.warn("Answer list is empty, returning empty string");
                return StringUtils.EMPTY;
            }

            // find the channel while maintain default channel index
            int defaultChannelIndex = 0;
            for (int i = 0; i < answerList.size(); i++) {
                Answer a = answerList.get(i);
                if (Objects.equals(channel, a.getChannel())) {
                    return randomAnswer(a);
                }
                if (Objects.equals(Channel.DEFAULT_CHANNEL, a.getChannel())) {
                    defaultChannelIndex = i;
                }
            }

            // no correlated channel, return the default one or the first one
            Answer a = answerList.get(defaultChannelIndex);
            return randomAnswer(a);
        } catch (IOException e) {
            logger.warn(String.format("Unable to parse answer list json, returning original answer string %s. " +
                    "Please check your format of answer.", answer), e);
            return answer;
        }
    }

    private static String randomAnswer(Answer a) {
        if (a.getAnswerList() == null || a.getAnswerList().isEmpty()) {
            logger.warn("Answer list is empty, returning empty string");
            return StringUtils.EMPTY;
        }
        return a.getAnswerList().get(RANDOM.nextInt(a.getAnswerList().size()));
    }

}
