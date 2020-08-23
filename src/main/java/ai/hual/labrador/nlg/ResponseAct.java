package ai.hual.labrador.nlg;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representation of a processed sentence.
 * Created by Dai Wentao on 2017/7/5.
 */
@JsonSerialize(using = ResponseActJsonSerializer.class)
@JsonDeserialize(using = ResponseActJsonDeserializer.class)
public class ResponseAct {

    /**
     * label of response dialog act
     */
    private String label;

    /**
     * Slots extracted from query, with sLot name as key and slot content as values.
     */
    private ListMultimap<String, Object> slots;

    public ResponseAct() {
        slots = ArrayListMultimap.create();
    }

    /**
     * Initiate with specified label and empty slots.
     *
     * @param label the label of response act
     */
    public ResponseAct(String label) {
        this.label = label;
        slots = ArrayListMultimap.create();
    }

    /**
     * Copy construction.
     *
     * @param act Another response act to be copied from.
     */
    public ResponseAct(ResponseAct act) {
        label = act.label;
        slots = ArrayListMultimap.create(act.slots);
    }

    public ResponseAct put(String slotName, Object slotValue) {
        slots.put(slotName, slotValue);
        return this;
    }

    @Override
    public String toString() {
        return label + "(" + slots.keySet().stream().map(key -> key + "=" +
                slots.get(key).stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList()))
                .collect(Collectors.joining(", ")) +
                ")";
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ListMultimap<String, Object> getSlots() {
        return slots;
    }

    public void setSlots(ListMultimap<String, Object> slots) {
        this.slots = slots;
    }
}
