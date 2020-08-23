package ai.hual.labrador.nlu;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Model to present the information of slots during NLU.
 * Created by ethan on 17-6-30.
 */
@JsonSerialize(using = SlotValueJsonSerializer.class)
@JsonDeserialize(using = SlotValueJsonDeserializer.class)
public class SlotValue implements Comparable<SlotValue> {

    // Value of the slot, eg: 1995-10-1, sweet, etc.
    public Object matched;

    // Name of the lambda functions to transform slot value, eg:duration(w).
    public String label;

    // Type of the slot, eg: Date, Taste, etc.
    public String key;

    // Original start position of this slot
    public int realStart;

    // Original end position of this slot
    public int realEnd;

    // real length in origin query
    public int realLength;

    // start position of this slot in processed query.
    public int start;

    // end position of this slot in processed query.
    public int end;

    // length in pQuery
    public int length;

    // matched regex
    public String regex;

    public SlotValue() {

    }

    /**
     * Copy constructor.
     *
     * @param slotValue a slot value to be copied from
     */
    public SlotValue(SlotValue slotValue) {
        this.matched = slotValue.matched;
        this.key = slotValue.key;
        this.label = slotValue.label;
        this.start = slotValue.start;
        this.end = slotValue.end;
        this.realStart = slotValue.realStart;
        this.realEnd = slotValue.realEnd;
        this.length = slotValue.length;
        this.realLength = slotValue.realLength;
        this.regex = slotValue.regex;
    }

    public SlotValue(Object matched) {
        this.matched = matched;
        this.key = "";
        this.label = "";
        this.start = 0;
        this.end = 0;
        this.realStart = 0;
        this.realEnd = 0;
        this.length = 0;
        this.realLength = 0;
    }

    /**
     * Default constructor.
     *
     * @param matched   matched object
     * @param key       key name
     * @param label     label name
     * @param start     start position
     * @param end       end position
     * @param realStart real start with respect to original query
     * @param realEnd   real end with respect to original query
     */
    public SlotValue(Object matched, String key, String label, int start, int end, int realStart, int realEnd) {
        this.matched = matched;
        this.key = key;
        this.label = label;
        this.start = start;
        this.end = end;
        this.realStart = realStart;
        this.realEnd = realEnd;

        this.length = end - start;
        this.realLength = realEnd - realStart;
    }

    public SlotValue(Object matched, String key, String label, int start, int end, int realStart, int realEnd, String regex) {
        this.matched = matched;
        this.key = key;
        this.label = label;
        this.start = start;
        this.end = end;
        this.realStart = realStart;
        this.realEnd = realEnd;

        this.length = end - start;
        this.realLength = realEnd - realStart;
        this.regex = regex;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SlotValue))
            return false;
        SlotValue slotValue = (SlotValue) o;

        return this.getClass().equals(o.getClass()) && this.matched.equals(slotValue.matched);

    }

    @Override
    public int hashCode() {
        return this.matched.hashCode();
    }

    public String toString() {
        return this.key + "=(" + this.matched + ")," + this.realStart + "-" + this.realEnd +
                "," + this.start + "-" + this.end;
    }

    @Override
    public int compareTo(SlotValue other) {
        return Integer.compare(this.realStart, other.realStart);
    }

    public Object getMatched() {
        return matched;
    }

    public String getKey() {
        return key;
    }

    @JsonIgnore
    public int getStart() {
        return start;
    }

    @JsonIgnore
    public int getEnd() {
        return end;
    }

    public int getRealStart() {
        return realStart;
    }

    public int getRealEnd() {
        return realEnd;
    }

    @JsonIgnore
    public int getLength() {
        return length;
    }

    public int getRealLength() {
        return realLength;
    }

}
