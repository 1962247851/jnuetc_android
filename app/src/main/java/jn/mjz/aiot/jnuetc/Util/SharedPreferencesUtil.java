package jn.mjz.aiot.jnuetc.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.youth.xframe.XFrame;

import jn.mjz.aiot.jnuetc.View.Activity.SettingsActivity;

public class SharedPreferencesUtil {
    public static SharedPreferences getSharedPreferences(String name) {
        return XFrame.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSettingPreferences() {
        return XFrame.getContext().getSharedPreferences(SettingsActivity.sharedPreferencesName, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences(Context context, String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
}
