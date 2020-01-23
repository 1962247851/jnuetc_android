package jn.mjz.aiot.jnuetc.util;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * @author qq1962247851
 * @date 2020/1/21 18:38
 */
public class AnimationUtil {
    private static final String TAG = AnimationUtil.class.getSimpleName();

    /**
     * 从控件所在位置移动到控件的底部
     *
     * @return TranslateAnimation
     */
    public static TranslateAnimation moveToViewBottom() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f
        );
        mHiddenAction.setDuration(500);
        mHiddenAction.setFillAfter(true);
        return mHiddenAction;
    }

    /**
     * 从控件所在位置移动到控件的顶部
     *
     * @return TranslateAnimation
     */
    public static TranslateAnimation moveToViewTop() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f
        );
        mHiddenAction.setDuration(500);
        mHiddenAction.setFillAfter(true);
        return mHiddenAction;
    }

    /**
     * 从控件的底部移动到控件所在位置
     *
     * @return TranslateAnimation
     */
    public static TranslateAnimation moveToViewLocationFromBottom() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f
        );
        mHiddenAction.setDuration(500);
        mHiddenAction.setFillAfter(true);
        return mHiddenAction;
    }

    /**
     * 从控件的顶部移动到控件所在位置
     *
     * @return TranslateAnimation
     */
    public static TranslateAnimation moveToViewLocationFromTop() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f
        );
        mHiddenAction.setDuration(500);
        mHiddenAction.setFillAfter(true);
        return mHiddenAction;
    }

}
