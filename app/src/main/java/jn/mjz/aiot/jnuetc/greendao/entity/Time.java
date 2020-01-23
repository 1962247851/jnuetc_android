package jn.mjz.aiot.jnuetc.greendao.entity;

import android.os.Handler;

/**
 * @author 19622
 */
public class Time {
    private int day = 0;
    private int hour = 0;
    private int minute = 0;
    private int second = 0;

    private Handler handler = new Handler();
    private boolean pause = false;
    private static final int MAX_HOUR = 24;
    private static final int MAX_MINUTE = 60;
    private static final int MAX_SECOND = 60;


    public Time() {
    }


    public Time(int day, int hour, int minute, int second) {
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }


    public void startTiming(IOnUpdateListener i) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!pause) {
                    addSecond();
                    i.onAdded(Time.this);
                    handler.postDelayed(this, 1000);
                }
            }
        };
        runnable.run();

    }

    public void endTiming() {
        pause = true;
    }

    private void addSecond() {
        if (++second >= MAX_SECOND) {
            second = 0;
            addMinute();
        }
    }

    private void addMinute() {
        if (++minute >= MAX_MINUTE) {
            minute = 0;
            addHour();
        }
    }

    private void addHour() {
        if (++hour >= MAX_HOUR) {
            hour = 0;
            addDay();
        }
    }

    private void addDay() {
        day++;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }


    public interface IOnUpdateListener {
        /**
         * 增加
         *
         * @param time Time
         */
        void onAdded(Time time);
    }
}
