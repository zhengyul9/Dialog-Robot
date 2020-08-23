package ai.hual.labrador.utils;

import ai.hual.labrador.utils.DateUtils.Date;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static ai.hual.labrador.utils.DateUtils.thisDay;
import static ai.hual.labrador.utils.DateUtils.thisMonth;
import static ai.hual.labrador.utils.DateUtils.thisSeason;
import static ai.hual.labrador.utils.DateUtils.thisTendays;
import static ai.hual.labrador.utils.DateUtils.thisWeek;
import static ai.hual.labrador.utils.DateUtils.thisYear;

/**
 * Created by Veronica on 17/7/21.
 */
public class DirectionalDateUtils {

    /**
     * <p><tt>type</tt> is in {@link ai.hual.labrador.utils.DateType}. <tt>length</tt>
     * indicates how long the date period lasts, if it's 0, then
     * this is a ray period, which has only one defined end.
     * <tt>start</tt> and <tt>end</tt> are {@link ai.hual.labrador.utils.DateUtils.Date}
     * object.
     */
    @JsonSerialize
    public static class DirectionalDate {
        public DateType type;   // time unit
        @JsonSerialize
        public Date start;   // start point of this directional date
        @JsonSerialize
        public Date end;     // end point of this directional date
        public int length;  // length of duration

        public DirectionalDate() {
            this(null, null, 0);
        }

        public DirectionalDate(Date start, Date end, int length) {
            this.length = length;
            this.start = start;
            this.end = end;
        }

        public DirectionalDate(DateType type, Date start, Date end, int length) {
            this.type = type;
            this.length = length;
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof DirectionalDate)) return false;
            DirectionalDate other = (DirectionalDate) o;
            return this.length == other.length && this.type == other.type &&
                    this.start.equals(other.start) && this.end.equals(other.end);
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        public String toString() {
            if (start != null) { // start defined
                return start.toString();
            } else if (end != null) {   // end defined
                return end.toString();
            } else {
                return "Something wrong with this directional date";
            }
        }
    }

    public static DirectionalDate endDate(Date date) {
        DirectionalDate directionalDate = new DirectionalDate();

        directionalDate.end = date;

        return directionalDate;
    }

    public static DirectionalDate startDate(Date date) {
        DirectionalDate directionalDate = new DirectionalDate();

        directionalDate.start = date;

        return directionalDate;
    }

    public static DirectionalDate nextDateDuration(DateDurationUtils.Duration duration) {
        DirectionalDate directionalDate = new DirectionalDate();

        directionalDate.type = duration.type;
        directionalDate.length = duration.length;

        if (directionalDate.type == DateType.DAY) {
            directionalDate.start = thisDay();
        }

        if (directionalDate.type == DateType.WEEK) {
            directionalDate.start = thisWeek();
        }

        if (directionalDate.type == DateType.TENDAYS) {
            directionalDate.start = thisTendays();
        }

        if (directionalDate.type == DateType.MONTH) {
            directionalDate.start = thisMonth();
        }

        if (directionalDate.type == DateType.SEASON) {
            directionalDate.start = thisSeason();
        }

        if (directionalDate.type == DateType.YEAR) {
            directionalDate.start = thisYear();
        }

        return directionalDate;
    }


    public static DirectionalDate nextDateDuration(Date date, DateDurationUtils.Duration duration) {
        DirectionalDate directionalDate = new DirectionalDate();

        directionalDate.type = duration.type;
        directionalDate.start = date;
        directionalDate.length = duration.length;

        return directionalDate;
    }

    public static DirectionalDate prevDateDuration(DateDurationUtils.Duration duration) {
        DirectionalDate directionalDate = new DirectionalDate();

        directionalDate.type = duration.type;
        directionalDate.length = duration.length;

        if (directionalDate.type == DateType.DAY) {
            directionalDate.end = thisDay();
        }

        if (directionalDate.type == DateType.WEEK) {
            directionalDate.end = thisWeek();
        }

        if (directionalDate.type == DateType.TENDAYS) {
            directionalDate.end = thisTendays();
        }

        if (directionalDate.type == DateType.MONTH) {
            directionalDate.end = thisMonth();
        }

        if (directionalDate.type == DateType.SEASON) {
            directionalDate.end = thisSeason();
        }

        if (directionalDate.type == DateType.YEAR) {
            directionalDate.end = thisYear();
        }

        return directionalDate;
    }

    public static DirectionalDate prevDateDuration(Date date, DateDurationUtils.Duration duration) {
        DirectionalDate directionalDate = new DirectionalDate();

        directionalDate.type = duration.type;
        directionalDate.end = date;
        directionalDate.length = duration.length;

        return directionalDate;
    }

    public static void main() {
    }
}
