package brigthcare_medical_centre.util;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the date formatting helpers in DateUtils.
 */
public class DateUtilsTest {

    private Date fixedDate() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2026, Calendar.JANUARY, 15, 10, 30, 45);
        return cal.getTime();
    }

    @Test
    public void testFormatDate() {
        assertEquals("2026-01-15", DateUtils.formatDate(fixedDate()));
    }

    @Test
    public void testFormatDateTime() {
        assertEquals("2026-01-15 10:30:45", DateUtils.formatDateTime(fixedDate()));
    }

    @Test
    public void testGetCurrentDateFormat() {
        String current = DateUtils.getCurrentDate();
        assertNotNull(current);
        assertTrue("Expected yyyy-MM-dd pattern", current.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testGetCurrentDateTimeFormat() {
        String current = DateUtils.getCurrentDateTime();
        assertNotNull(current);
        assertTrue("Expected yyyy-MM-dd HH:mm:ss pattern",
                current.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void testGetCurrentDateMatchesToday() {
        assertEquals(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                DateUtils.getCurrentDate());
    }
}