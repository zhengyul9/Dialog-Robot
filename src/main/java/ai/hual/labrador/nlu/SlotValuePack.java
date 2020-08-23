package ai.hual.labrador.nlu;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to present a group of slots and related matched String.
 * Created by ethan on 17-7-4.
 */
public class SlotValuePack {

    // Matched pQuery string, eg: 一九九五年{{数字}}月{{数字}}日
    public String matched;

    // Matched query string, eg: 一九九五年5月3日
    public String matchedQuery;

    // slots within the ValuePack.
    public List<SlotValue> slotValues;

    // Type of the ValuePack.
    public String key;

    // Name of the lambda functions to transform ValuePack, eg:prevSeason().
    public String label;

    public int start;
    public int end;
    public int realStart;
    public int realEnd;

    /**
     * Initialize value pack with matched string, other fields to 0 and "",
     * for cases that no keys found in matched string.
     *
     * @param matched matched string
     */
    public SlotValuePack(String matched) {
        this.matched = matched;
        this.matchedQuery = "";
        this.slotValues = new ArrayList<>();
        this.label = "";
        this.start = 0;
        this.end = 0;
        this.realStart = 0;
        this.realEnd = 0;
    }

    /**
     * Constructor without matched query string.
     *
     * @param matched    matched pQuery string
     * @param slotValues list of slot values in concern
     * @param key        key name
     * @param label      label to be applied to slot values in transform
     * @param start      start position
     * @param end        end position
     * @param realStart  real start position w.r.t original query
     * @param realEnd    real end position w.r.t original query
     */
    public SlotValuePack(String matched, List<SlotValue> slotValues, String key,
                         String label, int start, int end, int realStart, int realEnd) {

        this(matched, "", slotValues, key, label, start, end, realStart, realEnd);
    }

    /**
     * Default constructor.
     *
     * @param matched      matched pQuery string
     * @param matchedQuery matched query string
     * @param slotValues   list of slot values in concern
     * @param key          key name
     * @param label        label to be applied to slot values in transform
     * @param start        start position
     * @param end          end position
     * @param realStart    real start position w.r.t original query
     * @param realEnd      real end position w.r.t original query
     */
    public SlotValuePack(String matched, String matchedQuery, List<SlotValue> slotValues, String key,
                         String label, int start, int end, int realStart, int realEnd) {

        this.matched = matched;
        this.matchedQuery = matchedQuery;
        this.slotValues = slotValues;
        this.key = key;
        this.label = label;
        this.start = start;
        this.end = end;
        this.realStart = realStart;
        this.realEnd = realEnd;
    }

    public String toString() {

        return "matched pQuery string: " + matched + ",matched query string: " + matchedQuery + ", with "
                + slotValues.size() + " slots, " + "label: " + label + ",\t" +
                "start: " + start + ", end: " + end + ", real start: " + realStart + ", real end: " + realEnd;
    }
}
