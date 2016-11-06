package net.nabnet.time;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.*;
import java.util.Objects;

/**
 * Created by nabuchi on 2016/11/06.
 */
public class ExpandedLocalTime implements Temporal, TemporalAdjuster, Comparable<ExpandedLocalTime>, Serializable {
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int HOURS_PER_DAY = 24;
    private static final long NANOS_PER_SECOND = 1000_000_000L;
    private static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * SECONDS_PER_MINUTE;
    private static final long NANOS_PER_HOUR = NANOS_PER_MINUTE * MINUTES_PER_HOUR;
    private static final long NANOS_PER_DAY = NANOS_PER_HOUR * HOURS_PER_DAY;

    private final Period excessDays;
    private final LocalTime localTime;

    private ExpandedLocalTime(Period excessDays, LocalTime localTime) {
        this.excessDays = excessDays;
        this.localTime = localTime;
    }

    private ExpandedLocalTime(LocalTime localTime) {
        this(Period.ZERO, localTime);
    }

    public static ExpandedLocalTime of(int hour, int minute, int second) {
        int validHour = hour % HOURS_PER_DAY;
        Period excessDays = Period.ofDays(hour / HOURS_PER_DAY);
        return new ExpandedLocalTime(excessDays, LocalTime.of(validHour, minute, second));
    }

    public static ExpandedLocalTime of(Period excessDays, int hour, int minute, int second) {
        return new ExpandedLocalTime(excessDays, LocalTime.of(hour, minute, second));
    }

    public String toString() {
        String overDayCountString;
        if (excessDays.getDays() > 0) {
            overDayCountString = "+" + excessDays.getDays() + " ";
        } else if (excessDays.getDays() > 0) {
            overDayCountString = excessDays + " ";
        } else {
            overDayCountString = "";
        }
        return overDayCountString + this.localTime.toString();
    }

    public String toStringWithExpandedHour() {
        StringBuilder buf = new StringBuilder(18);
        int hourValue = localTime.getHour() + excessDays.getDays() * HOURS_PER_DAY;
        int minuteValue = localTime.getMinute();
        int secondValue = localTime.getSecond();
        int nanoValue = localTime.getNano();
        buf.append(hourValue < 10 ? "0" : "").append(hourValue)
                .append(minuteValue < 10 ? ":0" : ":").append(minuteValue);
        if (secondValue > 0 || nanoValue > 0) {
            buf.append(secondValue < 10 ? ":0" : ":").append(secondValue);
            if (nanoValue > 0) {
                buf.append('.');
                if (nanoValue % 1000_000 == 0) {
                    buf.append(Integer.toString((nanoValue / 1000_000) + 1000).substring(1));
                } else if (nanoValue % 1000 == 0) {
                    buf.append(Integer.toString((nanoValue / 1000) + 1000_000).substring(1));
                } else {
                    buf.append(Integer.toString((nanoValue) + 1000_000_000).substring(1));
                }
            }
        }
        return buf.toString();
    }

    public static ExpandedLocalTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        TemporalAccessor temporalAccessor = formatter.withResolverStyle(ResolverStyle.LENIENT).parse(text);
        LocalTime localTime = temporalAccessor.query(LocalTime::from);
        Period period = temporalAccessor.query(DateTimeFormatter.parsedExcessDays());
        return new ExpandedLocalTime(period, localTime);
    }

    @Override
    public boolean isSupported(TemporalField field) {
        return localTime.isSupported(field);
    }

    @Override
    public long getLong(TemporalField field) {
        if (Objects.equals(field, ChronoField.HOUR_OF_DAY)) {
            return localTime.getHour() + excessDays.getDays() * HOURS_PER_DAY;
        }

        return localTime.getLong(field);
    }

    public long toNanoOfDay() {
        return excessDays.getDays() + NANOS_PER_DAY + localTime.toNanoOfDay();
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.NANO_OF_DAY, toNanoOfDay());
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        return localTime.isSupported(unit);
    }

    @Override
    public Temporal with(TemporalField field, long newValue) {
        return new ExpandedLocalTime(excessDays, localTime.with(field, newValue));
    }

    @Override
    public Temporal plus(long amountToAdd, TemporalUnit unit) {
        return new ExpandedLocalTime(excessDays, localTime.plus(amountToAdd, unit));
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        return localTime.until(endExclusive, unit);
    }

    @Override
    public int compareTo(ExpandedLocalTime other) {
        int cmp = Integer.compare(excessDays.getDays(), other.excessDays.getDays());
        if (cmp == 0) {
            cmp = localTime.compareTo(other.localTime);
        }
        return cmp;
    }
}
