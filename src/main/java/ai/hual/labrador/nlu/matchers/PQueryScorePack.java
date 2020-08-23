package ai.hual.labrador.nlu.matchers;

import java.util.List;

public class PQueryScorePack {
    private List<String> queryList;
    private List<Double> scoreList;
    private String bot;
    private int task;
    private int limit;

    public PQueryScorePack() {
    }

    public PQueryScorePack(List<String> queryList, List<Double> scoreList, String bot, int task, int limit) {
        this.queryList = queryList;
        this.scoreList = scoreList;
        this.bot = bot;
        this.task = task;
        this.limit = limit;
    }

    public List<String> getQueryList() {
        return queryList;
    }

    public void setQueryList(List<String> queryList) {
        this.queryList = queryList;
    }

    public List<Double> getScoreList() {
        return scoreList;
    }

    public void setScoreList(List<Double> scoreList) {
        this.scoreList = scoreList;
    }

    public String getBot() {
        return bot;
    }

    public void setBot(String bot) {
        this.bot = bot;
    }

    public int getTask() {
        return task;
    }

    public void setTask(int task) {
        this.task = task;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
