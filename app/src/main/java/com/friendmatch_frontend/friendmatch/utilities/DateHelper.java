package com.friendmatch_frontend.friendmatch.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {
    private String date;
    private String shortPattern, longPattern;

    public DateHelper(String date) {
        this.date = date;
        this.shortPattern = "yyyy-MM-dd";
        this.longPattern = "dd MMM, yyyy";
    }

    public void setShortPattern(String shortPattern) {
        this.shortPattern = shortPattern;
    }

    public void setLongPattern(String longPattern) {
        this.longPattern = longPattern;
    }

    public String changeDateFormatLong() {
        String longDate = null;
        SimpleDateFormat shortFormat = new SimpleDateFormat(shortPattern, Locale.US);
        SimpleDateFormat longFormat = new SimpleDateFormat(longPattern, Locale.US);
        Date shortDate;
        try {
            shortDate = shortFormat.parse(date);
            longDate = longFormat.format(shortDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return longDate;
    }

    public String changeDateFormatShort() {
        String shortDate = null;
        SimpleDateFormat shortFormat = new SimpleDateFormat(shortPattern, Locale.US);
        SimpleDateFormat longFormat = new SimpleDateFormat(longPattern, Locale.US);
        Date longDate;
        try {
            longDate = longFormat.parse(date);
            shortDate = shortFormat.format(longDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return shortDate;
    }

}