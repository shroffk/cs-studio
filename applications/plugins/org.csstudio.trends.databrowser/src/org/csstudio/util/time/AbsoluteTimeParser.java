package org.csstudio.util.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/** Parse an absolute date/time string.
 *  
 *  @see #parse(Calendar, String)
 *  
 *  @author Sergei Chevtsov developed the original code for the
 *          Java Archive Viewer, from which this code heavily borrows.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class AbsoluteTimeParser
{
    /** Constant to define 'now', i.e. the current wallclock date and time. */
    public static final String NOW = "now";
    
    /** The accepted date formats for absolute times. */
    @SuppressWarnings("nls")
    private static final DateFormat[] parsers = new SimpleDateFormat[]
    {   // Most complete version first
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS"),
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"),
        new SimpleDateFormat("yyyy/MM/dd HH:mm"),
        new SimpleDateFormat("yyyy/MM/dd HH"),
        new SimpleDateFormat("yyyy/MM/dd")
    };

    /** Like parse(), using a base calendar of 'now', 'current time zone'.
     *  @see #parse(Calendar, String)
     */
    public static Calendar parse(String text) throws Exception
    {
        Calendar cal = Calendar.getInstance();
        return parse(cal, text);
    }

    /** Adjust given calendar to the date and time parsed from the text.
     *  <p>
     *  The date/time text should follow the format
     *  <pre>
     *  YYYY/MM/DD hh:mm:ss.sss
     *  </pre>
     *  The milliseconds (sss), seconds(ss), minutes(mm), hours(hh)
     *  might be left off, and will then default to zero.
     *  <p>
     *  When omitting the year, the year from the passed-in calendar is used.
     *  When omitting the whole date (YYYY/MM/DD), the values from the passed-in
     *  calendar are used.
     *  It is not possible to provide <i>only</i> the month <i>without</i>
     *  the day or vice vesa.
     *  <p>
     *  An empty text leaves the provided calendar unchanged.
     *  <p>
     *  In addition, the special text "NOW" results in the current wallclock
     *  time.
     *  Unclear, if that's not really a relative time specification, because
     *  its value changes whenever evaluated...
     *  <p>
     *  All other cases result in an exception.
     *  
     *  @param cal Base calendar, defines the time zone as well as
     *             the year, in case the text doesn't include a year.
     *  @param text The text to parse.
     *  @return Adjusted calendar.
     *  @exception On error.
     */
    public static Calendar parse(Calendar cal, String text) throws Exception
    {
        String cooked = text.trim().toLowerCase();
        // Empty string? Pass cal as is back.
        if (cooked.length() < 1)
            return cal;
        // Handle NOW
        if (cooked.startsWith(NOW))
            return Calendar.getInstance();
        
        // Provide missing year from given cal
        int datesep = cooked.indexOf('/');
        if (datesep < 0) // No date at all provided? Use the one from cal.
            cooked = String.format("%04d/%02d/%02d %s",
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH),
                            cooked);
        else
        {   // Are there two date separators?
            datesep = cooked.indexOf('/', datesep + 1);
            // If not, assume that we have MM/DD, and add the YYYY.
            if (datesep < 0)
                cooked = String.format("%04d/%s",
                                cal.get(Calendar.YEAR),
                                cooked);
        }
        
        for (DateFormat parser : parsers)
        {
            // Try the parsers
            try
            {
                // DateFormat returns Date, but pretty much all of Date
                // is deprecated, so we convert to Calendar right away.
                cal.setTimeInMillis(parser.parse(cooked).getTime());
                return cal;
            }
            catch (Exception e)
            {
                // Ignore, try the next one
            }
        }
        // No parser parsed the string?
        throw new Exception("Cannot parse date and time from '" + text + "'");
    }
}
