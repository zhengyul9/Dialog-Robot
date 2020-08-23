package ai.hual.labrador.utils;

/**
 * Created by ethan on 17-7-19.
 */
public class DateDurationUtils {

    /**
     * <p><tt>type</tt> is in {@link ai.hual.labrador.utils.DateType}, <tt>length</tt>
     * indicates how long the date period lasts.
     */
    public static class Duration {

        public DateType type;
        public int length;

        public Duration() {

            this(null, 0);
        }

        public Duration(DateType type, int length) {

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

    public static Duration cDuration(int c) {

        Duration duration = new Duration();

        duration.type = DateType.CENTURY;
        duration.length = c;

        return duration;
    }

    public static Duration cDurationHalf(int c) {

        Duration duration = new Duration();

        duration.type = DateType.YEAR;
        duration.length = c * 100 + 50;

        return duration;
    }

    public static Duration cDurationHalf() {

        Duration duration = new Duration();

        duration.type = DateType.CENTURY;
        duration.length = 50;

        return duration;
    }


    public static Duration yDuration(int y) {

        Duration duration = new Duration();

        duration.type = DateType.YEAR;

        if (y < 1000 && y > 0) {
            duration.length = y;
            return duration;
        } else
            return null;
    }

    public static Duration yDurationHalf(int y) {

        Duration duration = new Duration();

        duration.type = DateType.MONTH;

        if (y < 1000 && y > 0) {
            duration.length = y * 12 + 6;
            return duration;
        } else
            return null;
    }

    public static Duration yDurationHalf() {

        Duration duration = new Duration();

        duration.type = DateType.MONTH;

        duration.length = 6;

        return duration;
    }

    public static Duration sDuration(int s) {

        Duration duration = new Duration();

        duration.type = DateType.SEASON;
        duration.length = s;

        return duration;
    }

    public static Duration mDuration(int m) {

        Duration duration = new Duration();

        duration.type = DateType.MONTH;
        duration.length = m;

        return duration;
    }

    public static Duration mDurationHalf(int m) {

        Duration duration = new Duration();

        duration.type = DateType.DAY;
        duration.length = m * 30 + 15;

        return duration;
    }

    public static Duration mDurationHalf() {

        Duration duration = new Duration();

        duration.type = DateType.DAY;
        duration.length = 15;

        return duration;
    }

    public static Duration tdDuration(int td) {

        Duration duration = new Duration();

        duration.type = DateType.TENDAYS;
        duration.length = td;

        return duration;
    }

    public static Duration wDuration(int w) {

        Duration duration = new Duration();

        duration.type = DateType.WEEK;
        duration.length = w;

        return duration;
    }

    public static Duration dDuration(int d) {

        Duration duration = new Duration();

        duration.type = DateType.DAY;
        duration.length = d;

        return duration;
    }

    public static void main(String[] args) {

    }
}
