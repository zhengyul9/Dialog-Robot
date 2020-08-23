package ai.hual.labrador.utils;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Created by ethan on 17-7-14.
 */
public class DateUtils {

    /**
     * <p><tt>type</tt> is in {@link ai.hual.labrador.utils.DateType}, date unit which
     * is at higher level of <tt>type</tt> is set meaningfully,
     * that is, it's not 0.
     */
    public static class Date implements Comparable<Date> {

        public DateType type;
        public int century;
        public int year;
        public int season;
        public int month;
        public int tendays;
        public int week;
        public int weekday;
        public int day;         // month day

        public Date() {
            this(0, 0, 0);
        }

        public Date(int y, int m, int d) {
            this.year = y;
            this.month = m;
            this.week = 0;
            this.day = d;
        }

        public Date(DateType type, int y, int m, int d) {
            this.type = type;
            this.year = y;
            this.month = m;
            this.week = 0;
            this.day = d;
        }

        /**
         * Add date duration to a date.
         *
         * @param duration duration
         * @return deviated duration
         */
        public Date addDuration(DateDurationUtils.Duration duration) {
            LocalDate localDate = LocalDate.of(year, month, day);
            switch (duration.type) {
                case DAY:
                    localDate = localDate.plusDays(duration.length);
                    break;
                case WEEK:
                    localDate = localDate.plusDays(duration.length * 7);
                    break;
                case MONTH:
                    localDate = localDate.plusMonths(duration.length);
                    break;
                case YEAR:
                    localDate = localDate.plusYears(duration.length);
                    break;
                default:
                    localDate = localDate.plusDays(duration.length);
            }
            return new Date(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        }

        @Override
        public int compareTo(Date other) {
            if (other.century * century != 0 && other.century != century)
                return century - other.century;
            else if (other.year * year != 0 && other.year != year)
                return year - other.year;
            else if (other.season * season != 0 && other.season != season)
                return season - other.season;
            else if (other.month * month != 0 && other.month != month)
                return month - other.month;
            else if (other.tendays * tendays != 0 && other.tendays != tendays)
                return tendays - other.tendays;
            else if (other.week * week != 0 && other.week != week)
                return week - other.week;
            else if (other.weekday * weekday != 0 && other.weekday != weekday)
                return weekday - other.weekday;
            else if (other.day * day != 0 && other.day != day)
                return day - other.day;
            else
                return 0;
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) return true;
            if (!(o instanceof Date)) return false;
            Date other = (Date) o;
            return this.year == other.year && this.month == other.month
                    && this.week == other.week && this.day == other.day &&
                    ((this.type != null && other.type != null) && this.type.equals(other.type));
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        @Override
        public String toString() {

            String yearStr = Integer.toString(year);
            String monthStr = Integer.toString(month);
            String dayStr = Integer.toString(day);

            String[] dateArray = {yearStr, monthStr, dayStr};

            IntStream.range(0, dateArray.length).forEach(i -> {
                if (dateArray[i].length() < 2)
                    dateArray[i] = "0" + dateArray[i];
            });

            return dateArray[0] + "-" + dateArray[1] + "-" + dateArray[2];
        }
    }

    /**
     * Complete date with current date.
     *
     * @param date Date that might be uncompleted, e.g 0-10-12, 0-0-10
     * @return a completed date
     */
    public static Date completeDateWithCurrent(Date date) {
        LocalDate localDate = LocalDate.now();
        // overwrite updated info
        if (date.year == 0)
            date.year = localDate.getYear();
        if (date.month == 0)
            date.month = localDate.getMonthValue();
        if (date.day == 0) {
            date.day = localDate.getDayOfMonth();
        }
        return date;
    }


    /**
     * Standard date expression to {@link ai.hual.labrador.utils.DateUtils.Date}.
     *
     * @param dateStr date in string, e.g. 2018-01-01
     * @return Date object if <code>dateStr</code> is in standard form.
     */
    public static Date strToDate(String dateStr) {
        String dateRegex = "(\\d+)-(\\d+)-(\\d+)";
        Pattern pattern = Pattern.compile(dateRegex);
        Matcher matcher = pattern.matcher(dateStr);
        if (matcher.find()) {
            int year, month, day;
            String yearStr = matcher.group(1);
            String monthStr = matcher.group(2);
            String dayStr = matcher.group(3);
            year = Integer.parseInt(yearStr);
            month = Integer.parseInt(monthStr);
            day = Integer.parseInt(dayStr);
            return new Date(year, month, day);
        } else
            return null;
    }

    public static int getTendays(LocalDate localdate) {

        int tendays;

        if (localdate.getDayOfMonth() == 31) {
            tendays = 3;
        } else {
            tendays = (localdate.getDayOfMonth() - 1) % 10 + 1;
        }

        return tendays;

    }

    public static int getSeason(LocalDate localdate) {

        int season;

        season = (localdate.getMonthValue() - 1) / 3 + 1;

        return season;

    }

    public static Date yDate(int y) {

        Date date = new Date();

        date.type = DateType.YEAR;

        if (y >= 1700) {
            date.year = y;

        } else if (y <= 50) {
            date.year = y + 2000;
        } else if (y < 100) {
            date.year = 1900 + y;
        } else {
            date.year = y;
        }
        return date;
    }

    /**
     * Deal with consecutive chinese digit char, e.g "四五天"， "九五年"， "一两个"
     * tell the type of digits, then parse it.
     *
     * @param yearNumber string of number and next char
     * @return @code{int} if vague expression, @code{date} if date expression
     */
    public static Object yStrDate(String yearNumber) {

        Date date = new Date();

        date.type = DateType.YEAR;

        String numberString;
        Character lastChar = yearNumber.charAt(yearNumber.length() - 1);
        numberString = yearNumber.substring(0, yearNumber.length() - 1);
        int number;
        if (lastChar == '年' || lastChar == '的') {   // year number
            number = DigitUtils.getYearDigits(numberString);
            if (number == 0)
                return null;    // unreasonable year expression
            date.year = number;
            return date;
        } else  // unreasonable input
            return null;
    }

    public static Date thisYearMDate(int m) {

        LocalDate currentDate = LocalDate.now();
        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = currentDate.getYear();
        date.month = m;

        return date;
    }

    public static Date mDate(int m) {

        Date date = new Date();

        date.type = DateType.MONTH;

        date.month = m;

        return date;
    }

    public static Date dDate(int d) {

        Date date = new Date();

        date.type = DateType.DAY;

        date.day = d;

        return date;
    }

    public static Date mdDate(int m, int d) {

        Date date = new Date();

        date.type = DateType.DAY;

        if (m >= 1 && m <= 12 && d >= 1 && d <= 31) {

            date.year = 0;
            date.month = m;
            date.day = d;
            return date;

        } else {

            return null;
        }
    }

    public static Date ymDate(int y, int m) {

        Date date = new Date();

        date.type = DateType.DAY;

        if (m < 1 || m > 12 || y < 1)
            return null;
        else {

            if (y > 50 && y < 100)  // 75年 -> 1975年
                date.year = 1900 + y;
            else if (y >= 10 && y <= 50)    // 17年 -> 2017年
                date.year = 2000 + y;
            else if (y >= 1700)
                date.year = y;
            else    // 5年 -> null
                return null;
        }
        date.month = m;

        return date;
    }

    public static Date ymStrDate(String ymStr, int m) {

        Date date = new Date();

        int i;

        date.type = DateType.MONTH;

        String numberString = null;

        for (i = 0; i < ymStr.length(); i++) {
            if (ymStr.charAt(i) == '年') {
                numberString = ymStr.substring(0, i);
                break;
            }
        }

        date.year = DigitUtils.getYearDigits(Objects.requireNonNull(numberString));
        date.month = m;

        return date;
    }

    public static Date ymdDate(int y, int m, int d) {

        Date date = new Date();

        date.type = DateType.DAY;

        if (m < 1 || m > 12 || y < 1)
            return null;
        else {

            if (y > 50 && y < 100)  // 75年 -> 1975年
                date.year = 1900 + y;
            else if (y >= 10 && y <= 50)    // 17年 -> 2017年
                date.year = 2000 + y;
            else if (y > 1000)  // 1800年 -> 1800年
                date.year = y;
            else    // 5年 -> null
                return null;
        }
        date.month = m;
        date.day = d;

        return date;
    }

    public static Date ymdStrDate(String ymdStr, int m, int d) {

        Date date = new Date();

        int i;

        date.type = DateType.DAY;

        String numberString = null;

        for (i = 0; i < ymdStr.length(); i++) {
            if (ymdStr.charAt(i) == '年') {
                numberString = ymdStr.substring(0, i);
                break;
            }
        }

        date.year = DigitUtils.getYearDigits(Objects.requireNonNull(numberString));
        date.month = m;
        date.day = d;

        return date;
    }

    public static Date thisDay() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();
        date.day = currentDate.getDayOfMonth();

        return date;
    }

    public static Date prevDayOne() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusDays(1);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date prevDayTwo() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusDays(2);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date prevDayThree() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusDays(3);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date nextDayOne() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(1);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date nextDayTwo() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(2);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date nextDayThree() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(3);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date thisWeekday(int weekday) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(weekday - currentDate.getDayOfWeek().getValue());

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();
        date.weekday = weekday;

        return date;
    }

    public static Date thisSunday() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(7 - currentDate.getDayOfWeek().getValue());

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();
        date.weekday = 7;

        return date;
    }

    public static Date prevWeekday(int weekday) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(weekday - currentDate.getDayOfWeek().getValue() - 7);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();
        date.weekday = weekday;

        return date;
    }

    public static Date prevSunday() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(-currentDate.getDayOfWeek().getValue());

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();
        date.weekday = 7;

        return date;
    }

    public static Date nextWeekday(int weekday) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(weekday - currentDate.getDayOfWeek().getValue() + 7);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();
        date.weekday = weekday;

        return date;
    }

    public static Date nextSunday() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(14 - currentDate.getDayOfWeek().getValue());

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();
        date.weekday = 7;

        return date;
    }

    public static Date thisWeek() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.WEEK;

        date.year = currentDate.getYear();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        date.week = currentDate.get(weekFields.weekOfWeekBasedYear());

        return date;
    }

    public static Date prevWeek() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusWeeks(1);

        Date date = new Date();

        date.type = DateType.WEEK;

        date.year = expectDate.getYear();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        date.week = expectDate.get(weekFields.weekOfWeekBasedYear());

        return date;
    }

    public static Date nextWeek() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusWeeks(1);

        Date date = new Date();

        date.type = DateType.WEEK;

        date.year = expectDate.getYear();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        date.week = expectDate.get(weekFields.weekOfWeekBasedYear());

        return date;
    }

    public static Date thisTendays() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();
        date.tendays = getTendays(currentDate);

        return date;
    }

    public static Date firstTendays() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();
        date.tendays = 1;

        return date;
    }

    public static Date midTendays() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();
        date.tendays = 2;

        return date;
    }

    public static Date lastTendays() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();
        date.tendays = 3;

        return date;
    }

    public static Date thisMonth() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();

        return date;
    }

    public static Date thisMonthDay(int d) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();
        date.day = d;

        return date;
    }

    public static Date prevMonth() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusMonths(1);

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();

        return date;
    }

    public static Date prevMonthDay(int d) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusMonths(1);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = d;

        return date;
    }

    public static Date prevMonthFirstTd() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusMonths(1);

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.tendays = 1;

        return date;
    }

    public static Date prevMonthMidTd() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusMonths(1);

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.tendays = 2;

        return date;
    }

    public static Date prevMonthLastTd() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusMonths(1);

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.tendays = 3;

        return date;
    }

    public static Date nextMonth() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusMonths(1);

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();

        return date;
    }

    public static Date nextMonthDay(int d) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusMonths(1);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = d;

        return date;
    }

    public static Date nextMonthFirstTd() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusMonths(1);

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.tendays = 1;

        return date;
    }

    public static Date nextMonthMidTd() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusMonths(1);

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.tendays = 2;

        return date;
    }

    public static Date nextMonthLastTd() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusMonths(1);

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.tendays = 3;

        return date;
    }

    public static Date monthFirstTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear();
        date.month = m;
        date.tendays = 1;

        return date;
    }

    public static Date monthMidTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear();
        date.month = m;
        date.tendays = 2;

        return date;
    }

    public static Date monthLastTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear();
        date.month = m;
        date.tendays = 3;

        return date;
    }

    public static Date thisSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear();
        date.season = getSeason(currentDate);

        return date;
    }

    public static Date prevSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear();
        date.season = thisSeason().season - 1;

        return date;
    }

    public static Date nextSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();
        date.season = thisSeason().season + 1;

        return date;
    }

    public static Date thisYear() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear();

        return date;
    }

    public static Date prevYearOne() {

        LocalDate currentDate = LocalDate.now();


        Date date = new Date();
        date.year = currentDate.getYear() - 1;
        date.type = DateType.YEAR;

        return date;
    }

    public static Date prevYearOneThisSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() - 1;
        date.season = thisSeason().season;

        return date;
    }

    public static Date prevYearOnePrevSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() - 1;
        date.season = prevSeason().season;

        return date;
    }

    public static Date prevYearOneNextSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() - 1;
        date.season = nextSeason().season;

        return date;
    }

    public static Date prevYearOneMonth(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = currentDate.getYear() - 1;
        date.month = m;

        return date;
    }

    public static Date prevYearOneMonthFirstTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() - 1;
        date.month = m;
        date.tendays = 1;

        return date;
    }

    public static Date prevYearOneMonthMidTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() - 1;
        date.month = m;
        date.tendays = 2;

        return date;
    }

    public static Date prevYearOneMonthLastTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() - 1;
        date.month = m;
        date.tendays = 3;

        return date;
    }

    public static Date prevYearOneMonthDay(int m, int d) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() - 1;
        date.month = m;
        date.day = d;

        return date;
    }

    public static Date prevYearOneThisDay() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() - 1;
        date.month = currentDate.getMonthValue();
        date.day = currentDate.getDayOfMonth();

        return date;
    }

    public static Date prevYearTwo() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() - 2;

        return date;
    }

    public static Date prevYearTwoThisSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() - 2;
        date.season = thisSeason().season;

        return date;
    }

    public static Date prevYearTwoPrevSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() - 2;
        date.season = prevSeason().season;

        return date;
    }

    public static Date prevYearTwoNextSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() - 2;
        date.season = nextSeason().season;

        return date;
    }

    public static Date prevYearTwoMonth(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = currentDate.getYear() - 2;
        date.month = m;

        return date;
    }

    public static Date prevYearTwoMonthFirstTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() - 2;
        date.month = m;
        date.tendays = 1;

        return date;
    }

    public static Date prevYearTwoMonthMidTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() - 2;
        date.month = m;
        date.tendays = 2;

        return date;
    }

    public static Date prevYearTwoMonthLastTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() - 2;
        date.month = m;
        date.tendays = 3;

        return date;
    }

    public static Date prevYearTwoMonthDay(int m, int d) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() - 2;
        date.month = m;
        date.day = d;

        return date;
    }

    public static Date prevYearTwoThisDay() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() - 2;
        date.month = currentDate.getMonthValue();
        date.day = currentDate.getDayOfMonth();

        return date;
    }

    public static Date prevYearThree() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() - 3;

        return date;
    }


    public static Date prevYearThreeThisSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() - 3;
        date.season = thisSeason().season;

        return date;
    }

    public static Date prevYearThreePrevSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() - 3;
        date.season = prevSeason().season;

        return date;
    }

    public static Date prevYearThreeNextSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() - 3;
        date.season = nextSeason().season;

        return date;
    }

    public static Date prevYearThreeMonth(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = currentDate.getYear() - 3;
        date.month = m;

        return date;
    }

    public static Date prevYearThreeMonthFirstTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() - 3;
        date.month = m;
        date.tendays = 1;

        return date;
    }

    public static Date prevYearThreeMonthMidTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() - 3;
        date.month = m;
        date.tendays = 2;

        return date;
    }

    public static Date prevYearThreeMonthLastTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() - 3;
        date.month = m;
        date.tendays = 3;

        return date;
    }

    public static Date prevYearThreeMonthDay(int m, int d) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() - 3;
        date.month = m;
        date.day = d;

        return date;
    }

    public static Date prevYearThreeThisDay() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() - 3;
        date.month = currentDate.getMonthValue();
        date.day = currentDate.getDayOfMonth();

        return date;
    }

    public static Date nextYearOne() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();
        date.year = currentDate.getYear() + 1;
        date.type = DateType.YEAR;

        return date;
    }

    public static Date nextYearOneThisSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() + 1;
        date.season = thisSeason().season;

        return date;
    }

    public static Date nextYearOnePrevSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() + 1;
        date.season = nextSeason().season;

        return date;
    }

    public static Date nextYearOneNextSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() + 1;
        date.season = nextSeason().season;

        return date;
    }

    public static Date nextYearOneMonth(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = currentDate.getYear() + 1;
        date.month = m;

        return date;
    }

    public static Date nextYearOneMonthFirstTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() + 1;
        date.month = m;
        date.tendays = 1;

        return date;
    }

    public static Date nextYearOneMonthMidTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() + 1;
        date.month = m;
        date.tendays = 2;

        return date;
    }

    public static Date nextYearOneMonthLastTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() + 1;
        date.month = m;
        date.tendays = 3;

        return date;
    }

    public static Date nextYearOneMonthDay(int m, int d) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() + 1;
        date.month = m;
        date.day = d;

        return date;
    }

    public static Date nextYearOneThisDay() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() + 1;
        date.month = currentDate.getMonthValue();
        date.day = currentDate.getDayOfMonth();

        return date;
    }

    public static Date nextYearTwo() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() + 2;

        return date;
    }

    public static Date nextYearTwoThisSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() + 2;
        date.season = thisSeason().season;

        return date;
    }

    public static Date nextYearTwoPrevSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() + 2;
        date.season = nextSeason().season;

        return date;
    }

    public static Date nextYearTwoNextSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() + 2;
        date.season = nextSeason().season;

        return date;
    }

    public static Date nextYearTwoMonth(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = currentDate.getYear() + 2;
        date.month = m;

        return date;
    }

    public static Date nextYearTwoMonthFirstTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() + 2;
        date.month = m;
        date.tendays = 1;

        return date;
    }

    public static Date nextYearTwoMonthMidTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() + 2;
        date.month = m;
        date.tendays = 2;

        return date;
    }

    public static Date nextYearTwoMonthLastTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() + 2;
        date.month = m;
        date.tendays = 3;

        return date;
    }

    public static Date nextYearTwoMonthDay(int m, int d) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() + 2;
        date.month = m;
        date.day = d;

        return date;
    }

    public static Date nextYearTwoThisDay() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() + 2;
        date.month = currentDate.getMonthValue();
        date.day = currentDate.getDayOfMonth();

        return date;
    }

    public static Date nextYearThree() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() + 3;

        return date;
    }


    public static Date nextYearThreeThisSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() + 3;
        date.season = thisSeason().season;

        return date;
    }

    public static Date nextYearThreePrevSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() + 3;
        date.season = nextSeason().season;

        return date;
    }

    public static Date nextYearThreeNextSeason() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = currentDate.getYear() + 3;
        date.season = nextSeason().season;

        return date;
    }

    public static Date nextYearThreeMonth(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = currentDate.getYear() + 3;
        date.month = m;

        return date;
    }

    public static Date nextYearThreeMonthFirstTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() + 3;
        date.month = m;
        date.tendays = 1;

        return date;
    }

    public static Date nextYearThreeMonthMidTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() + 3;
        date.month = m;
        date.tendays = 2;

        return date;
    }

    public static Date nextYearThreeMonthLastTd(int m) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear() + 3;
        date.month = m;
        date.tendays = 3;

        return date;
    }

    public static Date nextYearThreeMonthDay(int m, int d) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() + 3;
        date.month = m;
        date.day = d;

        return date;
    }

    public static Date nextYearThreeThisDay() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = currentDate.getYear() + 3;
        date.month = currentDate.getMonthValue();
        date.day = currentDate.getDayOfMonth();

        return date;
    }

    public static Date thisCentury() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.CENTURY;

        date.century = currentDate.getYear() / 100 + 1;

        return date;
    }

    public static Date prevCentury() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.CENTURY;

        date.century = currentDate.getYear() / 100;

        return date;
    }

    public static Date nextCentury() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.CENTURY;

        date.century = currentDate.getYear() / 100 - 1;

        return date;
    }

    public static Date beforeCentury(int c) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.CENTURY;

        date.century = currentDate.getYear() / 100 + 1 - c;

        return date;
    }

    public static Date beforeCenturyHalf(int c) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() - 100 * c - 50;

        return date;
    }


    public static Date beforeCenturyHalf() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() - 50;

        return date;
    }

    public static Date beforeYear(int y) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() - y;

        return date;
    }

    public static Date beforeYearHalf(int y) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        LocalDate expectDate = currentDate.minusMonths(y * 12 + 6);
        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();

        return date;
    }

    public static Date beforeYearHalf() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        LocalDate expectDate = currentDate.minusMonths(6);
        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();

        return date;
    }

    public static Date beforeSeason(int s) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusMonths(3L * s);

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = expectDate.getYear();
        date.season = getSeason(expectDate);

        return date;
    }

    public static Date beforeMonth(int m) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusMonths(m);

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();

        return date;
    }

    public static Date beforeMonthHalf(int m) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusMonths(m).minusDays(15);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date beforeMonthHalf() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusDays(15);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date beforeTendays(int td) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();
        if (thisTendays().tendays != 1) { // ensure that it's not the first tendays of the month
            date.tendays = thisTendays().tendays - td;
        } else {
            return null;
        }

        return date;
    }

    public static Date beforeWeek(int w) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusWeeks(w);

        Date date = new Date();

        date.type = DateType.WEEK;

        date.year = expectDate.getYear();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        date.week = expectDate.get(weekFields.weekOfWeekBasedYear());

        return date;
    }

    public static Date beforeDay(int d) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.minusDays(d);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = currentDate.getDayOfMonth();

        return date;
    }

    public static Date afterCentury(int c) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.CENTURY;

        date.century = currentDate.getYear() / 100 + 1 + c;

        return date;
    }

    public static Date afterCenturyHalf(int c) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() + 100 * c + 50;

        return date;
    }

    public static Date afterCenturyHalf() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() + 50;

        return date;
    }

    public static Date afterYear(int y) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.YEAR;

        date.year = currentDate.getYear() + y;

        return date;
    }

    public static Date afterYearHalf(int y) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        LocalDate expectDate = currentDate.plusMonths(y * 12 + 6);
        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();

        return date;
    }

    public static Date afterYearHalf() {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.MONTH;

        LocalDate expectDate = currentDate.plusMonths(6);
        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();

        return date;
    }

    public static Date afterSeason(int s) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusMonths(3L * s);

        Date date = new Date();

        date.type = DateType.SEASON;

        date.year = expectDate.getYear();
        date.season = getSeason(expectDate);

        return date;
    }

    public static Date afterMonth(int m) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusMonths(m);

        Date date = new Date();

        date.type = DateType.MONTH;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();

        return date;
    }

    public static Date afterMonthHalf(int m) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusMonths(m).plusDays(15);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date afterMonthHalf() {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(15);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = expectDate.getDayOfMonth();

        return date;
    }

    public static Date afterTendays(int td) {

        LocalDate currentDate = LocalDate.now();

        Date date = new Date();

        date.type = DateType.TENDAYS;

        date.year = currentDate.getYear();
        date.month = currentDate.getMonthValue();
        if (thisTendays().tendays != 3) { // ensure that it's not the last tendays of the month
            date.tendays = thisTendays().tendays + td;
        } else {
            return null;
        }

        return date;
    }

    public static Date afterWeek(int w) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusWeeks(w);

        Date date = new Date();

        date.type = DateType.WEEK;

        date.year = expectDate.getYear();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        date.week = expectDate.get(weekFields.weekOfWeekBasedYear());

        return date;
    }

    public static Date afterDay(int d) {

        LocalDate currentDate = LocalDate.now();
        LocalDate expectDate = currentDate.plusDays(d);

        Date date = new Date();

        date.type = DateType.DAY;

        date.year = expectDate.getYear();
        date.month = expectDate.getMonthValue();
        date.day = currentDate.getDayOfMonth();

        return date;
    }
}
