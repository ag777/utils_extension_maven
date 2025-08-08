package github.ag777.util.quartz;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Cron表达式构建器，支持链式调用和各种时间场景
 * <p>
 * Cron表达式格式：秒 分 时 日 月 周 年(可选)
 * <p>
 * 使用示例：
 * <pre>
 * // 每30秒执行一次
 * String cron = CronBuilder.create().everySeconds(30).build();
 * 
 * // 每天上午9:30执行
 * String cron = CronBuilder.create().daily().at(9, 30, 0).build();
 * 
 * // 每周一上午9:00执行
 * String cron = CronBuilder.create().weekly(1).at(9, 0, 0).build();
 * 
 * // 每月1号上午8:00执行
 * String cron = CronBuilder.create().monthly(1).at(8, 0, 0).build();
 * 
 * // 工作日每2小时执行一次
 * String cron = CronBuilder.create().weekdays().everyHours(2).build();
 * </pre>
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/08/05 11:39
 */
public class CronBuilder {
    
    private String second = "*";
    private String minute = "*";
    private String hour = "*";
    private String dayOfMonth = "*";
    private String month = "*";
    private String dayOfWeek = "?";
    private String year = "";
    
    /**
     * 创建CronBuilder实例
     */
    public static CronBuilder create() {
        return new CronBuilder();
    }
    
    // ============ 间隔执行方法 ============
    
    /**
     * 每X秒执行一次（自动转换单位）
     * @param interval 秒间隔，如果>=60会自动转换为分钟
     */
    public CronBuilder everySeconds(int interval) {
        if (interval <= 0) {
            interval = 1; // 默认最小1秒
        }
        
        if (interval >= 60) {
            // 转换为分钟
            int minutes = interval / 60;
            int remainingSeconds = interval % 60;
            if (remainingSeconds == 0) {
                return everyMinutes(minutes);
            } else {
                // 有余数秒，无法用简单的 cron 间隔表示
                // 回退到每分钟执行，这是最接近的可表示间隔
                return everyMinutes(minutes > 0 ? minutes : 1);
            }
        } else {
            this.second = "0/" + interval;
        }
        return this;
    }
    
    /**
     * 每X分钟执行一次（自动转换单位）
     * @param interval 分钟间隔，如果>=60会自动转换为小时
     */
    public CronBuilder everyMinutes(int interval) {
        if (interval <= 0) {
            interval = 1; // 默认最小1分钟
        }
        
        if (interval >= 60) {
            // 转换为小时
            int hours = interval / 60;
            int remainingMinutes = interval % 60;
            if (remainingMinutes == 0) {
                return everyHours(hours);
            } else {
                // 有余数分钟，无法用简单的 cron 间隔表示
                // 回退到每小时执行，这是最接近的可表示间隔
                return everyHours(hours > 0 ? hours : 1);
            }
        } else {
            this.minute = "0/" + interval;
        }
        this.second = "0";
        return this;
    }
    

    
    /**
     * 每X天执行一次（覆盖周设置）
     * @param interval 天间隔
     */
    public CronBuilder everyDays(int interval) {
        if (interval <= 0) {
            interval = 1; // 默认最小1天
        }
        this.dayOfMonth = "1/" + interval;
        this.dayOfWeek = "?"; // 清除周设置，避免冲突
        this.hour = "0";
        this.minute = "0";
        this.second = "0";
        return this;
    }
    
    /**
     * 每X小时执行一次（自动转换单位）
     * @param interval 小时间隔，如果>=24会自动转换为天
     */
    public CronBuilder everyHours(int interval) {
        if (interval <= 0) {
            interval = 1; // 默认最小1小时
        }
        
        if (interval >= 24) {
            // 转换为天
            int days = interval / 24;
            int remainingHours = interval % 24;
            if (remainingHours == 0) {
                return everyDays(days);
            } else {
                // 有余数小时，无法用简单的 cron 间隔表示
                // 回退到每天执行，这是最接近的可表示间隔
                return everyDays(days > 0 ? days : 1);
            }
        } else {
            this.hour = "0/" + interval;
        }
        this.minute = "0";
        this.second = "0";
        return this;
    }
    
    // ============ 固定周期方法 ============
    
    /**
     * 每天执行（自动处理与周设置的冲突）
     */
    public CronBuilder daily() {
        this.dayOfMonth = "*";
        this.dayOfWeek = "?"; // 清除周设置，避免冲突
        return this;
    }
    
    /**
     * 每周执行（自动调整参数，覆盖月日设置）
     * @param dayOfWeek 周几执行（1=周日, 2=周一, ..., 7=周六），超出范围会自动调整
     */
    public CronBuilder weekly(int dayOfWeek) {
        // 自动调整到1-7范围内
        dayOfWeek = ((dayOfWeek - 1) % 7) + 1;
        if (dayOfWeek <= 0) {
            dayOfWeek = 1; // 默认周日
        }
        this.dayOfMonth = "?"; // 清除月日设置，避免冲突
        this.dayOfWeek = String.valueOf(dayOfWeek);
        return this;
    }
    
    /**
     * 每月执行（自动调整参数，覆盖周设置）
     * @param dayOfMonth 月份中的第几天，超出范围会自动调整到1-31之间
     */
    public CronBuilder monthly(int dayOfMonth) {
        // 自动调整到1-31范围内
        if (dayOfMonth < 1) {
            dayOfMonth = 1;
        } else if (dayOfMonth > 31) {
            dayOfMonth = 31;
        }
        this.dayOfMonth = String.valueOf(dayOfMonth);
        this.dayOfWeek = "?"; // 清除周设置，避免冲突
        return this;
    }
    
    /**
     * 每月最后一天执行（覆盖周设置）
     */
    public CronBuilder monthlyLastDay() {
        this.dayOfMonth = "L";
        this.dayOfWeek = "?"; // 清除周设置，避免冲突
        return this;
    }
    
    /**
     * 工作日执行（周一到周五，覆盖月日设置）
     */
    public CronBuilder weekdays() {
        this.dayOfMonth = "?"; // 清除月日设置，避免冲突
        this.dayOfWeek = "2-6"; // 2=周一, 6=周五
        return this;
    }
    
    /**
     * 周末执行（周六、周日，覆盖月日设置）
     */
    public CronBuilder weekends() {
        this.dayOfMonth = "?"; // 清除月日设置，避免冲突
        this.dayOfWeek = "1,7"; // 1=周日, 7=周六
        return this;
    }
    
    // ============ 年份和月份设置方法 ============
    
    /**
     * 每年执行
     */
    public CronBuilder yearly() {
        this.year = "*";
        return this;
    }
    
    /**
     * 指定年份执行（覆盖其他年份设置）
     * @param year 年份，如2024
     */
    public CronBuilder inYear(int year) {
        this.year = String.valueOf(year);
        return this;
    }
    
    /**
     * 多个年份执行
     * @param years 年份数组
     */
    public CronBuilder inYears(int... years) {
        this.year = Arrays.stream(years)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        return this;
    }
    
    /**
     * 年份范围执行
     * @param startYear 开始年份
     * @param endYear 结束年份
     */
    public CronBuilder betweenYears(int startYear, int endYear) {
        if (startYear > endYear) {
            int temp = startYear;
            startYear = endYear;
            endYear = temp;
        }
        this.year = startYear + "-" + endYear;
        return this;
    }
    
    /**
     * 指定月份执行
     * @param month 月份(1-12)，超出范围会自动调整
     */
    public CronBuilder inMonth(int month) {
        // 自动调整到1-12范围内
        month = Math.max(1, Math.min(12, month));
        this.month = String.valueOf(month);
        return this;
    }
    
    /**
     * 多个月份执行
     * @param months 月份数组，超出范围的值会自动调整到1-12之间
     */
    public CronBuilder inMonths(int... months) {
        String monthStr = Arrays.stream(months)
                .map(m -> Math.max(1, Math.min(12, m))) // 自动调整到1-12范围
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        this.month = monthStr;
        return this;
    }
    
    /**
     * 月份范围执行
     * @param startMonth 开始月份(1-12)
     * @param endMonth 结束月份(1-12)
     */
    public CronBuilder betweenMonths(int startMonth, int endMonth) {
        // 自动调整范围
        startMonth = Math.max(1, Math.min(12, startMonth));
        endMonth = Math.max(1, Math.min(12, endMonth));
        
        // 如果开始月份>结束月份，自动交换
        if (startMonth > endMonth) {
            int temp = startMonth;
            startMonth = endMonth;
            endMonth = temp;
        }
        
        this.month = startMonth + "-" + endMonth;
        return this;
    }
    
    /**
     * 季度执行
     * @param quarter 季度(1-4)，1=春季(1-3月), 2=夏季(4-6月), 3=秋季(7-9月), 4=冬季(10-12月)
     */
    public CronBuilder inQuarter(int quarter) {
        quarter = Math.max(1, Math.min(4, quarter));
        return switch (quarter) {
            case 1 -> inMonths(1, 2, 3);   // 春季
            case 2 -> inMonths(4, 5, 6);   // 夏季
            case 3 -> inMonths(7, 8, 9);   // 秋季
            case 4 -> inMonths(10, 11, 12); // 冬季
            default -> inMonths(1, 2, 3);
        };
    }
    
    // ============ 高级组合方法 ============
    
    /**
     * 完整日期时间设置（一次性设置所有时间组件）
     * @param year 年份
     * @param month 月份(1-12)
     * @param dayOfMonth 日期(1-31)
     * @param hour 小时(0-23)
     * @param minute 分钟(0-59)
     * @param second 秒(0-59)
     */
    public CronBuilder exactDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return inYear(year).inMonth(month).monthly(dayOfMonth).at(hour, minute, second);
    }
    
    /**
     * 完整日期时间设置（秒默认为0）
     */
    public CronBuilder exactDateTime(int year, int month, int dayOfMonth, int hour, int minute) {
        return exactDateTime(year, month, dayOfMonth, hour, minute, 0);
    }
    
    /**
     * 从当前时间设置完整的执行时间（包含年月日）
     */
    public CronBuilder atCurrentDateTime() {
        return at(new Date());
    }
    
    /**
     * 从指定日期设置完整的执行时间（包含年月日）
     */
    public CronBuilder atExactDate(Date date) {
        return at(date);
    }
    
    /**
     * 从当前时间只设置时分秒
     */
    public CronBuilder atCurrentTime() {
        return atTime(new Date());
    }
    
    /**
     * 设置为今年执行
     */
    public CronBuilder thisYear() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        return inYear(cal.get(java.util.Calendar.YEAR));
    }
    
    /**
     * 设置为明年执行
     */
    public CronBuilder nextYear() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        return inYear(cal.get(java.util.Calendar.YEAR) + 1);
    }
    
    /**
     * 设置为本月执行
     */
    public CronBuilder thisMonth() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        return inMonth(cal.get(java.util.Calendar.MONTH) + 1);
    }
    
    /**
     * 设置为下个月执行
     */
    public CronBuilder nextMonth() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int nextMonth = cal.get(java.util.Calendar.MONTH) + 2; // +1 for next month, +1 for 0-based index
        if (nextMonth > 12) {
            nextMonth = 1;
        }
        return inMonth(nextMonth);
    }
    
    /**
     * 工作时间执行（周一到周五，9-17点）
     */
    public CronBuilder duringWorkHours() {
        return weekdays().betweenHours(9, 17);
    }
    
    /**
     * 午休时间执行（12-14点）
     */
    public CronBuilder duringLunchTime() {
        return betweenHours(12, 14);
    }
    
    /**
     * 非工作时间执行（周末或工作日18-8点）
     */
    public CronBuilder outsideWorkHours() {
        // 这个比较复杂，使用自定义表达式
        return custom(CronField.DAY_OF_WEEK, "1,7") // 周末
               .custom(CronField.HOUR, "0-8,18-23"); // 或者工作日的非工作时间
    }
    
    // ============ 时间设置方法 ============
    
    /**
     * 设置具体执行时间（自动调整参数）
     * @param hour 小时，超出范围会自动调整
     * @param minute 分钟，超出范围会自动调整
     * @param second 秒，超出范围会自动调整
     */
    public CronBuilder at(int hour, int minute, int second) {
        // 处理秒溢出
        if (second >= 60) {
            minute += second / 60;
            second = second % 60;
        } else if (second < 0) {
            second = 0;
        }
        
        // 处理分钟溢出
        if (minute >= 60) {
            hour += minute / 60;
            minute = minute % 60;
        } else if (minute < 0) {
            minute = 0;
        }
        
        // 处理小时溢出
        hour = hour % 24;
        if (hour < 0) {
            hour = 0;
        }
        
        this.hour = String.valueOf(hour);
        this.minute = String.valueOf(minute);
        this.second = String.valueOf(second);
        return this;
    }
    
    /**
     * 设置具体执行时间（秒默认为0）
     * @param hour 小时(0-23)
     * @param minute 分钟(0-59)
     */
    public CronBuilder at(int hour, int minute) {
        return at(hour, minute, 0);
    }
    
    /**
     * 从Date对象设置完整执行时间（包含年月日时分秒）
     * @param date 日期对象，如果为null则使用当前时间
     */
    public CronBuilder at(Date date) {
        return at(date, true);
    }
    
    /**
     * 从Date对象只设置时分秒（不包含年月日）
     * @param date 日期对象，如果为null则使用当前时间
     */
    public CronBuilder atTime(Date date) {
        return at(date, false);
    }
    
    /**
     * 从Date对象设置执行时间
     * @param date 日期对象，如果为null则使用当前时间
     * @param includeDate 是否包含年月日信息
     */
    public CronBuilder at(Date date, boolean includeDate) {
        if (date == null) {
            date = new Date(); // 使用当前时间
        }
        
        // 使用Calendar来获取时间组件
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH) + 1; // Calendar的月份从0开始
        int dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH);
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = cal.get(java.util.Calendar.MINUTE);
        int second = cal.get(java.util.Calendar.SECOND);
        
        if (includeDate) {
            this.year = String.valueOf(year);
            this.month = String.valueOf(month);
            this.dayOfMonth = String.valueOf(dayOfMonth);
            this.dayOfWeek = "?"; // 清除周设置，避免冲突
        }
        
        return at(hour, minute, second);
    }
    
    /**
     * 设置多个执行时间点（自动调整参数）
     * @param hours 小时数组，超出范围的值会自动调整到0-23之间
     */
    public CronBuilder atHours(int... hours) {
        String hourStr = Arrays.stream(hours)
                .map(h -> h % 24) // 自动调整到0-23范围
                .map(h -> h < 0 ? 0 : h) // 处理负数
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        this.hour = hourStr;
        return this;
    }
    
    /**
     * 设置多个执行分钟点（自动调整参数）
     * @param minutes 分钟数组，超出范围的值会自动调整到0-59之间
     */
    public CronBuilder atMinutes(int... minutes) {
        String minuteStr = Arrays.stream(minutes)
                .map(m -> m % 60) // 自动调整到0-59范围
                .map(m -> m < 0 ? 0 : m) // 处理负数
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        this.minute = minuteStr;
        return this;
    }
    
    /**
     * 设置多个执行秒点（自动调整参数）
     * @param seconds 秒数组，超出范围的值会自动调整到0-59之间
     */
    public CronBuilder atSeconds(int... seconds) {
        String secondStr = Arrays.stream(seconds)
                .map(s -> s % 60) // 自动调整到0-59范围
                .map(s -> s < 0 ? 0 : s) // 处理负数
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        this.second = secondStr;
        return this;
    }
    
    // ============ 范围设置方法 ============
    
    /**
     * 设置小时范围（自动调整参数）
     * @param startHour 开始小时，会自动调整到0-23范围
     * @param endHour 结束小时，会自动调整到0-23范围
     */
    public CronBuilder betweenHours(int startHour, int endHour) {
        // 自动调整范围
        startHour = Math.max(0, Math.min(23, startHour));
        endHour = Math.max(0, Math.min(23, endHour));
        
        // 如果开始时间>=结束时间，自动交换
        if (startHour >= endHour) {
            int temp = startHour;
            startHour = endHour;
            endHour = temp;
        }
        
        this.hour = startHour + "-" + endHour;
        return this;
    }
    
    /**
     * 设置分钟范围（自动调整参数）
     * @param startMinute 开始分钟，会自动调整到0-59范围
     * @param endMinute 结束分钟，会自动调整到0-59范围
     */
    public CronBuilder betweenMinutes(int startMinute, int endMinute) {
        // 自动调整范围
        startMinute = Math.max(0, Math.min(59, startMinute));
        endMinute = Math.max(0, Math.min(59, endMinute));
        
        // 如果开始时间>=结束时间，自动交换
        if (startMinute >= endMinute) {
            int temp = startMinute;
            startMinute = endMinute;
            endMinute = temp;
        }
        
        this.minute = startMinute + "-" + endMinute;
        return this;
    }
    
    // ============ 高级方法 ============
    
    /**
     * 自定义cron表达式的某个字段（自动处理冲突）
     * @param field 字段名称
     * @param value 字段值
     */
    public CronBuilder custom(CronField field, String value) {
        switch (field) {
            case SECOND:
                this.second = value;
                break;
            case MINUTE:
                this.minute = value;
                break;
            case HOUR:
                this.hour = value;
                break;
            case DAY_OF_MONTH:
                this.dayOfMonth = value;
                // 设置月日时，清除周设置以避免冲突
                if (!"?".equals(value) && !"*".equals(value)) {
                    this.dayOfWeek = "?";
                }
                break;
            case MONTH:
                this.month = value;
                break;
            case DAY_OF_WEEK:
                this.dayOfWeek = value;
                // 设置周时，清除月日设置以避免冲突
                if (!"?".equals(value) && !"*".equals(value)) {
                    this.dayOfMonth = "?";
                }
                break;
            case YEAR:
                this.year = value;
                break;
        }
        return this;
    }
    
    /**
     * 构建并返回Cron表达式
     * @return Cron表达式字符串
     * @throws IllegalStateException 如果构建的表达式无效
     */
    public String build() {
        // 冲突已经在各个设置方法中处理了，这里只做最后的兜底检查
        String finalDayOfMonth = dayOfMonth;
        String finalDayOfWeek = dayOfWeek;
        
        // 兜底检查：如果仍有冲突，优先保留 day-of-week 设置
        if (!"?".equals(dayOfWeek) && !"*".equals(dayOfWeek) && 
            !"?".equals(dayOfMonth) && !"*".equals(dayOfMonth)) {
            // 同时设置了具体的月日和周，优先保留周设置
            finalDayOfMonth = "?";
        }
        
        StringBuilder cronExpression = new StringBuilder();
        cronExpression.append(second).append(" ")
                     .append(minute).append(" ")
                     .append(hour).append(" ")
                     .append(finalDayOfMonth).append(" ")
                     .append(month).append(" ")
                     .append(finalDayOfWeek);
        
        if (year != null && !year.isEmpty()) {
            cronExpression.append(" ").append(year);
        }
        
        String cron = cronExpression.toString();
        
        // 验证Cron表达式是否有效
        try {
            new CronExpression(cron);
        } catch (ParseException e) {
            throw new IllegalStateException("构建的Cron表达式无效: " + cron, e);
        }
        
        return cron;
    }
    
    /**
     * Cron表达式字段枚举
     */
    public enum CronField {
        SECOND, MINUTE, HOUR, DAY_OF_MONTH, MONTH, DAY_OF_WEEK, YEAR
    }
    
    // ============ 常用预设方法 ============
    
    /**
     * 每秒执行
     */
    public static String everySecond() {
        return create().everySeconds(1).build();
    }
    
    /**
     * 每分钟执行
     */
    public static String everyMinute() {
        return create().everyMinutes(1).build();
    }
    
    /**
     * 每小时执行
     */
    public static String everyHour() {
        return create().everyHours(1).build();
    }
    
    /**
     * 每天午夜执行
     */
    public static String everyDayAtMidnight() {
        return create().daily().at(0, 0, 0).build();
    }
    
    /**
     * 每天中午执行
     */
    public static String everyDayAtNoon() {
        return create().daily().at(12, 0, 0).build();
    }
    
    /**
     * 工作日上午9点执行
     */
    public static String weekdaysAt9AM() {
        return create().weekdays().at(9, 0, 0).build();
    }
    
    /**
     * 每周一上午9点执行
     */
    public static String mondayAt9AM() {
        return create().weekly(2).at(9, 0, 0).build(); // 2=周一
    }
    
    /**
     * 每月1号上午9点执行
     */
    public static String monthlyFirstDayAt9AM() {
        return create().monthly(1).at(9, 0, 0).build();
    }
    
    /**
     * 每年1月1日午夜执行（新年）
     */
    public static String newYear() {
        return create().yearly().inMonth(1).monthly(1).at(0, 0, 0).build();
    }
    
    /**
     * 春季开始执行（3月1日）
     */
    public static String springStart() {
        return create().yearly().inMonth(3).monthly(1).at(0, 0, 0).build();
    }
    
    /**
     * 夏季开始执行（6月1日）
     */
    public static String summerStart() {
        return create().yearly().inMonth(6).monthly(1).at(0, 0, 0).build();
    }
    
    /**
     * 秋季开始执行（9月1日）
     */
    public static String autumnStart() {
        return create().yearly().inMonth(9).monthly(1).at(0, 0, 0).build();
    }
    
    /**
     * 冬季开始执行（12月1日）
     */
    public static String winterStart() {
        return create().yearly().inMonth(12).monthly(1).at(0, 0, 0).build();
    }
    
    /**
     * 每季度第一天执行
     */
    public static String quarterlyStart() {
        return create().yearly().inMonths(1, 4, 7, 10).monthly(1).at(0, 0, 0).build();
    }
    
    /**
     * 每年生日提醒
     * @param month 生日月份
     * @param day 生日日期
     * @param hour 提醒小时
     * @param minute 提醒分钟
     */
    public static String birthdayReminder(int month, int day, int hour, int minute) {
        return create().yearly().inMonth(month).monthly(day).at(hour, minute, 0).build();
    }
    
    /**
     * 工作日早晨提醒
     * @param hour 小时
     * @param minute 分钟
     */
    public static String weekdayMorningReminder(int hour, int minute) {
        return create().weekdays().at(hour, minute, 0).build();
    }
    
    /**
     * 周末晚上提醒
     * @param hour 小时
     * @param minute 分钟
     */
    public static String weekendEveningReminder(int hour, int minute) {
        return create().weekends().at(hour, minute, 0).build();
    }
    
    // ============ 业务场景预设方法 ============
    
    /**
     * 数据备份时间（每天凌晨2点）
     */
    public static String dataBackupTime() {
        return create().daily().at(2, 0, 0).build();
    }
    
    /**
     * 系统维护时间（每周日凌晨3点）
     */
    public static String systemMaintenanceTime() {
        return create().weekly(1).at(3, 0, 0).build(); // 1=周日
    }
    
    /**
     * 日志清理时间（每月最后一天23:30）
     */
    public static String logCleanupTime() {
        return create().monthlyLastDay().at(23, 30, 0).build();
    }
    
    /**
     * 报表生成时间（工作日上午8点）
     */
    public static String reportGenerationTime() {
        return create().weekdays().at(8, 0, 0).build();
    }
    
    /**
     * 缓存刷新时间（每小时整点）
     */
    public static String cacheRefreshTime() {
        return create().everyHours(1).build();
    }
    
    /**
     * 健康检查时间（每5分钟）
     */
    public static String healthCheckTime() {
        return create().everyMinutes(5).build();
    }
    
    /**
     * 会议提醒时间（工作日指定时间）
     */
    public static String meetingReminderTime(int hour, int minute) {
        return create().weekdays().at(hour, minute, 0).build();
    }
    
    /**
     * 月度报告时间（每月1号上午9点）
     */
    public static String monthlyReportTime() {
        return create().monthly(1).at(9, 0, 0).build();
    }
    
    /**
     * 季度总结时间（每季度最后一个月的最后一天）
     */
    public static String quarterlyReportTime() {
        return create().inMonths(3, 6, 9, 12).monthlyLastDay().at(17, 0, 0).build();
    }
    
    /**
     * 年终总结时间（12月31日下午5点）
     */
    public static String yearEndReportTime() {
        return create().inMonth(12).monthly(31).at(17, 0, 0).build();
    }
    
    // ============ 节假日和特殊日期 ============
    
    /**
     * 情人节提醒（2月14日上午9点）
     */
    public static String valentinesDay() {
        return birthdayReminder(2, 14, 9, 0);
    }
    
    /**
     * 圣诞节提醒（12月25日上午9点）
     */
    public static String christmasDay() {
        return birthdayReminder(12, 25, 9, 0);
    }
    
    /**
     * 母亲节提醒（5月第二个周日，简化为5月8-14日的周日）
     */
    public static String mothersDay() {
        return create().inMonth(5).custom(CronField.DAY_OF_MONTH, "8-14").custom(CronField.DAY_OF_WEEK, "1").at(9, 0, 0).build();
    }
    
    /**
     * 感恩节提醒（11月第四个周四，简化为11月22-28日的周四）
     */
    public static String thanksgivingDay() {
        return create().inMonth(11).custom(CronField.DAY_OF_MONTH, "22-28").custom(CronField.DAY_OF_WEEK, "5").at(9, 0, 0).build();
    }
    
    /**
     * 月薪发放日提醒（每月最后一个工作日下午4点）
     */
    public static String salaryDay() {
        // 简化实现：每月25-31日的工作日下午4点
        return create().custom(CronField.DAY_OF_MONTH, "25-31").custom(CronField.DAY_OF_WEEK, "2-6").at(16, 0, 0).build();
    }
    
    // ============ 月份常量 ============
    
    public static final class Months {
        public static final int JANUARY = 1;
        public static final int FEBRUARY = 2;
        public static final int MARCH = 3;
        public static final int APRIL = 4;
        public static final int MAY = 5;
        public static final int JUNE = 6;
        public static final int JULY = 7;
        public static final int AUGUST = 8;
        public static final int SEPTEMBER = 9;
        public static final int OCTOBER = 10;
        public static final int NOVEMBER = 11;
        public static final int DECEMBER = 12;
    }
    
    // ============ 星期常量 ============
    
    public static final class DaysOfWeek {
        public static final int SUNDAY = 1;
        public static final int MONDAY = 2;
        public static final int TUESDAY = 3;
        public static final int WEDNESDAY = 4;
        public static final int THURSDAY = 5;
        public static final int FRIDAY = 6;
        public static final int SATURDAY = 7;
    }
    
    // ============ 时间常量 ============
    
    public static final class Times {
        public static final int MIDNIGHT = 0;
        public static final int DAWN = 6;
        public static final int MORNING = 9;
        public static final int NOON = 12;
        public static final int AFTERNOON = 15;
        public static final int EVENING = 18;
        public static final int NIGHT = 21;
    }
}