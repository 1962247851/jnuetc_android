package jn.mjz.aiot.jnuetc.util;

import androidx.annotation.Nullable;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jn.mjz.aiot.jnuetc.greendao.entity.Time;

/**
 * @author 19622
 */
public class DateUtil {

    private Date date;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public DateUtil(long paramLong) {
        this.date = new Date(paramLong);
    }

    public static String getCurrentDate() {
        return (new DateUtil(getCurrentTimeMillis())).getDate();
    }

    public static String getCurrentDate(String paramString) {
        return (new DateUtil(getCurrentTimeMillis())).getDate(paramString);
    }

    public static String getCurrentDateTime() {
        return (new DateUtil(getCurrentTimeMillis())).toDateFormat();
    }

    public static String getCurrentTime() {
        return (new DateUtil(getCurrentTimeMillis())).getTime();
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static String getDate(long paramLong) {
        return (new DateUtil(paramLong)).getDate();
    }

    public static String getDateAndTime(long timeStamp, @Nullable String splitString) {
        StringBuilder stringBuilder = new StringBuilder();
        DateUtil dateUtil = new DateUtil(timeStamp);
        if (splitString == null) {
            stringBuilder.append(dateUtil.getDate());
            stringBuilder.append(dateUtil.getTime());
        } else {
            stringBuilder.append(dateUtil.getDate());
            stringBuilder.append(splitString);
            stringBuilder.append(dateUtil.getTime());
        }
        return stringBuilder.toString();
    }

    public static String getTime(long paramLong) {
        return (new DateUtil(paramLong)).getTime();
    }

    private long subInMilliSeconds(DateUtil paramDateUtil) {
        return getTimeMillis() - paramDateUtil.getTimeMillis();
    }

    public String getDate() {
        return toDateFormat("yyyy/MM/dd");
    }

    public String getDate(String paramString) {
        return toDateFormat(paramString);
    }

    public String getDateTime() {
        return toDateFormat();
    }

    public String getTime() {
        return toDateFormat("HH:mm:ss");
    }

    public long getTimeMillis() {
        return this.date.getTime();
    }

    public Date toDate() {
        return this.date;
    }

    public String toDateFormat() {
        return this.sdf.format(this.date);
    }

    public String toDateFormat(String s) {
        return (new SimpleDateFormat(s)).format(this.date);
    }

    public long toMilliSeconds() {
        return this.date.getTime();
    }

    public Timestamp toTimeStamp() {
        return new Timestamp(this.date.getTime());
    }

    public static Time diffTime(Long timestampStart, Long timestampEnd) {
        //时间差的毫秒数
        double diff = timestampEnd - timestampStart;
        //计算出相差天数
        int days = (int) Math.floor(diff / (24 * 3600 * 1000));
        //计算出小时数
        double leave1 = (diff % (24 * 3600 * 1000));
        //计算天数后剩余的毫秒数
        int hours = (int) Math.floor(leave1 / (3600 * 1000));
        //计算相差分钟数
        double leave2 = leave1 % (3600 * 1000);
        //计算小时数后剩余的毫秒数
        int minutes = (int) Math.floor(leave2 / (60 * 1000));
        //计算相差秒数
        double leave3 = leave2 % (60 * 1000);
        //计算分钟数后剩余的毫秒数
        int seconds = (int) Math.round(leave3 / 1000);
        String returnStr = seconds + "秒";
        if (minutes > 0) {
            returnStr = minutes + "分" + returnStr;
        }
        if (hours > 0) {
            returnStr = hours + "小时" + returnStr;
        }
        if (days > 0) {
            returnStr = days + "天" + returnStr;
        }
        Time time = new Time();
        time.setDay(days);
        time.setHour(hours);
        time.setMinute(minutes);
        time.setSecond(seconds);
        return time;
    }

    /**
     * 获取指定日期所在的一周(周一到周日)
     *
     * @param date date
     * @return ["2020-01-13",
     * "2020-01-14",
     * "2020-01-15",
     * "2020-01-16",
     * "2020-01-17",
     * "2020-01-18",
     * "2020-01-19" ]
     */
    public static List<String> getDateToWeek(Date date) {
        List<String> dateWeekList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String time;
        //count 用来存取与当天日期的相差数
        int count;
        for (int i = 1; i < 8; i++) {
            //新建日历
            Calendar cal = Calendar.getInstance();
            //在日历中找到当前日期
            cal.setTime(date);
            //当前日期是本周第几天，默认按照中国习惯星期一为一周的第一天(末尾的+1的由来)
            count = -cal.get(Calendar.DAY_OF_WEEK) + 1;
            //循环。当天与本周周一到周日相差的天数
            cal.add(Calendar.DATE, count + i);
            //转化格式
            time = sdf.format(cal.getTime());
            //存入list
            dateWeekList.add(time);
        }
        return dateWeekList;
    }
}
