package jn.mjz.aiot.jnuetc.Application;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.youth.xframe.XFrame;

import jn.mjz.aiot.jnuetc.Greendao.Dao.DaoMaster;
import jn.mjz.aiot.jnuetc.Greendao.Dao.DaoSession;
import jn.mjz.aiot.jnuetc.Util.OKHttpEngine;

public class MyApplication extends Application {
    public static final String DB_NAME = "JNUETC.db";
    private static final String TAG = "MyApplication";
    private static DaoSession sDaoSession;


    @Override
    public void onCreate() {
        super.onCreate();
        initGreenDao();
        XFrame.init(this);
        XFrame.initXHttp(new OKHttpEngine());//不加会在响应成功后更新UI出错

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
}