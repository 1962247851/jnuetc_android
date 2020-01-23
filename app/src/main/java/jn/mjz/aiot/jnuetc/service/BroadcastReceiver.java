package jn.mjz.aiot.jnuetc.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import com.youth.xframe.common.XActivityStack;
import com.youth.xframe.utils.log.XLog;

import java.util.List;

import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.view.activity.DetailsActivity;
import jn.mjz.aiot.jnuetc.view.activity.MainActivity;
import jn.mjz.aiot.jnuetc.view.fragment.DataChangeLogFragment;
import jn.mjz.aiot.jnuetc.view.fragment.DetailsFragment;

/**
 * @author 19622
 */
public class BroadcastReceiver extends PushMessageReceiver {
    private String mRegId;
    private long mResultCode = -1;
    private String mReason;
    private String mCommand;
    private String mMessage;
    private String mTopic;
    private String mAlias;
    private String mUserAccount;
    private String mStartTime;
    private String mEndTime;

    /**
     * 透传消息到达手机端后，SDK会将消息通过广播方式传给AndroidManifest中注册的PushMessageReceiver的子类的{@link #onReceivePassThroughMessage(Context, MiPushMessage)}
     *
     * @param context
     * @param message
     */
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        XLog.json(GsonUtil.getInstance().toJson(message));
        mMessage = message.getContent();
        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        } else if (!TextUtils.isEmpty(message.getUserAccount())) {
            mUserAccount = message.getUserAccount();
        }
    }

    /**
     * 用户点击之后再传给您的PushMessageReceiver的子类的{@link #onNotificationMessageClicked(Context, MiPushMessage)}
     *
     * @param context
     * @param message
     */
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {

        Intent intent = new Intent(context, DetailsActivity.class);
        Intent intentMain = new Intent(context, MainActivity.class);
        Data data = GsonUtil.getInstance().fromJson(message.getContent(), Data.class);
        DataChangeLogFragment.sortLogByTimeDesc(data.getDataChangeLogs());
        App.getDaoSession().getDataDao().update(data);
        intent.putExtra("id", data.getId());
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            if (XActivityStack.getInstance() == null || XActivityStack.getInstance().findActivity(MainActivity.class) == null) {
                context.startActivities(new Intent[]{intentMain, intent});
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            context.startActivities(new Intent[]{intentMain, intent});
        }
        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        } else if (!TextUtils.isEmpty(message.getUserAccount())) {
            mUserAccount = message.getUserAccount();
        }
    }

    /**
     * 通知消息到达时会到达PushMessageReceiver子类的onNotificationMessageArrived方法
     * 对于应用在前台时不弹出通知的通知消息，SDK会将消息通过广播方式传给AndroidManifest中注册的PushMessageReceiver的子类的
     * {@link #onNotificationMessageArrived(Context, MiPushMessage)}
     *
     * @param context
     * @param message
     */
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        XLog.json(GsonUtil.getInstance().toJson(message));
        mMessage = message.getContent();
        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        } else if (!TextUtils.isEmpty(message.getUserAccount())) {
            mUserAccount = message.getUserAccount();
        }
    }

    /**
     * 用来接收客户端向服务器发送命令消息后返回的响应
     *
     * @param context
     * @param message
     */
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;
            }
        }
    }


    /**
     * 用来接受客户端向服务器发送注册命令消息后返回的响应
     *
     * @param context
     * @param message
     */
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
            }
        }
    }
}