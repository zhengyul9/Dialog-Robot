package ai.hual.labrador.nlu;

import ai.hual.labrador.utils.DateType;
import ai.hual.labrador.utils.DateUtils;
import ai.hual.labrador.utils.DirectionalDateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotValueJsonSerializerTest {

    @Test
    void dateSlotSerializeTest() throws JsonProcessingException {
        SlotValue dateSlot = new SlotValue(new DateUtils.Date(DateType.DAY, 0, 10, 12), "日期",
                "date(m,d)", 8, 14, 3, 9);

        String expected = "{\"matched\":{\"type\":\"DAY\",\"century\":0,\"year\":0,\"season\":0,\"month\":10,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":12},\"type\":\"ai.hual.labrador.utils.DateUtils$Date\",\"label\":\"date(m,d)\",\"key\":\"日期\",\"realStart\":3,\"realEnd\":9,\"realLength\":6,\"regex\":null}";
        assertEquals(expected, new ObjectMapper().writeValueAsString(dateSlot));
    }

    @Test
    void directionalDateSlotSerializeTest() throws JsonProcessingException {
        SlotValue dirDateSlot = new SlotValue(new DirectionalDateUtils.DirectionalDate(
                new DateUtils.Date(DateType.DAY, 2018, 7, 20), null, 0),
                "日期起始", "startDate(date)", 15, 23, 9, 17);

        String expected = "{\"matched\":{\"type\":null,\"start\":{\"type\":\"DAY\",\"century\":0,\"year\":2018,\"season\":0,\"month\":7,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":20},\"end\":null,\"length\":0},\"type\":\"ai.hual.labrador.utils.DirectionalDateUtils$DirectionalDate\",\"label\":\"startDate(date)\",\"key\":\"日期起始\",\"realStart\":9,\"realEnd\":17,\"realLength\":8,\"regex\":null}";
        assertEquals(expected, new ObjectMapper().writeValueAsString(dirDateSlot));
    }
}