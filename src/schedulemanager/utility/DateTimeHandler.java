/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Tim Smith
 */
public class DateTimeHandler {

    /**
     * Date & time DateTimeFormatter pattern of "M/d/YYYY h:mm:ssa"
     */
    public static final DateTimeFormatter DATE_TIME_STAMP = DateTimeFormatter.ofPattern("M/d/YYYY h:mm:ssa");

    /**
     * Date & time DateTimeFormatter pattern of "M/d/YYYY h:mma"
     */
    public static final DateTimeFormatter FULL_DATE_TIME = DateTimeFormatter.ofPattern("M/d/YYYY h:mma");

    /**
     * Date DateTimeFormatter pattern of "M/d/YYYY"
     */
    public static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("M/d/YYYY");

    /**
     * Date & time DateTimeFormatter pattern of "MMMM d, YYYY"
     */
    public static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("MMMM d, YYYY");

    /**
     * Time only DateTimeFormatter pattern of "h:mma"
     */
    public static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("h:mma");

    /**
     * Time only DateTimeFormatter pattern of "h"
     */
    public static final DateTimeFormatter HOUR_ONLY = DateTimeFormatter.ofPattern("h");

    /**
     * Time only DateTimeFormatter pattern of "mm"
     */
    public static final DateTimeFormatter MINUTE_ONLY = DateTimeFormatter.ofPattern("mm");

    /**
     * Time only DateTimeFormatter pattern of "a"
     */
    public static final DateTimeFormatter AMPM_ONLY = DateTimeFormatter.ofPattern("a");

}
