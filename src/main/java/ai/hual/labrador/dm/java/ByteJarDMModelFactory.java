package ai.hual.labrador.dm.java;

import ai.hual.labrador.dm.DMModelFactory;
import ai.hual.labrador.dm.SerializableDMModel;

/**
 * A factory that makes {@link ByteJarDMModel}
 * Created by Dai Wentao on 2017/7/6.
 */
@Deprecated
public class ByteJarDMModelFactory implements DMModelFactory {


    @Override
    public SerializableDMModel make(byte[] data) {
        return new ByteJarDMModel(data);
    }
}
