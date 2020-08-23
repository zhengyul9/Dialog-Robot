package ai.hual.labrador.dm;

/**
 * A factory that makes {@link DMModel}.
 * Created by Dai Wentao on 2017/7/6.
 */
public interface DMModelFactory {

    /**
     * make {@link DMModel} from a bunch of byte arrays.
     *
     * @param data A byte array data containing DMModel
     * @return A {@link DMModel} made out of entries.
     */
    public SerializableDMModel make(byte[] data);

}
