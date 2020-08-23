package ai.hual.labrador.utils;

import ai.hual.labrador.utils.DateUtils.Date;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilsTest {

    @Test
    void compareYmdDateTest() {
        Date date1 = new Date(2017, 11, 26);
        Date date2 = new Date(2017, 11, 28);
        Date date3 = new Date(2017, 11, 28);
        Date date4 = new Date(2020, 11, 28);
        Date date5 = new Date(2020, 6, 28);

        int result1 = date1.compareTo(date2);
        int result2 = date2.compareTo(date3);
        int result3 = date4.compareTo(date3);
        int result4 = date4.compareTo(date5);

        assertEquals(-2, result1);
        assertEquals(0, result2);
        assertEquals(3, result3);
        assertEquals(5, result4);
    }

    @Test
    void toStringTest() {
        Date date = new Date(2017, 1, 1);
        String result = date.toString();
        assertEquals("2017-01-01", result);
    }
}