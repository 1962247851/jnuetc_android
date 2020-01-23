package jn.mjz.aiot.jnuetc.application;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.youth.xframe.XFrame;
import com.youth.xframe.base.XApplication;
import com.youth.xframe.utils.log.XLog;
import com.youth.xframe.widget.XToast;

import jn.mjz.aiot.jnuetc.greendao.Dao.DaoMaster;
import jn.mjz.aiot.jnuetc.greendao.Dao.DaoSession;
import jn.mjz.aiot.jnuetc.greendao.entity.MingJu;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.util.OkHttpEngine;
import jn.mjz.aiot.jnuetc.util.SharedPreferencesUtil;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class App extends XApplication {

    public static final String DB_NAME = "JNUETC.db";
    private static final String TAG = "App";
    private static DaoSession sDaoSession;
    public static final Boolean DEBUG_MODE = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initGreenDao();
        XFrame.initXHttp(new OkHttpEngine());
        XLog.init().setDebug(DEBUG_MODE);
        LoggerInterface newLogger = new LoggerInterface() {
            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };
        Logger.setLogger(this, newLogger);
    }

    private void initGreenDao() {
        DaoMaster.DevOpenHelper helper =
                new DaoMaster.DevOpenHelper(this, DB_NAME);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        sDaoSession = new DaoMaster(sqLiteDatabase).newSession();
    }

    public static DaoSession getDaoSession() {
        return sDaoSession;
    }

    public static void initToolbar(Toolbar toolbar, AppCompatActivity activity) {

        boolean showMingJu = SharedPreferencesUtil.getSettingPreferences().getBoolean("show_ming_ju", true);

        if (showMingJu) {
            MainViewModel.getMingJu(new HttpUtil.HttpUtilCallBack<MingJu>() {
                @Override
                public void onResponse(MingJu result) {
                    activity.getSupportActionBar().setSubtitle(result.getContent());
                    toolbar.setOnClickListener(v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        String message = "来自：《" + result.getShiName() + "》" + "\n" +
                                "作者：" + result.getAuthor() + "\n" +
                                "话题：" + result.getTopic();
                        builder.setTitle(result.getContent())
                                .setMessage(message)
                                .setNeutralButton("换一句", (dialog, which) -> initToolbar(toolbar, activity))
                                .setPositiveButton("关闭", null)
                                .setNegativeButton("复制诗句", (dialog, which) -> {
                                    MainViewModel.copyToClipboard(activity, result.getContent());
                                    XToast.success("已复制诗句");
                                });
                        builder.create().show();
                    });
                }

                @Override
                public void onFailure(String error) {
                    XToast.error("名句获取失败\n" + error);
                }
            });
        }
    }
}