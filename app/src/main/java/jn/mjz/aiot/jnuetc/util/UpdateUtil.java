package jn.mjz.aiot.jnuetc.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;

import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;
import okhttp3.Response;

import static com.google.android.material.snackbar.Snackbar.LENGTH_SHORT;

public class UpdateUtil {

    private static final String TAG = "UpdateUtil";
    private static String FILE_NAME = "update";

    public static void checkForUpdate(boolean firstOpen, Context context, View snackBarView, @Nullable IUpdateErrorListener iUpdateErrorListener) {

        if (!firstOpen) {
            XLoadingDialog.with(context).setMessage("检查更新中，请稍后").setCanceled(false).show();
        }

        UpdateUtil.checkForUpdate(new UpdateUtil.IUpdateListener() {
            @Override
            public void HaveNewVersion(String date, String url, String message, float newVersion) {

                if (!firstOpen) {
                    XLoadingDialog.with(context).dismiss();
                }

                SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(FILE_NAME);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String[] messages = message.split("。");
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : messages) {
                    stringBuilder.append(s)
                            .append("\n");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                builder.setCancelable(false)
                        .setOnKeyListener((dialogInterface, i, keyEvent) -> keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
                        .setMessage("更新时间：" + date + "\n\n" + stringBuilder.toString())
                        .setTitle("发现新版本" + newVersion)
//                        .setNeutralButton("不再提醒该版本", (dialogInterface, i) -> {
//                            editor.putBoolean(String.valueOf(newVersion), false);
//                            editor.apply();
//                            XToast.success("屏蔽成功");
//                        })
                        .setPositiveButton("前往下载", (dialogInterface, i) -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));

                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                if (sharedPreferences.getBoolean(String.valueOf(newVersion), true)) {
                    alertDialog.show();
                    //防止点击消失
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        }
                    });

                } else if (!firstOpen) {
                    Snackbar.make(snackBarView, "发现新版本（已屏蔽）", LENGTH_SHORT)
                            .setAction("取消屏蔽", view -> {
                                editor.putBoolean(String.valueOf(newVersion), true);
                                editor.apply();
                                alertDialog.show();
                            })
                            .show();
                }
            }


            @Override
            public void NoUpdate() {
                if (iUpdateErrorListener != null) {
                    iUpdateErrorListener.onServerValid();
                }
                if (!firstOpen) {
                    XLoadingDialog.with(context).dismiss();
                    XToast.success("当前是最新版本");
                }
            }

            @Override
            public void Error() {
                if (iUpdateErrorListener != null) {
                    iUpdateErrorListener.onServerInvalid();
                }
                if (!firstOpen) {
                    XLoadingDialog.with(context).dismiss();
                    XToast.error("检查更新失败");
                }
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setOnKeyListener((dialogInterface, i, keyEvent) -> keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
                        .setTitle("注意")
                        .setMessage("检查更新失败或服务器不可用，请下载最新版本或联系管理员（QQ：1962247851）\n当前版本：" + XAppUtils.getVersionName(context))
                        .setPositiveButton("复值QQ号并打开QQ", null)
                        .setCancelable(false)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    MainViewModel.copyToClipboard(context, "1962247851");
                    if (XAppUtils.isInstallApp("com.tencent.mobileqq")) {
                        XAppUtils.startApp("com.tencent.mobileqq");
                        XToast.success(String.format("QQ：%s已复制到剪切板", "1962247851"));
                    } else {
                        XToast.error("未安装手Q或安装的版本不支持");
                    }
                });
            }
        });
    }

    public static void checkForUpdate(IUpdateListener updateListener) {
        HttpUtil.post.haveResponse(GlobalUtil.URLS.QUERY.CHECK_FOR_UPDATE, null, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, String result) {
                String state = response.headers().get("state");
                if ("OK".equals(state)) {
                    JsonObject jsonObject = GsonUtil.getInstance().fromJson(result, JsonObject.class);
                    float newVersion = Float.parseFloat(jsonObject.get("version").getAsString());
                    float localVersion = Float.parseFloat(XAppUtils.getVersionName(XFrame.getContext()));
                    if (newVersion > localVersion) {
                        updateListener.HaveNewVersion(DateUtil.getDateAndTime(jsonObject.get("date").getAsLong(), " "), jsonObject.get("url").getAsString(), jsonObject.get("message").getAsString(), newVersion);
                    } else {
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

    public static void checkHistory(IHistoryListener iHistoryListener) {
        HttpUtil.post.haveResponse(GlobalUtil.URLS.QUERY.CHECK_HISTORY, null, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, String result) {
                String state = response.headers().get("state");
                if ("OK".equals(state)) {
                    StringBuilder builder = new StringBuilder();
                    JsonArray jsonArray = GsonUtil.getInstance().fromJson(result, JsonArray.class);

                    for (int i = 0; i < jsonArray.size(); i++) {
                        String[] messages = jsonArray.get(i).getAsJsonObject().get("message").getAsString().split("。");
                        builder.append("版本：")
                                .append(jsonArray.get(i).getAsJsonObject().get("version").getAsString())
                                .append("（")
                                .append(DateUtil.getDateAndTime(jsonArray.get(i).getAsJsonObject().get("date").getAsLong(), " "))
                                .append("）\n更新内容：\n");
                        for (String s : messages) {
                            builder.append(s).append("\n");
                        }
                        builder.deleteCharAt(builder.length() - 1);
//                                .append(jsonArray.get(i).getAsJsonObject().get("message").getAsString())
                        builder.append(i == jsonArray.size() - 1 ? "" : "\n\n");
                    }
                    iHistoryListener.Success(builder.toString());
                } else {
                    iHistoryListener.Error();
                }
            }

            @Override
            public void onFailure(IOException e) {
                XToast.error("数据获取失败");
                iHistoryListener.Error();
            }
        });
    }

    public interface IUpdateErrorListener {
        void onServerInvalid();

        void onServerValid();
    }

    public interface IUpdateListener {
        void HaveNewVersion(String date, String url, String message, float newVersion);

        void NoUpdate();

        void Error();
    }

    public interface IHistoryListener {
        void Success(String historyString);

        void Error();
    }
}
