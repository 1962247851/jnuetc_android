package jn.mjz.aiot.jnuetc.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.JsonObject;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.utils.XDateUtils;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.http.XHttp;
import com.youth.xframe.utils.log.XLog;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.util.List;

import jn.mjz.aiot.jnuetc.greendao.entity.Version;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class UpdateUtil {


    private static UpdateUtil instance;

    public static UpdateUtil getInstance() {
        if (instance == null) {
            instance = new UpdateUtil();
        }
        return instance;
    }

    private static final String TAG = "UpdateUtil";

    public static void checkForUpdate(boolean firstOpen, Context context, @Nullable IServerAvailableListener iServerAvailableListener) {
        if (!firstOpen) {
            XLoadingDialog.with(context).setMessage("检查更新中，请稍后").setCanceled(false).show();
        }
        UpdateUtil.checkForUpdate(new UpdateUtil.IUpdateListener() {
            @Override
            public void haveNewVersion(Version version) {
                if (!firstOpen) {
                    XLoadingDialog.with(context).cancel();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String[] messages = version.getMessage().split("。");
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : messages) {
                    stringBuilder.append(s)
                            .append("\n");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                builder.setCancelable(false)
                        .setOnKeyListener((dialogInterface, i, keyEvent) -> keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
                        .setMessage("更新时间：" + XDateUtils.millis2String(version.getDate(), "yyyy/MM/dd HH:mm:ss") + "\n\n" + stringBuilder.toString())
                        .setTitle("发现新版本" + version.getVersion())
                        .setPositiveButton("前往下载", (dialogInterface, i) -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(version.getUrl()))));

                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                //防止点击消失
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    //进入浏览器下载
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(version.getUrl())));
                });
            }


            @Override
            public void noUpdate() {
                if (iServerAvailableListener != null) {
                    iServerAvailableListener.onServerValid();
                }
                if (!firstOpen) {
                    XLoadingDialog.with(context).dismiss();
                    XToast.success("当前是最新版本");
                }
            }

            @Override
            public void develop() {
                if (iServerAvailableListener != null) {
                    iServerAvailableListener.onServerValid();
                }
                if (!firstOpen) {
                    XLoadingDialog.with(context).dismiss();
                    XToast.success("当前是开发版本");
                }
            }

            @Override
            public void error() {
                if (iServerAvailableListener != null) {
                    iServerAvailableListener.onServerInvalid();
                }
                if (!firstOpen) {
                    XLoadingDialog.with(context).dismiss();
                    XToast.error("检查更新失败");
                }
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setOnKeyListener((dialogInterface, i, keyEvent) -> keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
                        .setTitle("注意")
                        .setMessage("检查更新失败或服务器不可用，请重试或下载最新版本\n当前版本：" + XAppUtils.getVersionName(context) + "\n开发者QQ：1962247851\n")
                        .setNeutralButton("重试", (dialog1, which) -> checkForUpdate(firstOpen, context, iServerAvailableListener))
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
        XHttp.obtain().post(GlobalUtil.Urls.Version.QUERY_ALL, null, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    List<Version> versionList = GsonUtil.parseJsonArray2ObjectList(jsonObject.get("body").getAsString(), Version.class);
                    if (versionList == null || versionList.isEmpty()) {
                        updateListener.noUpdate();
                    } else {
                        Version newVersion = versionList.get(versionList.size() - 1);
                        float newVersionCode = newVersion.getId();
                        float localVersionCode = XAppUtils.getVersionCode(XFrame.getContext());
//                        float localVersionName = Float.parseFloat(XAppUtils.getVersionName(XFrame.getContext()));
                        if (newVersionCode > localVersionCode) {
                            updateListener.haveNewVersion(newVersion);
                        } else if (localVersionCode > newVersionCode) {
                            updateListener.develop();
                        } else {
                            updateListener.noUpdate();
                        }
                    }
                } else {
                    updateListener.error();
                }
            }

            @Override
            public void onFailed(String error) {
                XLog.e(error);
                updateListener.error();
            }
        });
    }

    public static void checkHistory(IHistoryListener iHistoryListener) {
        XHttp.obtain().post(GlobalUtil.Urls.Version.QUERY_ALL, null, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                XLog.json(jsonObject.toString());
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    StringBuilder builder = new StringBuilder();
                    List<Version> versionList = GsonUtil.parseJsonArray2ObjectList(jsonObject.get("body").getAsString(), Version.class);
                    if (versionList != null && !versionList.isEmpty()) {
                        for (int i = 0; i < versionList.size(); i++) {
                            Version version = versionList.get(i);
                            if (i != 0) {
                                builder.append("\n\n");
                            }
                            String[] messages = version.getMessage().split("。");
                            builder.append("版本：")
                                    .append(version.getVersion())
                                    .append("（")
                                    .append(DateUtil.getDateAndTime(version.getDate(), " "))
                                    .append("）\n更新内容：\n");
                            for (String message : messages) {
                                builder.append(message).append("\n");
                            }
                            builder.deleteCharAt(builder.length() - 1);
                        }
                        iHistoryListener.success(builder.toString());
                    } else {
                        iHistoryListener.success("暂无更新日志");
                    }
                } else {
                    iHistoryListener.error();
                }
            }

            @Override
            public void onFailed(String error) {
                XLog.e(error);
                iHistoryListener.error();
                XToast.error("数据获取失败");
            }
        });
    }

    public interface IServerAvailableListener {
        /**
         * 服务器不可用
         */
        void onServerInvalid();

        /**
         * 服务器正常
         */
        void onServerValid();
    }

    public interface IUpdateListener {
        /**
         * 有新版本
         *
         * @param newVersion 最新版本
         */
        void haveNewVersion(Version newVersion);

        /**
         * 没有更新
         */
        void noUpdate();

        /**
         * 内测版本
         */
        void develop();


        /**
         * 错误
         */
        void error();
    }

    public interface IHistoryListener {
        /**
         * 成功
         *
         * @param historyString 所有更新日志
         */
        void success(String historyString);

        /**
         * 错误
         */
        void error();
    }
}
