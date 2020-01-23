package jn.mjz.aiot.jnuetc.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.youth.xframe.base.XApplication;

import jn.mjz.aiot.jnuetc.view.activity.SettingsActivity;

/**
 * @author 19622
 */
public class SharedPreferencesUtil {

    /**
     * 以MODE_PRIVATE方式访问SharedPreferences
     *
     * @param name 文件名
     * @return SharedPreferences
     */
    public static SharedPreferences getSharedPreferences(String name) {
        return XApplication.getInstance().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * 获取设置
     *
     * @return SharedPreferences
     */
    public static SharedPreferences getSettingPreferences() {
        return XApplication.getInstance().getSharedPreferences(SettingsActivity.sharedPreferencesName, Context.MODE_PRIVATE);
    }
}
