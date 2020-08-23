package ai.hual.labrador.utils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalTime;

/**
 * Created by Veronica on 17/7/21.
 */
public class DirectionalTimeUtils {

    /**
     * <p><tt>type</tt> is in {@link ai.hual.labrador.utils.TimeType}. <tt>length</tt>
     * indicates how long the time period lasts, if it's 0, then
     * this is a ray period, which has only one defined end.
     * <tt>start</tt> and <tt>end</tt> are {@link ai.hual.labrador.utils.TimeUtils.Time}
     * object.
     */
    public static class DirectionalTime {
        @JsonSerialize
        TimeUtils.Time start;   // start point of this directional time
        @JsonSerialize
        TimeUtils.Time end;     // end point of this directional time
        public TimeType type;   // time unit of duration
        public int length;  // length of duration

        public DirectionalTime() {

            this(null, null, 0);
        }


        public DirectionalTime(TimeUtils.Time start, TimeUtils.Time end, int length) {

            this.start = start;
            this.end = end;
            this.length = length;
        }

        public DirectionalTime(TimeType type, TimeUtils.Time start, TimeUtils.Time end, int length) {

            this.type = type;
            this.start = start;
            this.end = end;
            this.length = length;
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) return true;
            if (!(o instanceof DirectionalTime)) return false;
            DirectionalTime other = (DirectionalTime) o;
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

    public static DirectionalTime beforeTime(TimeUtils.Time time) {

        DirectionalTime directionalTime = new DirectionalTime();

        directionalTime.end = time;

        return directionalTime;
    }

    public static DirectionalTime afterTime(TimeUtils.Time time) {

        DirectionalTime directionalTime = new DirectionalTime();

        directionalTime.start = time;

        return directionalTime;
    }

    public static DirectionalTime nextTimeDuration(TimeDurationUtils.Duration duration) {

        LocalTime currentTime = LocalTime.now();
        DirectionalTime directionalTime = new DirectionalTime();

        directionalTime.type = duration.type;
        directionalTime.length = duration.length;

        if (directionalTime.type == TimeType.HOUR) {
            directionalTime.start = TimeUtils.thisHour();
        }

        if (directionalTime.type == TimeType.QUARTER) {
            TimeUtils.Time startTime = new TimeUtils.Time();
            startTime.type = TimeType.MINUTE;
            startTime.hour = currentTime.getHour();
            startTime.minute = currentTime.getMinute();
            directionalTime.start = startTime;
        }

        if (directionalTime.type == TimeType.MINUTE) {
            TimeUtils.Time startTime = new TimeUtils.Time();
            startTime.type = TimeType.MINUTE;
            startTime.hour = currentTime.getHour();
            startTime.minute = currentTime.getMinute();
            directionalTime.start = startTime;
        }

        if (directionalTime.type == TimeType.SECOND) {
            directionalTime.start = TimeUtils.thisTime();
        }

        return directionalTime;
    }

    public static DirectionalTime nextTimeDuration(TimeUtils.Time time, TimeDurationUtils.Duration duration) {

        DirectionalTime directionalTime = new DirectionalTime();

        directionalTime.type = duration.type;
        directionalTime.start = time;
        directionalTime.length = duration.length;

        return directionalTime;
    }

    public static DirectionalTime prevTimeDuration(TimeDurationUtils.Duration duration) {

        LocalTime currentTime = LocalTime.now();
        DirectionalTime directionalTime = new DirectionalTime();

        directionalTime.type = duration.type;
        directionalTime.length = duration.length;

        if (directionalTime.type == TimeType.HOUR) {
            directionalTime.end = TimeUtils.thisHour();
        }

        if (directionalTime.type == TimeType.QUARTER) {
            TimeUtils.Time endTime = new TimeUtils.Time();
            endTime.type = TimeType.MINUTE;
            endTime.hour = currentTime.getHour();
            endTime.minute = currentTime.getMinute();
            directionalTime.end = endTime;
        }

        if (directionalTime.type == TimeType.MINUTE) {
            TimeUtils.Time endTime = new TimeUtils.Time();
            endTime.type = TimeType.MINUTE;
            endTime.hour = currentTime.getHour();
            endTime.minute = currentTime.getMinute();
            directionalTime.end = endTime;
        }

        if (directionalTime.type == TimeType.SECOND) {
            directionalTime.end = TimeUtils.thisTime();
        }

        return directionalTime;
    }

    public static DirectionalTime prevTimeDuration(TimeUtils.Time time, TimeDurationUtils.Duration duration) {

        DirectionalTime directionalTime = new DirectionalTime();

        directionalTime.type = duration.type;
        directionalTime.end = time;
        directionalTime.length = duration.length;

        return directionalTime;
    }

    public static void main() {
    }
}
