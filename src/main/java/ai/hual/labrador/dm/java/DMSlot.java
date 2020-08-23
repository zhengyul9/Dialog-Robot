package ai.hual.labrador.dm.java;

import ai.hual.labrador.dialog.AccessorRepository;
import ai.hual.labrador.dm.ContextedString;
import ai.hual.labrador.dm.SlotUpdateStrategy;
import ai.hual.labrador.exceptions.DMException;

import java.util.Map;

public class DMSlot {

    /**
     * Slot name.
     */
    private String name;

    /**
     * Data type of this slot.
     */
    private String type;

    /**
     * The update strategy.
     */
    private SlotUpdateStrategy updateStrategy;

    public DMSlot(DMSlotConfig slotConfig, ClassLoader classLoader, AccessorRepository accessorRepository) {
        name = slotConfig.getName();
        type = slotConfig.getType();
        String updateStrategyName = slotConfig.getUpdateStrategy();
        if (updateStrategyName != null) {
            try {
                updateStrategy = (SlotUpdateStrategy) Class.forName(
                        updateStrategyName, true, classLoader).newInstance();
                Map<String, ContextedString> params = slotConfig.paramsAsMap();
                for (ContextedString contextedString : params.values()) {
                    if (contextedString != null && contextedString.getStr() != null) {
                        contextedString.setStr(contextedString.getStr().replace(SlotUpdateStrategy.SLOT_NAME, name));
                    }
                }
                updateStrategy.setUp(name, params, accessorRepository);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new DMException("Class " + updateStrategyName + " not found.");
            }
        } else {
            updateStrategy = null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SlotUpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(SlotUpdateStrategy updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
