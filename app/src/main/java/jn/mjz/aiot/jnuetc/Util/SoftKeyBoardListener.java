package jn.mjz.aiot.jnuetc.Util;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SoftKeyBoardListener {
    private static final String TAG = "SoftKeyBoardListener";
    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;

    private View rootView;

    private int rootViewVisibleHeight;

    private SoftKeyBoardListener(AppCompatActivity paramActivity) {
        this.rootView = paramActivity.getWindow().getDecorView();
        this.rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            SoftKeyBoardListener.this.rootView.getWindowVisibleDisplayFrame(rect);
            int i = rect.height();
            Log.e(TAG, "onGlobalLayout: " + i);
            if (SoftKeyBoardListener.this.rootViewVisibleHeight == 0) {
                SoftKeyBoardListener.this.rootViewVisibleHeight = i;
                return;
            }
            if (SoftKeyBoardListener.this.rootViewVisibleHeight == i)
                return;
            if (SoftKeyBoardListener.this.rootViewVisibleHeight - i > 200) {
                if (SoftKeyBoardListener.this.onSoftKeyBoardChangeListener != null)
                    SoftKeyBoardListener.this.onSoftKeyBoardChangeListener.keyBoardShow(SoftKeyBoardListener.this.rootViewVisibleHeight - i);
                SoftKeyBoardListener.this.rootViewVisibleHeight = i;
                return;
            }
            if (i - SoftKeyBoardListener.this.rootViewVisibleHeight > 200) {
                if (SoftKeyBoardListener.this.onSoftKeyBoardChangeListener != null)
                    SoftKeyBoardListener.this.onSoftKeyBoardChangeListener.keyBoardHide(i - SoftKeyBoardListener.this.rootViewVisibleHeight);
                SoftKeyBoardListener.this.rootViewVisibleHeight = i;
                return;
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
        void keyBoardHide(int height);

        void keyBoardShow(int height);
    }
}
