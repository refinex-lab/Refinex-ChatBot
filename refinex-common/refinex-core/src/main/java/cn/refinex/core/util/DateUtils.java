package cn.refinex.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 日期工具类
 *
 * @author Refinex
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

    // ========================= 常量 =========================

    /**
     * 默认时区（运行时系统时区）
     */
    public static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    /**
     * 默认区域（运行时系统区域）
     */
    public static final Locale DEFAULT_LOCALE = Locale.getDefault();

    /**
     * 常用日期格式：yyyy-MM-dd
     */
    public static final String PATTERN_DATE = "yyyy-MM-dd";

    /**
     * 常用日期时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 带毫秒的日期时间格式：yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final String PATTERN_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 缓存 DateTimeFormatter，避免重复创建（线程安全）
     */
    private static final ConcurrentMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

    // ========================= 内部工具方法 =========================

    /**
     * 获取缓存的 DateTimeFormatter
     *
     * @param pattern 非空的格式化模式
     * @return 对应的 DateTimeFormatter
     */
    private static DateTimeFormatter formatter(final String pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        return FORMATTER_CACHE.computeIfAbsent(pattern, p -> DateTimeFormatter.ofPattern(p, DEFAULT_LOCALE));
    }

    // ========================= 解析与格式化 =========================

    /**
     * 使用默认格式（yyyy-MM-dd）解析为 LocalDate
     *
     * @param dateStr 非空日期字符串
     * @return LocalDate
     * @throws DateTimeParseException 解析失败时抛出
     */
    public static LocalDate parseLocalDate(final String dateStr) {
        return parseLocalDate(dateStr, PATTERN_DATE);
    }

    /**
     * 使用自定义格式解析为 LocalDate
     *
     * @param dateStr 非空日期字符串
     * @param pattern 非空格式
     * @return LocalDate
     * @throws DateTimeParseException 解析失败
     */
    public static LocalDate parseLocalDate(final String dateStr, final String pattern) {
        Objects.requireNonNull(dateStr, "dateStr must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        return LocalDate.parse(dateStr, formatter(pattern));
    }

    /**
     * 使用默认格式（yyyy-MM-dd HH:mm:ss）解析为 LocalDateTime
     *
     * @param dateTimeStr 非空字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseLocalDateTime(final String dateTimeStr) {
        return parseLocalDateTime(dateTimeStr, PATTERN_DATETIME);
    }

    /**
     * 使用自定义格式解析为 LocalDateTime
     *
     * @param dateTimeStr 非空字符串
     * @param pattern     非空模式
     * @return LocalDateTime
     */
    public static LocalDateTime parseLocalDateTime(final String dateTimeStr, final String pattern) {
        Objects.requireNonNull(dateTimeStr, "dateTimeStr must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        return LocalDateTime.parse(dateTimeStr, formatter(pattern));
    }

    /**
     * 将 LocalDateTime 按指定时区转换并格式化为字符串
     *
     * @param dateTime 非空 LocalDateTime
     * @param pattern  非空格式
     * @param zone     非空时区
     * @return 格式化字符串
     */
    public static String format(final LocalDateTime dateTime, final String pattern, final ZoneId zone) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        return dateTime.atZone(zone).format(formatter(pattern));
    }

    /**
     * 使用默认时区格式化 LocalDateTime（yyyy-MM-dd HH:mm:ss）
     *
     * @param dateTime 非空 LocalDateTime
     * @return 格式化字符串
     */
    public static String format(final LocalDateTime dateTime) {
        return format(dateTime, PATTERN_DATETIME, DEFAULT_ZONE);
    }

    /**
     * 将 LocalDate 格式化为 yyyy-MM-dd
     *
     * @param date LocalDate
     * @return 格式化字符串
     */
    public static String format(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.format(formatter(PATTERN_DATE));
    }

    // ========================= 转换（Date/Instant/LocalDateTime） =========================

    /**
     * 将 Date 转换为 LocalDateTime（使用指定时区）
     *
     * @param date 非空 java.util.Date
     * @param zone 非空时区
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(final Date date, final ZoneId zone) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        return LocalDateTime.ofInstant(date.toInstant(), zone);
    }

    /**
     * 将 LocalDateTime 转换为 java.util.Date（指定时区）
     *
     * @param localDateTime 非空 LocalDateTime
     * @param zone          非空时区
     * @return java.util.Date
     */
    public static Date toDate(final LocalDateTime localDateTime, final ZoneId zone) {
        Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * 将 LocalDate 转换为 java.util.Date（视为当天开始时间，指定时区）
     *
     * @param localDate 非空 LocalDate
     * @param zone      非空时区
     * @return java.util.Date
     */
    public static Date toDate(final LocalDate localDate, final ZoneId zone) {
        Objects.requireNonNull(localDate, "localDate must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        return toDate(localDate.atStartOfDay(), zone);
    }

    /**
     * 将 Instant 转换为 LocalDateTime（指定时区）
     *
     * @param instant 非空 Instant
     * @param zone    非空时区
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(final Instant instant, final ZoneId zone) {
        Objects.requireNonNull(instant, "instant must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * 将 LocalDateTime 转换为 Instant（指定时区）
     *
     * @param localDateTime 非空 LocalDateTime
     * @param zone          非空时区
     * @return Instant
     */
    public static Instant toInstant(final LocalDateTime localDateTime, final ZoneId zone) {
        Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        return localDateTime.atZone(zone).toInstant();
    }

    /**
     * 将毫秒时间戳转换为 LocalDateTime（默认时区）
     *
     * @param epochMilli 毫秒时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime ofEpochMilli(final long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), DEFAULT_ZONE);
    }

    /**
     * 将 LocalDateTime 转换为毫秒时间戳（基于指定时区）
     *
     * @param localDateTime 非空 LocalDateTime
     * @param zone          非空时区
     * @return 毫秒时间戳
     */
    public static long toEpochMilli(final LocalDateTime localDateTime, final ZoneId zone) {
        return toInstant(localDateTime, zone).toEpochMilli();
    }

    // ========================= 首/末时间点（天/周/月/季/年） =========================

    /**
     * 获取某日的开始时刻（00:00）
     *
     * @param date 非空 LocalDate
     * @return LocalDateTime（当天00:00）
     */
    public static LocalDateTime startOfDay(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.atStartOfDay();
    }

    /**
     * 获取某日的结束时刻（23:59:59.999999999）
     * 注意：部分系统只支持毫秒精度，必要时可转换为毫秒截断
     *
     * @param date 非空 LocalDate
     * @return LocalDateTime（当天最大时间）
     */
    public static LocalDateTime endOfDay(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.atTime(LocalTime.MAX);
    }

    /**
     * 获取包含指定日期的本周开始（基于给定 locale 的第一工作日）
     *
     * @param date Locale 非空
     * @return LocalDateTime（本周开始的 00:00）
     */
    public static LocalDateTime startOfWeek(final LocalDate date, final Locale locale) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        DayOfWeek firstDayOfWeek = WeekFields.of(locale).getFirstDayOfWeek();
        LocalDate start = date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
        return start.atStartOfDay();
    }

    /**
     * 获取本周结束（基于给定 locale 的第一工作日）
     *
     * @param date   非空 LocalDate
     * @param locale 非空 Locale
     * @return LocalDateTime（本周结束的最大时间）
     */
    public static LocalDateTime endOfWeek(final LocalDate date, final Locale locale) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        DayOfWeek firstDayOfWeek = WeekFields.of(locale).getFirstDayOfWeek();
        DayOfWeek lastDayOfWeek = firstDayOfWeek.plus(6);
        LocalDate end = date.with(TemporalAdjusters.nextOrSame(lastDayOfWeek));
        return end.atTime(LocalTime.MAX);
    }

    /**
     * 获取本月开始（当月第一天 00:00）
     *
     * @param date 非空 LocalDate
     * @return LocalDateTime
     */
    public static LocalDateTime startOfMonth(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.withDayOfMonth(1).atStartOfDay();
    }

    /**
     * 获取本月结束（当月最后一天 23:59:59.999...）
     *
     * @param date 非空 LocalDate
     * @return LocalDateTime
     */
    public static LocalDateTime endOfMonth(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        LocalDate last = date.with(TemporalAdjusters.lastDayOfMonth());
        return last.atTime(LocalTime.MAX);
    }

    /**
     * 获取指定日期所在季度的第一天（当日所在季度第一天 00:00）
     *
     * @param date 非空 LocalDate
     * @return LocalDateTime
     */
    public static LocalDateTime startOfQuarter(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        int currentMonth = date.getMonthValue();
        int startMonth = ((currentMonth - 1) / 3) * 3 + 1;
        LocalDate first = LocalDate.of(date.getYear(), startMonth, 1);
        return first.atStartOfDay();
    }

    /**
     * 获取指定日期所在季度的结束时间（季度最后一天 23:59:59.999...）
     *
     * @param date 非空 LocalDate
     * @return LocalDateTime
     */
    public static LocalDateTime endOfQuarter(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        int currentMonth = date.getMonthValue();
        int startMonth = ((currentMonth - 1) / 3) * 3 + 1;
        LocalDate firstOfQuarter = LocalDate.of(date.getYear(), startMonth, 1);
        LocalDate firstOfNextQuarter = firstOfQuarter.plusMonths(3);
        LocalDate lastOfQuarter = firstOfNextQuarter.minusDays(1);
        return lastOfQuarter.atTime(LocalTime.MAX);
    }

    /**
     * 获取本年的开始（当年1月1日 00:00）
     *
     * @param date 非空 LocalDate
     * @return LocalDateTime
     */
    public static LocalDateTime startOfYear(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return LocalDate.of(date.getYear(), 1, 1).atStartOfDay();
    }

    /**
     * 获取本年结束（当年12月31日 23:59:59.999...）
     *
     * @param date 非空 LocalDate
     * @return LocalDateTime
     */
    public static LocalDateTime endOfYear(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return LocalDate.of(date.getYear(), 12, 31).atTime(LocalTime.MAX);
    }

    // ========================= 日期计算与区间 =========================

    /**
     * 计算两个日期之间相差的天数（精确到日，不包含时间部分），结果可能为负数
     *
     * @param start 非空 LocalDate
     * @param end   非空 LocalDate
     * @return 相差天数（end - start）
     */
    public static long daysBetween(final LocalDate start, final LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个时间点之间的指定单位差值（支持 DAYS/HOURS/MINUTES/SECONDS/MILLIS）
     *
     * @param start 非空 Temporal（例如 LocalDateTime）
     * @param end   非空 Temporal
     * @param unit  ChronoUnit
     * @return 差值（end - start）
     */
    public static long between(final Temporal start, final Temporal end, final ChronoUnit unit) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        return unit.between(start, end);
    }

    /**
     * 判断两个 LocalDateTime 是否在同一天
     *
     * @param a 非空 LocalDateTime
     * @param b 非空 LocalDateTime
     * @return true 如果同一天
     */
    public static boolean isSameDay(final LocalDateTime a, final LocalDateTime b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        return a.toLocalDate().equals(b.toLocalDate());
    }

    /**
     * 判断某日期是否在指定范围（包含边界）内
     *
     * @param date  非空 LocalDate
     * @param start 非空 LocalDate
     * @param end   非空 LocalDate
     * @return true 如果在范围内
     */
    public static boolean isBetweenInclusive(final LocalDate date, final LocalDate start, final LocalDate end) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return !date.isBefore(start) && !date.isAfter(end);
    }

    // ========================= 工作日与节假日支持 =========================

    /**
     * 节假日提供者接口，可以实现此接口以注入自定义假期规则（例如法定节假日、调休等）
     */
    public interface HolidayProvider {
        /**
         * 判断给定日期是否为假日（true 表示假日，非工作日）
         *
         * @param date 非空 LocalDate
         * @return boolean
         */
        boolean isHoliday(LocalDate date);

        /**
         * 默认实现：基于给定集合判断是否是假期
         */
        class FromSet implements HolidayProvider {
            private final Set<LocalDate> holidaySet;

            public FromSet(final Set<LocalDate> holidaySet) {
                this.holidaySet = Objects.requireNonNull(holidaySet, "holidaySet must not be null");
            }

            @Override
            public boolean isHoliday(final LocalDate date) {
                return holidaySet.contains(date);
            }
        }
    }

    /**
     * 默认周末判定（周六、周日）
     */
    private static final Set<DayOfWeek> DEFAULT_WEEKEND = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)));


    /**
     * 判断某日是否为周末（使用默认周末规则）
     *
     * @param date 非空 LocalDate
     * @return true 如果为周末
     */
    public static boolean isWeekend(final LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return DEFAULT_WEEKEND.contains(date.getDayOfWeek());
    }

    /**
     * 判断某日是否为工作日（非周末且非节假日）
     *
     * @param date            非空 LocalDate
     * @param holidayProvider 非空 HolidayProvider（可传入 HolidayProvider.FromSet）
     * @return true 如果为工作日
     */
    public static boolean isWorkingDay(final LocalDate date, final HolidayProvider holidayProvider) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(holidayProvider, "holidayProvider must not be null");
        return !DEFAULT_WEEKEND.contains(date.getDayOfWeek()) && !holidayProvider.isHoliday(date);
    }

    /**
     * 在指定日期上增加工作日（跳过周末和假期）。支持正负值（向前/向后移动）
     *
     * @param base            非空起始日期
     * @param businessDays    要增加的工作日数量（可为负）
     * @param holidayProvider 非空 HolidayProvider（企业自定义）
     * @return 计算后的 LocalDate
     */
    public static LocalDate addBusinessDays(final LocalDate base, final int businessDays, final HolidayProvider holidayProvider) {
        Objects.requireNonNull(base, "base must not be null");
        Objects.requireNonNull(holidayProvider, "holidayProvider must not be null");
        if (businessDays == 0) {
            return base;
        }
        int remaining = Math.abs(businessDays);
        int direction = businessDays > 0 ? 1 : -1;
        LocalDate cursor = base;
        while (remaining > 0) {
            cursor = cursor.plusDays(direction);
            if (!DEFAULT_WEEKEND.contains(cursor.getDayOfWeek()) && !holidayProvider.isHoliday(cursor)) {
                remaining--;
            }
        }
        return cursor;
    }

    /**
     * 获取下一个工作日（不包含 base 本身）
     *
     * @param base            非空 LocalDate
     * @param holidayProvider 非空 HolidayProvider
     * @return 下一个工作日
     */
    public static LocalDate nextWorkingDay(final LocalDate base, final HolidayProvider holidayProvider) {
        return addBusinessDays(base, 1, holidayProvider);
    }

    // ========================= 截断 / 取整 =========================

    /**
     * 将 LocalDateTime 截断到指定单位（支持 DAYS/HOURS/MINUTES/SECONDS）
     *
     * @param dateTime 非空 LocalDateTime
     * @param unit     非空 ChronoUnit（不能为 MONTHS/YEARS）
     * @return 截断后的 LocalDateTime
     */
    public static LocalDateTime truncate(final LocalDateTime dateTime, final ChronoUnit unit) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        switch (unit) {
            case DAYS:
                return dateTime.toLocalDate().atStartOfDay();
            case HOURS:
                return dateTime.truncatedTo(ChronoUnit.HOURS);
            case MINUTES:
                return dateTime.truncatedTo(ChronoUnit.MINUTES);
            case SECONDS:
                return dateTime.truncatedTo(ChronoUnit.SECONDS);
            case MILLIS:
                return dateTime.truncatedTo(ChronoUnit.MILLIS);
            default:
                throw new IllegalArgumentException("Unsupported unit for truncate: " + unit);
        }
    }

    /**
     * 将 LocalDateTime 向上舍入到指定单位（ceil）
     * 例如：2025-10-04 10:15:10 向上舍入到分钟 => 2025-10-04 10:16:00
     *
     * @param dateTime 非空 LocalDateTime
     * @param unit     非空 ChronoUnit（支持 MINUTES/HOURS/SECONDS）
     * @return 向上舍入结果
     */
    public static LocalDateTime ceil(final LocalDateTime dateTime, final ChronoUnit unit) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        LocalDateTime truncated = truncate(dateTime, unit);
        if (truncated.equals(dateTime)) {
            return truncated;
        }
        return truncated.plus(1, unit);
    }

    // ========================= 格式校验 / 容错解析 =========================

    /**
     * 尝试解析字符串为 LocalDate，解析失败返回 Optional.empty()（不抛异常）
     *
     * @param dateStr 非空字符串
     * @param pattern 非空格式
     * @return Optional<LocalDate>
     */
    public static Optional<LocalDate> tryParseLocalDate(final String dateStr, final String pattern) {
        Objects.requireNonNull(dateStr, "dateStr must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        try {
            return Optional.of(parseLocalDate(dateStr, pattern));
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }

    /**
     * 判断字符串是否为指定格式的合法日期（严格匹配）
     *
     * @param dateStr 非空字符串
     * @param pattern 非空模式
     * @return boolean
     */
    public static boolean isValidDate(final String dateStr, final String pattern) {
        return tryParseLocalDate(dateStr, pattern).isPresent();
    }

    // ========================= 人性化/可读化输出 =========================

    /**
     * 将两个时间点转换为人性化的时间差描述，常用于日志/UI 展示（中文）
     * 例如："刚刚"、"3分钟前"、"2天前"、"1年前" 等。
     *
     * @param from 非空起始时间
     * @param to   非空结束时间
     * @return 中文描述
     */
    public static String humanReadableDiff(final LocalDateTime from, final LocalDateTime to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        boolean future = to.isAfter(from);
        long seconds = Math.abs(ChronoUnit.SECONDS.between(from, to));
        if (seconds < 60) {
            return "刚刚";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " 分钟" + (future ? "后" : "前");
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " 小时" + (future ? "后" : "前");
        }
        long days = hours / 24;
        if (days < 30) {
            return days + " 天" + (future ? "后" : "前");
        }
        long months = days / 30;
        if (months < 12) {
            return months + " 个月" + (future ? "后" : "前");
        }
        long years = months / 12;
        return years + " 年" + (future ? "后" : "前");
    }

    // ========================= 其他常用工具 =========================

    /**
     * 判断两个时间段是否相交（区间以 [start, end] 表示，入参需满足 start <= end）
     *
     * @param aStart 非空开始时间
     * @param aEnd   非空结束时间
     * @param bStart 非空开始时间
     * @param bEnd   非空结束时间
     * @return true 如果相交
     */
    public static boolean overlap(final LocalDateTime aStart, final LocalDateTime aEnd, final LocalDateTime bStart, final LocalDateTime bEnd) {
        Objects.requireNonNull(aStart, "aStart must not be null");
        Objects.requireNonNull(aEnd, "aEnd must not be null");
        Objects.requireNonNull(bStart, "bStart must not be null");
        Objects.requireNonNull(bEnd, "bEnd must not be null");
        if (aStart.isAfter(aEnd) || bStart.isAfter(bEnd)) {
            throw new IllegalArgumentException("区间开始时间必须小于等于结束时间");
        }
        return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart);
    }

    /**
     * 将 Period（年/月/日）表达为人可读的字符串（例如用于统计/报表）
     *
     * @param period 非空 Period
     * @return 可读字符串，例如："1年2个月3天"
     */
    public static String formatPeriod(final Period period) {
        Objects.requireNonNull(period, "period must not be null");
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();
        StringBuilder sb = new StringBuilder();
        if (years != 0) {
            sb.append(Math.abs(years)).append("年");
        }
        if (months != 0) {
            sb.append(Math.abs(months)).append("月");
        }
        if (days != 0) {
            sb.append(Math.abs(days)).append("天");
        }
        if (sb.isEmpty()) {
            return "0天";
        }
        return sb.toString();
    }
}
