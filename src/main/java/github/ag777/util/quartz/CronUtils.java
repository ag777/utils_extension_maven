package github.ag777.util.quartz;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Cron表达式计算工具类
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/8/1 15:54
 */
public class CronUtils {

    /**
     * 根据Cron表达式计算下次执行时间，并以指定格式返回。
     *
     * @param cronExpression Cron表达式
     * @param currentTime 当前时间点，用于计算下次执行时间的基准
     * @param dateFormat 返回日期的格式
     * @return 下一次执行时间的Optional封装，以指定格式的字符串表示。如果Cron表达式无效，则返回Optional.empty()
     * @throws ParseException 如果Cron表达式解析失败
     */
    public static Optional<String> calculateNextFireTimeFormatted(String cronExpression, Date currentTime, String dateFormat) throws ParseException {
        Optional<Date> date = calculateNextFireTime(cronExpression, currentTime);
        if (date.isPresent()) {
            return formatOptionalDate(date.get(), dateFormat);
        }
        return Optional.empty();
    }

    /**
     * 根据Cron表达式计算下次执行时间。
     *
     * @param cronExpression Cron表达式
     * @param currentTime 当前时间点，用于计算下次执行时间的基准
     * @return 下一次执行时间的Optional封装，如果Cron表达式无效，则返回Optional.empty()
     * @throws ParseException 如果Cron表达式解析失败
     */
    public static Optional<Date> calculateNextFireTime(String cronExpression, Date currentTime) throws ParseException {
        CronExpression cron = new CronExpression(cronExpression);
        Date nextValidTimeAfter = cron.getNextValidTimeAfter(currentTime);
        return Optional.ofNullable(nextValidTimeAfter);
    }

    /**
     * 辅助方法，用于将Date对象格式化为字符串。
     *
     * @param date 需要格式化的日期
     * @param dateFormat 日期格式
     * @return 格式化后日期的Optional封装。如果日期为null，则返回Optional.empty()
     */
    private static Optional<String> formatOptionalDate(Date date, String dateFormat) {
        if (date == null) {
            return Optional.empty();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return Optional.of(formatter.format(date));
    }

    /**
     * 判断Cron表达式是否为周期性任务
     *
     * @param cronExpression Cron表达式
     * @return true表示周期性任务，false表示一次性任务
     * @throws ParseException 如果Cron表达式解析失败
     */
    /**
     * 判断Cron表达式是否为周期性任务
     * 通过分析表达式结构判断，比计算下次执行时间更高效
     *
     * @param cronExpression Cron表达式
     * @return true表示周期性任务，false表示一次性任务
     * @throws ParseException 如果Cron表达式解析失败
     */
    public static boolean isRecurringTask(String cronExpression) throws ParseException {
        return !isOneTimeTask(cronExpression);
    }

    /**
     * 判断Cron表达式是否为一次性任务
     * 通过分析表达式结构判断，比计算下次执行时间更高效
     *
     * @param cronExpression Cron表达式
     * @return true表示一次性任务，false表示周期性任务
     * @throws ParseException 如果Cron表达式解析失败
     */
    public static boolean isOneTimeTask(String cronExpression) throws ParseException {
        String[] fields = parseCronExpression(cronExpression);
        return isOneTimeYear(fields) && hasOnlySpecificValues(fields);
    }

    /**
     * 解析Cron表达式为字段数组
     *
     * @param cronExpression Cron表达式
     * @return Cron表达式字段数组
     */
    private static String[] parseCronExpression(String cronExpression) {
        return cronExpression.split("\\s+");
    }

    /**
     * 判断年份字段是否为一次性（单个具体年份）
     *
     * @param fields Cron表达式字段数组
     * @return true表示是一次性年份，false表示是周期性年份或无年份
     */
    private static boolean isOneTimeYear(String[] fields) {
        if (fields.length < 7) {
            return true;  // 无年份字段视为一次性年份
        }
        String yearField = fields[6];
        return !yearField.equals("*") && 
               !yearField.contains(",") && 
               !yearField.contains("/") && 
               !yearField.contains("-");
    }

    /**
     * 检查是否所有字段都是具体值（不包含通配符、列表、范围等）
     *
     * @param fields Cron表达式字段数组
     * @return true表示都是具体值，false表示包含特殊字符
     */
    private static boolean hasOnlySpecificValues(String[] fields) {
        for (int i = 0; i < fields.length && i < 6; i++) {
            String field = fields[i];
            // 跳过日期和星期的互斥字段（问号字符）
            if ((i == 3 || i == 5) && field.equals("?")) {
                continue;
            }
            // 如果字段包含特殊字符，说明不是具体值
            if (containsSpecialCharacters(field)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查字段是否包含特殊字符
     *
     * @param field Cron表达式字段
     * @return true表示包含特殊字符，false表示不包含
     */
    private static boolean containsSpecialCharacters(String field) {
        return field.contains("*") || 
               field.contains(",") || 
               field.contains("/") || 
               field.contains("-") || 
               field.contains("L") || 
               field.contains("W") || 
               field.contains("#");
    }
}
