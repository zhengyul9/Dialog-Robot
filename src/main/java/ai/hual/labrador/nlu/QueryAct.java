package ai.hual.labrador.nlu;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Representation of a processed sentence.
 * Created by Dai Wentao on 2017/6/26.
 */
@JsonSerialize(using = QueryActJsonSerializer.class)
@JsonDeserialize(using = QueryActJsonDeserializer.class)
public class QueryAct implements Comparable<QueryAct> {

    /**
     * Original query string.
     */
    private String query;

    /**
     * Processed query string.
     */
    private String pQuery;

    /**
     * Intent of the query.
     */
    private String intent;

    /**
     * Regex used to match intent in pQuery.
     */
    private String regex;

    /**
     * Matched regex start position w.r.t pQuery.
     */
    private int regexStart;

    /**
     * Matched regex real start position w.r.t query.
     */
    private int regexRealStart;

    /**
     * Matched regex end position w.r.t pQuery.
     */
    private int regexEnd;

    /**
     * Matched regex real end position w.r.t pQuery.
     */
    private int regexRealEnd;

    /**
     * Slots extracted from query, with slot name as key and slot content as values.
     */
    private ListMultimap<String, SlotValue> slots;

    /**
     * Score of the query act.
     */
    private double score;

    public QueryAct() {
        slots = ArrayListMultimap.create();
        regexStart = 0;
        regexEnd = 0;
    }

    public QueryAct(String query) {
        this.query = query;
        this.pQuery = query;
        this.slots = ArrayListMultimap.create();
        this.score = 1f;
        regexStart = 0;
        regexEnd = 0;
    }

    /**
     * copy constructor
     *
     * @param queryAct act to be copied
     */
    public QueryAct(QueryAct queryAct) {
        query = queryAct.query;
        pQuery = queryAct.pQuery;
        slots = ArrayListMultimap.create();
        // deep copy
        queryAct.getSlots().values().forEach(value -> slots.put(value.key, new SlotValue(value)));
        score = queryAct.score;
        regexStart = queryAct.getRegexStart();
        regexEnd = queryAct.getRegexEnd();
    }

    /**
     * @param query  query
     * @param pQuery pQuery
     * @param slots  slots, the slots of constructed QueryAct will copy the param slots
     * @param score  score
     */
    public QueryAct(String query, String pQuery, ListMultimap<String, SlotValue> slots, double score) {
        this.query = query;
        this.pQuery = pQuery;
        this.slots = ArrayListMultimap.create(slots);
        this.score = score;
        regexStart = 0;
        regexEnd = 0;
    }

    public QueryAct(String query, String pQuery, String intent, ListMultimap<String, SlotValue> slots, double score) {
        this.query = query;
        this.pQuery = pQuery;
        this.intent = intent;
        this.regexStart = 0;
        this.regexEnd = 0;
        this.slots = ArrayListMultimap.create(slots);
        this.score = score;
    }

    public QueryAct(String query, String pQuery, String intent, String regex, int regexStart, int regexEnd,
                    int regexRealStart, int regexRealEnd, ListMultimap<String, SlotValue> slots, double score) {
        this.query = query;
        this.pQuery = pQuery;
        this.intent = intent;
        this.regex = regex;
        this.regexStart = regexStart;
        this.regexEnd = regexEnd;
        this.regexRealStart = regexRealStart;
        this.regexRealEnd = regexRealEnd;
        this.slots = ArrayListMultimap.create(slots);
        this.score = score;
    }

    public List<String> getSlotAsStringList(String key) {
        return slots.get(key).stream().map(x -> x.matched.toString()).collect(Collectors.toList());
    }

    /**
     * Get slots sorted by start position as list.
     *
     * @return the list
     */
    public List<SlotValue> getSortedSlotAsList() {
        return slots.keySet().stream()
                .flatMap(k -> slots.get(k).stream())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get the last slot by start position.
     *
     * @return the last slotValue
     */
    public SlotValue getLastSlot() {
        List<SlotValue> sorted = getSortedSlotAsList();
        return sorted.isEmpty() ? null : sorted.get(sorted.size() - 1);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getPQuery() {
        return pQuery;
    }

    public void setPQuery(String pQuery) {
        this.pQuery = pQuery;
    }

    public ListMultimap<String, SlotValue> getSlots() {
        return slots;
    }

    public void setSlots(ListMultimap<String, SlotValue> slots) {
        this.slots = slots;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(QueryAct other) {
        return Double.compare(other.score, this.score);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof QueryAct)) return false;
        QueryAct other = (QueryAct) o;
        if (!this.getPQuery().equals(other.getPQuery())) {
            return false;
        }
        if (!((this.getIntent() == null && other.getIntent() == null) || (this.getIntent().equals(other.getIntent()))))
            return false;
        else {
            List<SlotValue> thisSlots = this.getSortedSlotAsList();
            List<SlotValue> otherSlots = other.getSortedSlotAsList();
            if (thisSlots.size() != otherSlots.size())
                return false;
            for (int i = 0; i < thisSlots.size(); i++) {
                if (!thisSlots.get(i).equals(otherSlots.get(i)))
                    return false;
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        return this.getPQuery().hashCode();
    }

    public String toString() {

        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("{query:").append(query).append(",pQuery:").append(pQuery).append(",intent:")
                .append(intent).append(",slots:[");

        slots.keySet().forEach(key ->
                slots.get(key).forEach(slotValue -> sBuffer.append(String.valueOf(slotValue)).append(", ")));

        sBuffer.append("],score:").append(score).append("}");

        return sBuffer.toString();
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public int getRegexEnd() {
        return regexEnd;
    }

    public void setRegexEnd(int regexEnd) {
        this.regexEnd = regexEnd;
    }

    public int getRegexStart() {
        return regexStart;
    }

    public void setRegexStart(int regexStart) {
        this.regexStart = regexStart;
    }

    public int getRegexRealStart() {
        return regexRealStart;
    }

    public void setRegexRealStart(int regexRealStart) {
        this.regexRealStart = regexRealStart;
    }

    public int getRegexRealEnd() {
        return regexRealEnd;
    }

    public void setRegexRealEnd(int regexRealEnd) {
        this.regexRealEnd = regexRealEnd;
    }
}