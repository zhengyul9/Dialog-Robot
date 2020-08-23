package ai.hual.labrador.local.remote;

import ai.hual.labrador.faq.FAQAccessor;
import ai.hual.labrador.faq.FaqAnswer;
import ai.hual.labrador.faq.FaqRankResponse;
import ai.hual.labrador.faq.FaqRankResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;

public class RemoteChatAccessor implements FAQAccessor {

    private static final int CHAT_ONLY = 2;


    public static final int DEFAULT_MAX_RELATED_QUESTION = 3;

    private static final Logger logger = LoggerFactory.getLogger(RemoteChatAccessor.class);
    private static ObjectMapper mapper = new ObjectMapper();

    private String matchingURL;
    private String bot;
    private String task;

    public RemoteChatAccessor() {
    }

    public RemoteChatAccessor(Properties properties) {
        matchingURL = properties.getProperty("faq.matching.url");
        bot = properties.getProperty("faq.matching.bot");
        task = properties.getProperty("faq.matching.task");
    }

    @Override
    public FaqAnswer handleFaqQuery(String question) {
        return query(question);
    }

    private FaqRankResponse queryFAQService(String question, String bot, String task, int limit) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        logger.info("Requesting {}, bot: {}, task: {}", matchingURL, bot, task);
        HttpPost postMethod = new HttpPost(matchingURL);
        ContentType contentType = ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8);
        postMethod.setEntity(MultipartEntityBuilder.create()
                .addTextBody("query", question, contentType)
                .addTextBody("bot", bot, contentType)
                .addTextBody("task", task, contentType)
                .addTextBody("limit", String.valueOf(limit), contentType)
                .addTextBody("chatting", String.valueOf(CHAT_ONLY))
                .build());
        try {
            HttpResponse rawResponse = httpClient.execute(postMethod);
            return mapper.readValue(rawResponse.getEntity().getContent(), FaqRankResponse.class);
        } catch (IOException e) {
            logger.warn("Unable to read FaqRankResponse for question {}", question);
            logger.warn("Exception caught calling FAQ service", e);
            FaqRankResponse response = new FaqRankResponse();
            response.setHits(new ArrayList<>());
            return response;
        }
    }

    private FaqAnswer query(String question) {
        if (Strings.isNullOrEmpty(matchingURL) || Strings.isNullOrEmpty(bot) || Strings.isNullOrEmpty(task)) {
            logger.debug("RemoteChatAccessor not configured properly, matchingURL: {}, bot: {}, task: {}", matchingURL,
                    bot, task);
            return null;
        }

        FaqRankResponse response = queryFAQService(question, bot, task, DEFAULT_MAX_RELATED_QUESTION + 1);

        if (response == null || response.getHits() == null || response.getHits().isEmpty()) {
            return null;
        }

        FaqAnswer answer = new FaqAnswer();
        answer.setConfidence(response.getConfidence());
        answer.setQuery(question);

        // related questions
        // find a question that equals to the query, if not found, use the first question as the standard question
        logger.info("Hits for question {}: {}, confidence: {}", question, response.getHits().size(), response.getConfidence());
        String processedQuestion = process(question);
        int firstQuestionIndex = 0;
        for (int i = 1; i < response.getHits().size(); i++) {
            if (process(response.getHits().get(i).getQuestion()).equals(processedQuestion)) {
                firstQuestionIndex = i;
                break;
            }
            boolean found = false;
            for (String sim : response.getHits().get(i).getSimQuestion()) {
                if (process(sim).equals(processedQuestion)) {
                    firstQuestionIndex = i;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        FaqRankResult firstQuestion = response.getHits().remove(firstQuestionIndex);
        response.getHits().add(0, firstQuestion);
        answer.setStandardQuestion(firstQuestion.getQuestion());
        answer.setAnswer(firstQuestion.getAnswer());

        return answer;
    }

    private static String process(String q) {
        return q == null ? null : q.trim().replaceAll(
                "[\\s？?，,。.：:“”\"‘’'！!（）()\\[\\]{}【】了不是的地得么吗吧哈呀哇哦呵]", "");
    }
}
