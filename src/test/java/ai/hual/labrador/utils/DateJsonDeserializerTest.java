package ai.hual.labrador.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateJsonDeserializerTest {

    @Test
    void deserializeTest() throws IOException {
        String json = "{\"type\":\"DAY\",\"century\":21,\"year\":2018,\"season\":1," +
                "\"month\":1,\"tendays\":3,\"week\":0,\"weekday\":2,\"day\":30}";

        DateUtils.Date date = new ObjectMapper().readValue(json, DateUtils.Date.class);

        assertEquals(DateType.DAY, date.type);
        assertEquals(21, date.century);
        assertEquals(2018, date.year);
        assertEquals(1, date.season);
        assertEquals(1, date.month);
        assertEquals(3, date.tendays);
        assertEquals(0, date.week);
        assertEquals(2, date.weekday);
        assertEquals(30, date.day);
    }
}