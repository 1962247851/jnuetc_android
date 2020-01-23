package jn.mjz.aiot.jnuetc.util;

import android.graphics.Rect;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author 19622
 */
public class SoftKeyBoardListener {
    private static final String TAG = "SoftKeyBoardListener";
    private static final int HEIGHT = 200;
    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;
    private View rootView;
    private int rootViewVisibleHeight;

    private SoftKeyBoardListener(AppCompatActivity paramActivity) {
        this.rootView = paramActivity.getWindow().getDecorView();
        this.rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            SoftKeyBoardListener.this.rootView.getWindowVisibleDisplayFrame(rect);
            int i = rect.height();
            if (SoftKeyBoardListener.this.rootViewVisibleHeight == 0) {
                SoftKeyBoardListener.this.rootViewVisibleHeight = i;
                return;
            }
            if (SoftKeyBoardListener.this.rootViewVisibleHeight == i) {
                return;
            }
            if (SoftKeyBoardListener.this.rootViewVisibleHeight - i > HEIGHT) {
                if (SoftKeyBoardListener.this.onSoftKeyBoardChangeListener != null) {
                    SoftKeyBoardListener.this.onSoftKeyBoardChangeListener.keyBoardShow(SoftKeyBoardListener.this.rootViewVisibleHeight - i);
                }
                SoftKeyBoardListener.this.rootViewVisibleHeight = i;
                return;
            }
            if (i - SoftKeyBoardListener.this.rootViewVisibleHeight > HEIGHT) {
                if (SoftKeyBoardListener.this.onSoftKeyBoardChangeListener != null) {
                    SoftKeyBoardListener.this.onSoftKeyBoardChangeListener.keyBoardHide(i - SoftKeyBoardListener.this.rootViewVisibleHeight);
                }
                SoftKeyBoardListener.this.rootViewVisibleHeight = i;
            }
        });
    }

    public static void setListener(AppCompatActivity paramActivity, OnSoftKeyBoardChangeListener paramOnSoftKeyBoardChangeListener) {
        (new SoftKeyBoardListener(paramActivity)).setOnSoftKeyBoardChangeListener(paramOnSoftKeyBoardChangeListener);
    }

    private void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener paramOnSoftKeyBoardChangeListener) {
        this.onSoftKeyBoardChangeListener = paramOnSoftKeyBoardChangeListener;
    }

    public interface OnSoftKeyBoardChangeListener {
        /**
         * 软键盘隐藏
         *
         * @param height 高度
         */
        void keyBoardHide(int height);

        /**
         * 软键盘显示
         *
         * @param height 高度
         */
        void keyBoardShow(int height);
    }
}
