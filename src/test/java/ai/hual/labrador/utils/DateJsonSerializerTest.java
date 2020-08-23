package ai.hual.labrador.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateJsonSerializerTest {

    @Test
    void testSerialize() throws JsonProcessingException {
        DateUtils.Date date = new DateUtils.Date(DateType.DAY, 2018, 1, 30);
        date.century = 21;
        date.season = 1;
        date.tendays = 3;
        date.weekday = 2;

        String expected = "{\"type\":\"DAY\",\"century\":21,\"year\":2018,\"season\":1," +
                "\"month\":1,\"tendays\":3,\"week\":0,\"weekday\":2,\"day\":30}";

        assertEquals(expected, new ObjectMapper().writeValueAsString(date));
    }
}