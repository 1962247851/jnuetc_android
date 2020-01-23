package jn.mjz.aiot.jnuetc.greendao.entity;

import com.youth.xframe.XFrame;

import java.util.ArrayList;
import java.util.List;

import jn.mjz.aiot.jnuetc.R;

/**
 * @author qq1962247851
 * @date 2020/1/16 11:16
 */
public class OrderTimeRankingInfo {

    public static final int COLOR_MORNING = XFrame.getColor(android.R.color.holo_blue_light);
    public static final int COLOR_NOON = XFrame.getColor(R.color.LightYellow);
    public static final int COLOR_AFTERNOON = XFrame.getColor(R.color.Orange);
    public static final int COLOR_EVENING = XFrame.getColor(R.color.DarkBlue);
    public static final String MORNING_LABEL_DESCRIPTION = "上午";
    public static final String NOON_LABEL_DESCRIPTION = "中午";
    public static final String AFTERNOON_LABEL_DESCRIPTION = "下午";
    public static final String EVENING_LABEL_DESCRIPTION = "夜晚";
    public static final String MORNING_LABEL = "上午6点-12点";
    public static final String NOON_LABEL = "中午12点-15点";
    public static final String AFTERNOON_LABEL = "下午15点-18点";
    public static final String EVENING_LABEL = "夜晚18点-6点";

    /**
     * 6-12
     */
    private List<Data> morningDataList;
    /**
     * 12-15
     */
    private List<Data> noonDataList;
    /**
     * 15-18
     */
    private List<Data> afterNoonDataList;
    /**
     * 18-24 24-6
     */
    private List<Data> eveningDataList;

    public OrderTimeRankingInfo() {
        this.morningDataList = new ArrayList<>();
        this.noonDataList = new ArrayList<>();
        this.afterNoonDataList = new ArrayList<>();
        this.eveningDataList = new ArrayList<>();
    }

    public List<Data> getMorningDataList() {
        return morningDataList;
    }

    public void setMorningDataList(List<Data> morningDataList) {
        this.morningDataList = morningDataList;
    }

    public List<Data> getNoonDataList() {
        return noonDataList;
    }

    public void setNoonDataList(List<Data> noonDataList) {
        this.noonDataList = noonDataList;
    }

    public List<Data> getAfterNoonDataList() {
        return afterNoonDataList;
    }

    public void setAfterNoonDataList(List<Data> afterNoonDataList) {
        this.afterNoonDataList = afterNoonDataList;
    }

    public List<Data> getEveningDataList() {
        return eveningDataList;
    }

    public void setEveningDataList(List<Data> eveningDataList) {
        this.eveningDataList = eveningDataList;
    }
}
