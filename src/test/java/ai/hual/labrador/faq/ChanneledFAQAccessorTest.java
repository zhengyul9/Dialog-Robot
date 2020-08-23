package ai.hual.labrador.faq;

import ai.hual.labrador.constants.Channel;
import ai.hual.labrador.nlg.answer.Answer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChanneledFAQAccessorTest {

    private static FAQAccessor twoChannelAccessor;
    private static FAQAccessor twoChannelNoDefaultAccessor;
    private static FAQAccessor noAnswerAccessor;
    private static FAQAccessor plainTextAccessor;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // two channel accessor: [
        //   {"channel": "A", "answerList": [ "A1", "A2" ]},
        //   {"channel": "默认", "answerList": [ "D1", "D2" ]}
        // ]
        // with hits as a singleton list where the element's answer is [
        //   {"channel": "X", "answerList": [ "X1", "X2" ]},
        //   {"channel": "Y", "answerList": [ "Y1", "Y2" ]}
        // ]
        twoChannelAccessor = mock(FAQAccessor.class);
        when(twoChannelAccessor.handleFaqQuery(any(String.class))).thenAnswer(x -> {
            FaqAnswer twoChannelAnswer = new FaqAnswer();
            twoChannelAnswer.setAnswer(mapper.writeValueAsString(Arrays.asList(
                    new Answer("A", Arrays.asList("A1", "A2")),
                    new Answer(Channel.DEFAULT_CHANNEL, Arrays.asList("D1", "D2")))));
            FaqRankResult faqRankResult = new FaqRankResult();
            faqRankResult.setAnswer(mapper.writeValueAsString(Arrays.asList(
                    new Answer("X", Arrays.asList("X1", "X2")),
                    new Answer("Y", Arrays.asList("Y1", "Y2")))));
            return twoChannelAnswer;
        });

        // two channel no default accessor: [
        //   {"channel": "A", "answerList": [ "A1", "A2" ]},
        //   {"channel": "B", "answerList": [ "B1", "B2" ]}
        // ]
        twoChannelNoDefaultAccessor = mock(FAQAccessor.class);
        when(twoChannelNoDefaultAccessor.handleFaqQuery(any(String.class))).thenAnswer(x -> {
            FaqAnswer twoChannelNoDefaultAnswer = new FaqAnswer();
            twoChannelNoDefaultAnswer.setAnswer(mapper.writeValueAsString(Arrays.asList(
                    new Answer("A", Arrays.asList("A1", "A2")),
                    new Answer("B", Arrays.asList("B1", "B2")))));
            return twoChannelNoDefaultAnswer;
        });

        // no answer accessor: []
        noAnswerAccessor = mock(FAQAccessor.class);
        when(noAnswerAccessor.handleFaqQuery(any(String.class))).thenAnswer(x -> {
            FaqAnswer noAnswerAnswer = new FaqAnswer();
            noAnswerAnswer.setAnswer(mapper.writeValueAsString(Collections.emptyList()));
            return noAnswerAnswer;
        });


        // plain text accessor: "plain text"
        plainTextAccessor = mock(FAQAccessor.class);
        when(plainTextAccessor.handleFaqQuery(any(String.class))).thenAnswer(x -> {
            FaqAnswer plainTextAnswer = new FaqAnswer();
            plainTextAnswer.setAnswer("plain text");
            return plainTextAnswer;
        });
    }

    @Test
    void testChannelAnswer() {
        ChanneledFAQAccessor accessor = new ChanneledFAQAccessor(twoChannelAccessor, "A");
        assertTrue(Arrays.asList("A1", "A2").contains(
                accessor.handleFaqQuery("whatever").getAnswer()));
    }

    @Test
    void testChannelRandomAnswer() {
        ChanneledFAQAccessor accessor = new ChanneledFAQAccessor(twoChannelAccessor, "A");
        assertTrue(IntStream.range(0, 1000).anyMatch(ignored ->
                "A1".equals(accessor.handleFaqQuery("whatever").getAnswer())));
        assertTrue(IntStream.range(0, 1000).anyMatch(ignored ->
                "A2".equals(accessor.handleFaqQuery("whatever").getAnswer())));
    }

    @Test
    void testChannelDefaultAnswer() {
        ChanneledFAQAccessor accessor = new ChanneledFAQAccessor(twoChannelAccessor, "NoSuchChannel");
        assertTrue(Arrays.asList("D1", "D2").contains(
                accessor.handleFaqQuery("whatever").getAnswer()));
    }

    @Test
    void testNoDefaultChannel() {
        ChanneledFAQAccessor accessor = new ChanneledFAQAccessor(twoChannelNoDefaultAccessor, "NoSuchChannel");
        assertTrue(Arrays.asList("A1", "A2").contains(
                accessor.handleFaqQuery("whatever").getAnswer()));
    }

    @Test
    void testNoAnswerChannel() {
        ChanneledFAQAccessor accessor = new ChanneledFAQAccessor(noAnswerAccessor, "A");
        assertNull(accessor.handleFaqQuery("whatever"));
    }

    @Test
    void testPlainTextChannel() {
        ChanneledFAQAccessor accessor = new ChanneledFAQAccessor(plainTextAccessor, "A");
        assertEquals("plain text",
                accessor.handleFaqQuery("whatever").getAnswer());
    }

}