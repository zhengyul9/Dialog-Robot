package ai.hual.labrador.utils;

import java.time.LocalTime;
import java.util.stream.IntStream;

/**
 * Created by ethan on 17-7-17.
 */
public class TimeUtils {

    /**
     * <p><tt>type</tt> is in {@link ai.hual.labrador.utils.TimeType}, time unit which
     * is at higher level of <tt>type</tt> is set meaningfully,
     * that is, it's not 0.
     */
    public static class Time {

        public TimeType type;
        public int hour;
        public int minute;
        public int second;

        public Time() {

            this(0, 0, 0);
        }

        public Time(int h, int min, int s) {

            this.hour = h;
            this.minute = min;
            this.second = s;
        }

        public Time(TimeType type, int h, int min, int s) {

            this.type = type;
            this.hour = h;
            this.minute = min;
            this.second = s;
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) return true;
            if (!(o instanceof Time)) return false;
            Time other = (Time) o;
            return this.hour == other.hour && this.minute == other.minute
                    && this.second == other.second && this.type.equals(other.type);
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        public String toString() {

            String hourStr = Integer.toString(hour);
            String minuteStr = Integer.toString(minute);
            String secondStr = Integer.toString(second);

            String[] timeArray = {hourStr, minuteStr, secondStr};

            IntStream.range(0, timeArray.length).forEach(i -> {
                if (timeArray[i].length() < 2)
                    timeArray[i] = "0" + timeArray[i];
            });

            return timeArray[0] + ":" + timeArray[1] + ":" + timeArray[2];
        }
    }

    public static Time hTime(int h) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;

        Time time = new Time();

        time.type = TimeType.HOUR;

        time.hour = h;

        return time;
    }

    public static Time mTime(int m) {

        if (m >= 60 || m < 0)    // unreasonable minute
            return null;

        Time time = new Time();

        time.type = TimeType.MINUTE;

        time.minute = m;

        return time;
    }

    public static Time sTime(int s) {

        if (s >= 60 || s < 0)    // unreasonable second
            return null;

        Time time = new Time();

        time.type = TimeType.SECOND;

        time.second = s;

        return time;
    }

    public static Time hHalfHour(int h) {
        if (h > 24 || h <= 0)    // unreasonable hour
            return null;

        Time time = new Time();

        time.type = TimeType.MINUTE;

        time.hour = h;
        time.minute = 30;

        return time;
    }

    public static Time hmTime(int h, int m) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (m >= 60 || m < 0)    // unreasonable minute
            return null;

        Time time = new Time();

        time.type = TimeType.MINUTE;

        time.hour = h;
        time.minute = m;

        return time;
    }

    public static Time hqTime(int h, int q) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (q >= 4 || q < 0)    // unreasonable quarter
            return null;

        Time time = new Time();

        time.type = TimeType.MINUTE;

        time.hour = h;
        time.minute = q * 15;

        return time;
    }

    public static Time hmsTime(int h, int m, int s) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (m >= 60 || m < 0)    // unreasonable minute
            return null;
        if (s >= 60 || s < 0)    // unreasonable second
            return null;

        Time time = new Time();

        time.type = TimeType.SECOND;

        time.hour = h;
        time.minute = m;
        time.second = s;

        return time;
    }

    public static Time hTimeAm(int h) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;

        Time time = new Time();

        time.type = TimeType.HOUR;

        if (h > 12) // reassure it's hour of am
            h = h - 12;

        time.hour = h;

        return time;
    }

    public static Time hTimePm(int h) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;

        Time time = new Time();

        time.type = TimeType.HOUR;

        if (h < 12) // reassure it's hour of pm
            h = h + 12;

        time.hour = h;

        return time;
    }

    public static Time hHalfHourAm(int h) {
        if (h > 24 || h <= 0)    // unreasonable hour
            return null;

        Time time = new Time();
        time.type = TimeType.MINUTE;
        if (h > 12) // reassure it's hour of am
            h = h - 12;

        time.hour = h;
        time.minute = 30;

        return time;
    }

    public static Time hHalfHourPm(int h) {
        if (h > 24 || h <= 0)    // unreasonable hour
            return null;

        Time time = new Time();
        time.type = TimeType.MINUTE;
        if (h < 12) // reassure it's hour of pm
            h = h + 12;

        time.hour = h;
        time.minute = 30;

        return time;
    }

    public static Time hmTimeAm(int h, int m) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (m >= 60 || m < 0)    // unreasonable minute
            return null;

        Time time = new Time();

        time.type = TimeType.MINUTE;

        if (h > 12) // reassure it's hour of am
            h = h - 12;

        time.hour = h;
        time.minute = m;

        return time;
    }

    public static Time hmTimePm(int h, int m) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (m >= 60 || m < 0)    // unreasonable minute
            return null;

        Time time = new Time();

        time.type = TimeType.MINUTE;

        if (h < 12) // reassure it's hour of pm
            h = h + 12;

        time.hour = h;
        time.minute = m;

        return time;
    }

    public static Time hTimeNight(int h) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;

        Time time = new Time();

        time.type = TimeType.MINUTE;

        if (h >= 11 && h <= 12) // before 24:00
            h += 12;

        time.hour = h;

        return time;
    }

    public static Time hmTimeNight(int h, int m) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (m >= 60 || m < 0)    // unreasonable minute
            return null;
        if (h == 12 && m > 0)   // unreasonable combination
            return null;

        Time time = new Time();

        time.type = TimeType.MINUTE;

        if (h >= 11 && h <= 12) // before 24:00
            h += 12;

        time.hour = h;
        time.minute = m;

        return time;
    }

    public static Time hqTimeAm(int h, int q) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (q >= 4 || q < 0)    // unreasonable quarter
            return null;

        Time time = new Time();

        time.type = TimeType.MINUTE;

        if (h > 12) // reassure it's hour of am
            h = h - 12;

        time.hour = h;
        time.minute = q * 15;

        return time;
    }

    public static Time hqTimePm(int h, int q) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (q >= 4 || q < 0)    // unreasonable quarter
            return null;

        Time time = new Time();

        if (h < 12) // reassure it's hour of pm
            h = h + 12;

        time.type = TimeType.MINUTE;

        time.hour = h;
        time.minute = q * 15;

        return time;
    }


    public static Time hqTimeNight(int h, int q) {

        if (h > 12 || h <= 0)    // unreasonable hour
            return null;
        if (q >= 4 || q < 0)    // unreasonable quarter
            return null;
        if (h == 12 && q > 0)   // unreasonable combination
            return null;

        Time time = new Time();

        if (h >= 11) // before 24:00
            h += 12;

        time.type = TimeType.MINUTE;

        time.hour = h;
        time.minute = q * 15;   // 1 quarter equals 15 minutes

        return time;
    }

    public static Time hmsTimeAm(int h, int m, int s) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (m >= 60 || m < 0)    // unreasonable minute
            return null;
        if (s >= 60 || s < 0)    // unreasonable second
            return null;

        Time time = new Time();

        time.type = TimeType.SECOND;

        if (h > 12) // reassure it's hour of am
            h = h - 12;

        time.hour = h;
        time.minute = m;
        time.second = s;

        return time;
    }

    public static Time hmsTimePm(int h, int m, int s) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (m >= 60 || m < 0)    // unreasonable minute
            return null;
        if (s >= 60 || s < 0)    // unreasonable second
            return null;

        Time time = new Time();

        time.type = TimeType.SECOND;

        if (h < 12) // reassure it's hour of pm
            h = h + 12;

        time.hour = h;
        time.minute = m;
        time.second = s;

        return time;
    }

    public static Time hmsTimeNight(int h, int m, int s) {

        if (h > 24 || h <= 0)    // unreasonable hour
            return null;
        if (m >= 60 || m < 0)    // unreasonable minute
            return null;
        if (s >= 60 || s < 0)    // unreasonable second
            return null;

        Time time = new Time();

        time.type = TimeType.SECOND;

        if (h >= 11 && h <= 12) // before 24:00
            h += 12;

        time.hour = h;
        time.minute = m;
        time.second = s;

        return time;
    }

    public static Time thisTime() {

        Time time = new Time();

        LocalTime currentTime = LocalTime.now();

        time.type = TimeType.SECOND;

        time.hour = currentTime.getHour();
        time.minute = currentTime.getMinute();
        time.second = currentTime.getSecond();

        return time;
    }

    public static Time thisHour() {

        Time time = new Time();

        time.type = TimeType.HOUR;

        LocalTime currentTime = LocalTime.now();

        time.hour = currentTime.getHour();

        return time;
    }

    public static Time prevHourOne() {

        Time time = new Time();

        time.type = TimeType.HOUR;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusHours(1);

        time.hour = expectTime.getHour();

        return time;
    }

    public static Time nextHourOne() {

        Time time = new Time();

        time.type = TimeType.HOUR;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusHours(1);

        time.hour = expectTime.getHour();

        return time;
    }

    public static Time beforeDayHalf() {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusHours(12);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time beforeDayHalf(int d) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusHours(24 * d + 12);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time beforeHour(int h) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusHours(h);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time beforeHourHalf(int h) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusMinutes(h * 60 + 30);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time beforeHourHalf() {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusMinutes(30);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time beforeQuarter(int q) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusMinutes(q * 15L);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time beforeMin(int m) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusMinutes(m);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time beforeMinHalf(int m) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusSeconds(m * 60 + 30);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time beforeMinHalf() {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusSeconds(30);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time beforeSec(int s) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.minusSeconds(s);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterDayHalf() {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusHours(12);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterDayHalf(int d) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusHours(d * 24 + 12);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterHour(int h) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusHours(h);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterHourHalf(int h) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusMinutes(h * 60 + 30);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterHourHalf() {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusMinutes(30);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterQuarter(int q) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusMinutes(q * 15L);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterMin(int m) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusMinutes(m);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterMinHalf(int m) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusSeconds(m * 60 + 30);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterMinHalf() {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusSeconds(30);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static Time afterSec(int s) {

        Time time = new Time();

        time.type = TimeType.SECOND;

        LocalTime currentTime = LocalTime.now();
        LocalTime expectTime = currentTime.plusSeconds(s);

        time.hour = expectTime.getHour();
        time.minute = expectTime.getMinute();
        time.second = expectTime.getSecond();

        return time;
    }

    public static void main(String[] args) {
    }
}
