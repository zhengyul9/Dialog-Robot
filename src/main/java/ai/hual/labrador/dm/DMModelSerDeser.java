package ai.hual.labrador.dm;

import ai.hual.labrador.dm.java.ByteHSMDMModel;
import ai.hual.labrador.dm.java.ByteJarDMModel;
import ai.hual.labrador.dm.java.ByteJarDMModelFactory;
import ai.hual.labrador.exceptions.DMException;

import java.util.HashMap;
import java.util.Map;

/**
 * serialize {@link DMModel} to and deserialize {@link DMModel} from a byte array.
 * Created by Dai Wentao on 2017/7/6.
 */
public class DMModelSerDeser {

    /**
     * File name in zip for type file.
     */
    public static final String NAME_TYPE = "type";

    private Map<DMModelType, DMModelFactory> dmModelFactories;

    /**
     * Initialize with default factories.
     */
    public DMModelSerDeser() {
        dmModelFactories = new HashMap<>();
        dmModelFactories.put(DMModelType.JAVA, new ByteJarDMModelFactory());
    }

    /**
     * Initialize with given {@link DMModelFactory}s
     *
     * @param dmModelFactories Factories that converts byte entries to {@link DMModel}
     */
    public DMModelSerDeser(Map<DMModelType, DMModelFactory> dmModelFactories) {
        this.dmModelFactories = dmModelFactories;
    }

    /**
     * Deserialize dialog model with given byte array.
     *
     * @param data A model data with a {@link DMModel} in it.
     * @return The DialogModel deserialized from byte array.
     */
    public SerializableDMModel deserialize(DMModelType type, byte[] data) {
        switch (type) {
            case JAVA:
                return new ByteJarDMModel(data);
            case HSM:
                return new ByteHSMDMModel(data);
            default:
                return null;
        }
    }

    /**
     * Serialize the dm model into a byte array.
     *
     * @param dmModel The {@link DMModel} to be serialized.
     * @return A byte array that contains the dict model.
     */
    public byte[] serialize(DMModel dmModel) {
        if (!(dmModel instanceof SerializableDMModel)) {
            throw new DMException("DM model to be serialized is not serializable.");
        }
        return ((SerializableDMModel) dmModel).serialize();
    }

}
