package jn.mjz.aiot.jnuetc.View.CustomView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class ScrollViewPager extends ViewPager {
    private boolean isScroll = true;

    public ScrollViewPager(@NonNull Context paramContext) {
        super(paramContext);
    }

    public ScrollViewPager(@NonNull Context paramContext, @Nullable AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScroll){
            return super.onInterceptTouchEvent(ev);
        }else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isScroll){
            return super.onTouchEvent(ev);
        }else {
            return false;
        }

    }

    public void setScroll(boolean isScroll) {
        this.isScroll = isScroll;
    }
}
