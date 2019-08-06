package jn.mjz.aiot.jnuetc.Util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.JsonObject;
import com.youth.xframe.XFrame;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class UpdateUtil {

    public static void checkForUpdate(IUpdateListener updateListener) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("id", 1);
        HttpUtil.post.haveResponse(GlobalUtil.URLS.QUERY.CHECK_FOR_UPDATE, p, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, String result) {
                String state = response.headers().get("state");
                if (state != null && state.equals("OK")) {
//                    Log.e("versionResult: ", result);
                    JsonObject jsonObject = GsonUtil.getInstance().fromJson(result, JsonObject.class);
                    float newVersion = Float.parseFloat(jsonObject.get("version").getAsString());
                    float localVersion = Float.parseFloat(UpdateUtil.getLocalVersionName(XFrame.getContext()));
                    if (newVersion > localVersion) {
                        updateListener.HaveNewVersion(jsonObject.get("url").getAsString(), jsonObject.get("message").getAsString(),newVersion);
                    } else if (newVersion == localVersion) {
                        updateListener.NoUpdate();
                    }
                } else {
                    updateListener.Error();
                }
            }

            @Override
            public void onFailure(IOException e) {
                updateListener.Error();
            }
        });
    }

    /* 获取本地软件版本号​名字
     */
    private static String getLocalVersionName(Context ctx) {
        String localVersionName = null;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersionName;
    }

    public interface IUpdateListener {
        void HaveNewVersion(String url,String message, float newVersion);

        void NoUpdate();

        void Error();
    }
}
