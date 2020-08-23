package ai.hual.labrador.utils;

/**
 * Created by ethan on 17-7-19.
 */
public class TimeDurationUtils {

    /**
     * <p><tt>type</tt> is in {@link ai.hual.labrador.utils.TimeType}, <tt>length</tt>
     * indicates how long the time period lasts.
     */
    public static class Duration {

        public TimeType type;
        public int length;

        public Duration() {

            this(null, 0);
        }

        public Duration(TimeType type, int length) {

            this.type = type;
            this.length = length;
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) return true;
            if (!(o instanceof Duration)) return false;
            Duration other = (Duration) o;
            return this.length == other.length && this.type == other.type;
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        public String toString() {

            return length + " " + type.toString();
        }
    }

    public static Duration dDurationHalf(int d) {

        Duration duration = new Duration();

        duration.type = TimeType.HOUR;
        duration.length = d * 24 + 12;

        return duration;
    }

    public static Duration dDurationHalf() {

        Duration duration = new Duration();

        duration.type = TimeType.HOUR;
        duration.length = 12;

        return duration;
    }

    public static Duration hmsDuration(int h, int m, int s) {

        Duration duration = new Duration();

        duration.type = TimeType.SECOND;
        duration.length = h * 60 * 60 + m * 60 + s;

        return duration;
    }

    public static Duration hmDuration(int h, int m) {

        Duration duration = new Duration();

        duration.type = TimeType.MINUTE;
        duration.length = h * 60 + m;

        return duration;
    }

    public static Duration msDuration(Integer m, Integer s) {

        Duration duration = new Duration();

        duration.type = TimeType.SECOND;
        duration.length = m * 60 + s;

        return duration;
    }

    public static Duration hsDuration(int h, int s) {

        Duration duration = new Duration();

        duration.type = TimeType.SECOND;
        duration.length = h * 60 * 60 + s;

        return duration;
    }

    public static Duration hDuration(int h) {

        Duration duration = new Duration();

        duration.type = TimeType.HOUR;
        duration.length = h;

        return duration;
    }

    public static Duration hDurationHalf(int h) {

        Duration duration = new Duration();

        duration.type = TimeType.MINUTE;
        duration.length = h * 60 + 30;

        return duration;
    }

    public static Duration hDurationHalf() {

        Duration duration = new Duration();

        duration.type = TimeType.MINUTE;
        duration.length = 30;

        return duration;
    }

    public static Duration qDuration(int q) {

        Duration duration = new Duration();

        duration.type = TimeType.QUARTER;
        duration.length = q;

        return duration;
    }

    public static Duration mDuration(int m) {

        Duration duration = new Duration();

        duration.type = TimeType.MINUTE;
        duration.length = m;

        return duration;
    }

    public static Duration mDurationHalf(int m) {

        Duration duration = new Duration();

        duration.type = TimeType.SECOND;
        duration.length = m * 60 + 30;

        return duration;
    }

    public static Duration mDurationHalf() {

        Duration duration = new Duration();

        duration.type = TimeType.SECOND;
        duration.length = 30;

        return duration;
    }

    public static Duration sDuration(int s) {

        Duration duration = new Duration();

        duration.type = TimeType.SECOND;
        duration.length = s;

        return duration;
    }

    public static Duration sDurationHalf(int s) {

        Duration duration = new Duration();

        duration.type = TimeType.MILLISECOND;
        duration.length = s * 1000 + 500;

        return duration;
    }

    public static Duration sDurationHalf() {

        Duration duration = new Duration();

        duration.type = TimeType.MILLISECOND;
        duration.length = 500;

        return duration;
    }

    public static Duration msDuration(int ms) {

        Duration duration = new Duration();

        duration.type = TimeType.MILLISECOND;
        duration.length = ms;

        return duration;
    }

    public static void main(String[] args) {

    }
}
