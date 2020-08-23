package ai.hual.labrador.dm;

/**
 * A Serializable DMModel.
 * Created by Dai Wentao on 2017/7/6.
 */
public interface SerializableDMModel extends DMModel {

    /**
     * Serialize the model into byte arrays.
     *
     * @return serialized dm model as a byte array.
     */
    byte[] serialize();

}
