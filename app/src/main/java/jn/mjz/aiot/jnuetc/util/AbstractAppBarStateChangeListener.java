package jn.mjz.aiot.jnuetc.util;

import com.google.android.material.appbar.AppBarLayout;

/**
 * @author qq1962247851
 * @date 2020/1/18 19:35
 */
public abstract class AbstractAppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

    public enum State {
        /**
         * 展开
         */
        EXPANDED,
        /**
         * 折叠
         */
        COLLAPSED,
        /**
         * 中间
         */
        IDLE
    }

    private State mCurrentState = State.IDLE;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED, i);
            }
            mCurrentState = State.EXPANDED;
        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED, i);
            }
            mCurrentState =
                    State.COLLAPSED;
        } else {
            onStateChanged(appBarLayout, State.IDLE, i);
            mCurrentState = State.IDLE;
        }
    }

    /**
     * 状态变化
     *
     * @param appBarLayout appBarLayout
     * @param state        状态
     * @param i            高度
     */
    public abstract void onStateChanged(AppBarLayout appBarLayout, State state, int i);

}
