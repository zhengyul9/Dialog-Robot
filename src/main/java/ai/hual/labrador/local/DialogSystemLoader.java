package ai.hual.labrador.local;

import ai.hual.labrador.dialog.DialogSystem;

/**
 * A loader to load a dialog system.
 * Created by Dai Wentao on 2017/7/6.
 */
public interface DialogSystemLoader {

    /**
     * @return load a dialog system. either from cache or reload.
     * @throws Exception when fail loading
     */
    DialogSystem load() throws Exception;

}
