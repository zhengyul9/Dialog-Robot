package ai.hual.labrador.nlu.preprocessors.utils;

import ai.hual.labrador.nlu.SlotValue;

public class SlotIndexPair {
    private SlotValue slotValue;
    private int slotIndex;

    public SlotIndexPair(SlotValue slotValue, int slotIndex) {
        this.slotValue = slotValue;
        this.slotIndex = slotIndex;
    }

    public SlotValue getSlotValue() {
        return slotValue;
    }

    public void setSlotValue(SlotValue slotValue) {
        this.slotValue = slotValue;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }
}
