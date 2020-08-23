package ai.hual.labrador.local.remote;

import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.matchers.IntentClassifierAccessor;
import ai.hual.labrador.nlu.matchers.IntentClassifierResult;
import ai.hual.labrador.nlu.matchers.IntentRankResponse;
import ai.hual.labrador.nlu.matchers.IntentScorePair;
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
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class RemoteIntentClassifierAccessor implements IntentClassifierAccessor {

    private static final Logger logger = LoggerFactory.getLogger(RemoteIntentClassifierAccessor.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public static final int DEFAULT_MAX_RELATED_QUESTION = 3;

    private String matchingURL;
    private String bot;
    private String task;

    private double actScoreWeight;

    public RemoteIntentClassifierAccessor(Properties properties) {
        this.matchingURL = properties.getProperty("intent.classify.url");
        this.bot = properties.getProperty("intent.classify.bot");
        this.task = properties.getProperty("intent.classify.task");
        this.actScoreWeight = Optional.ofNullable(properties.getProperty("intent.classify.act.weight")).map(Double::parseDouble).orElse(0d);
    }

    @Override
    public IntentClassifierResult handleIntentClassification(List<QueryAct> queryActs) {
        if (Strings.isNullOrEmpty(matchingURL) || Strings.isNullOrEmpty(bot) || Strings.isNullOrEmpty(task)) {
            logger.debug("RemoteIntentClassifierAccessor not configured properly, matchingURL: {}, bot: {}, task: {}", matchingURL,
                    bot, task);
            return null;
        }

        double bestScore = -1d;
        IntentRankResponse response = null;
        String pQuery = null;
        for (QueryAct act : queryActs) {
            IntentRankResponse intentRankResponse = queryIntentService(act.getPQuery(), bot, task, DEFAULT_MAX_RELATED_QUESTION + 1);
            if (intentRankResponse.getConfidence() < 0)
                intentRankResponse.setConfidence(0);
            double score = intentRankResponse.getConfidence() + actScoreWeight * act.getScore();
            if (intentRankResponse.getConfidence() == 1 || score > bestScore) {
                pQuery = act.getPQuery();
                response = intentRankResponse;
                bestScore = score;
                if (intentRankResponse.getConfidence() == 1)
                    break;
            }
        }

        if (response == null || response.getHits() == null || response.getHits().isEmpty()) {
            return null;
        }

        IntentClassifierResult result = new IntentClassifierResult();

        result.setQuery(pQuery);
        result.setHits(response.getHits());
        double score = response.getHits().isEmpty() ? 0 : response.getHits().get(0).getScore();
        result.setScore(score);
        result.setConfidence(response.getConfidence());
        List<IntentScorePair> intentScorePairs = response.getHits().stream()
                .map(i -> new IntentScorePair(i.getIntent(), i.getScore()))
                .collect(Collectors.toList());
        result.setIntents(intentScorePairs);

        return result;
    }

    private IntentRankResponse queryIntentService(String query, String bot, String task, int limit) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        logger.info("Requesting {}, bot: {}, task: {}", matchingURL, bot, task);
        HttpPost postMethod = new HttpPost(matchingURL);
        ContentType contentType = ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8);
        postMethod.setEntity(MultipartEntityBuilder.create()
                .addTextBody("query", query, contentType)
                .addTextBody("bot", bot, contentType)
                .addTextBody("task", task, contentType)
                .addTextBody("limit", String.valueOf(limit), contentType)
                .build());
        try {
            HttpResponse rawResponse = httpClient.execute(postMethod);
            return mapper.readValue(rawResponse.getEntity().getContent(), IntentRankResponse.class);
        } catch (IOException e) {
            logger.warn("Unable to read IntentRankResponse for question {}", query);
            logger.warn("Exception caught calling Intent Classifier service", e);
            IntentRankResponse response = new IntentRankResponse();
            response.setHits(new ArrayList<>());
            return response;
        }
    }
}