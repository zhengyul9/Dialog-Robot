package ai.hual.labrador.dm;

import ai.hual.labrador.dialog.AccessorRepository;

import java.util.Properties;

/**
 * Dialog manager resource.
 * Created by Dai Wentao on 2017/6/28.
 * Updated by Dai Wentao on 2017/7/6. Changed to be an interface.
 */
public interface DMModel {

    /**
     * Make a DM instance out of the model.
     *
     * @return The DM made by this model.
     */
    DM make(AccessorRepository accessorRepository, Properties properties);

    /**
     * @return The type of this model
     */
    DMModelType getType();

}
