package ai.hual.labrador.nlu;

import ai.hual.labrador.utils.DateType;
import ai.hual.labrador.utils.DateUtils;
import ai.hual.labrador.utils.DirectionalDateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotValueJsonDeserializerTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void dateSlotDeserializeTest() throws IOException {
        String json = "{\"matched\":{\"type\":\"DAY\",\"century\":0,\"year\":0,\"season\":0,\"month\":10," +
                "\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":12},\"type\":\"ai.hual.labrador.utils.DateUtils$Date\"," +
                "\"label\":\"date(m,d)\"," + "\"key\":\"日期\",\"realStart\":3,\"realEnd\":9,\"realLength\":6}";

        SlotValue slotValue = mapper.readValue(json, SlotValue.class);
        assertTrue(slotValue.matched instanceof DateUtils.Date);
        DateUtils.Date date = (DateUtils.Date) slotValue.matched;
        assertEquals(DateType.DAY, date.type);
        assertEquals(0, date.year);
        assertEquals(10, date.month);
        assertEquals(12, date.day);
        assertEquals("date(m,d)", slotValue.label);
        assertEquals("日期", slotValue.key);
        assertEquals(3, slotValue.realStart);
        assertEquals(9, slotValue.realEnd);
        assertEquals(6, slotValue.realLength);
        assertEquals(0, slotValue.start);
        assertEquals(0, slotValue.end);
        assertEquals(0, slotValue.length);
    }

    @Test
    void directionalDateSlotDeserializeTest() throws IOException {
        String json = "{\"matched\":{\"type\":null,\"start\":{\"type\":\"DAY\",\"century\":0," +
                "\"year\":2018,\"season\":0,\"month\":7,\"tendays\":0,\"week\":0,\"weekday\":0,\"day\":20}," +
                "\"end\":null,\"length\":0},\"type\":\"ai.hual.labrador.utils.DirectionalDateUtils$DirectionalDate\"," +
                "\"label\":\"startDate(date)\"," + "\"key\":\"日期起始\",\"realStart\":9,\"realEnd\":17,\"realLength\":8}";

        SlotValue slotValue = mapper.readValue(json, SlotValue.class);
        assertTrue(slotValue.matched instanceof DirectionalDateUtils.DirectionalDate);
        DirectionalDateUtils.DirectionalDate dirDate = (DirectionalDateUtils.DirectionalDate) slotValue.matched;
        assertTrue(dirDate.start != null);
        assertTrue(dirDate.end == null);
        assertEquals(null, dirDate.type);
        assertEquals(2018, dirDate.start.year);
        assertEquals(7, dirDate.start.month);
        assertEquals(20, dirDate.start.day);
        assertEquals("startDate(date)", slotValue.label);
        assertEquals("日期起始", slotValue.key);
        assertEquals(9, slotValue.realStart);
        assertEquals(17, slotValue.realEnd);
        assertEquals(8, slotValue.realLength);
        assertEquals(0, slotValue.start);
        assertEquals(0, slotValue.end);
        assertEquals(0, slotValue.length);
    }
}