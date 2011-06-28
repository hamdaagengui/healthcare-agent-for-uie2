package uie2.exercise5;

import java.sql.Date;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

class MyDateFormat extends Format {
	 
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// create a simple date format that draws on the year portion of our timestamp.
    // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
    // for a full description of SimpleDateFormat.
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        long timestamp = ((Number) obj).longValue();
        Date date = new Date(timestamp);
        return dateFormat.format(date, toAppendTo, pos);
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return null;
    }
}