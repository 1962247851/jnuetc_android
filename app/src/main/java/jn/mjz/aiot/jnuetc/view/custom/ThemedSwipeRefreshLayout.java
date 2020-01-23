package jn.mjz.aiot.jnuetc.view.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.youth.xframe.XFrame;

import jn.mjz.aiot.jnuetc.R;

/**
 * @author qq1962247851
 * @date 2020/1/21 17:22
 */
public class ThemedSwipeRefreshLayout extends SwipeRefreshLayout {
    public ThemedSwipeRefreshLayout(@NonNull Context context) {
        super(context);
        setColorSchemeColors(XFrame.getColor(R.color.colorPrimary),
                XFrame.getColor(R.color.colorPrimaryDark),
                XFrame.getColor(R.color.colorAccent)
        );
    }

    public ThemedSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setColorSchemeColors(XFrame.getColor(R.color.colorPrimary),
                XFrame.getColor(R.color.colorPrimaryDark),
                XFrame.getColor(R.color.colorAccent)
        );
    }

}
