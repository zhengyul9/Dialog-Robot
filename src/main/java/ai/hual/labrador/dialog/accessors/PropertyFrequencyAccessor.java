package ai.hual.labrador.dialog.accessors;

import java.util.List;

public interface PropertyFrequencyAccessor {

    /**
     * beginDate:xxxx-xx-xx
     * endDate: xxxx-xx-xx
     */
    List<String> simQuestions(String entity, String beginDate, String endDate);

}