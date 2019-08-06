package jn.mjz.aiot.jnuetc.Util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.io.PrintStream;

public class SoftKeyBoardListener {
  private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;
  
  private View rootView;
  
  int rootViewVisibleHeight;
  
  public SoftKeyBoardListener(Activity paramActivity) {
    this.rootView = paramActivity.getWindow().getDecorView();
    this.rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          public void onGlobalLayout() {
            Rect rect = new Rect();
            SoftKeyBoardListener.this.rootView.getWindowVisibleDisplayFrame(rect);
            int i = rect.height();
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("");
            stringBuilder.append(i);
            printStream.println(stringBuilder.toString());
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
          }
        });
  }
  
  public static void setListener(Activity paramActivity, OnSoftKeyBoardChangeListener paramOnSoftKeyBoardChangeListener) { (new SoftKeyBoardListener(paramActivity)).setOnSoftKeyBoardChangeListener(paramOnSoftKeyBoardChangeListener); }
  
  private void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener paramOnSoftKeyBoardChangeListener) { this.onSoftKeyBoardChangeListener = paramOnSoftKeyBoardChangeListener; }
  
  public static interface OnSoftKeyBoardChangeListener {
    void keyBoardHide(int param1Int);
    
    void keyBoardShow(int param1Int);
  }
}
