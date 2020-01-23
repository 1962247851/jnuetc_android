package jn.mjz.aiot.jnuetc.view.adapter.pager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

import jn.mjz.aiot.jnuetc.view.adapter.recycler.TaskAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.WrapContentLinearLayoutManager;

/**
 * @author 19622
 */
public class SecondPagerAdapter extends PagerAdapter {

    private static final String TAG = "SecondPagerAdapter";
    private Context context;
    private List<String> titles;
    private TaskAdapter taskAdapter2;
    private TaskAdapter taskAdapter3;
    private RecyclerView recyclerView2;
    private RecyclerView recyclerView3;
    private boolean scrollUp = true;

    public SecondPagerAdapter(Context context, List<String> titles, TaskAdapter taskAdapter2, TaskAdapter taskAdapter3) {
        this.context = context;
        this.titles = titles;
        this.taskAdapter2 = taskAdapter2;
        this.taskAdapter3 = taskAdapter3;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (position == 0) {
            if (container.getChildAt(position) == null) {
                RelativeLayout relativeLayout = new RelativeLayout(context);
                recyclerView2 = new RecyclerView(context);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                recyclerView2.setLayoutParams(layoutParams);
                recyclerView2.setLayoutManager(new WrapContentLinearLayoutManager(context));
                recyclerView2.setAdapter(taskAdapter2);
                relativeLayout.addView(recyclerView2);
                container.addView(relativeLayout);
                return relativeLayout;
            } else {
                return container.getChildAt(position);
            }
        } else {
            if (container.getChildAt(position) == null) {
                RelativeLayout relativeLayout = new RelativeLayout(context);
                recyclerView3 = new RecyclerView(context);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                recyclerView3.setLayoutParams(layoutParams);
                recyclerView3.setLayoutManager(new WrapContentLinearLayoutManager(context));
                recyclerView3.setAdapter(taskAdapter3);
                relativeLayout.addView(recyclerView3);
                container.addView(relativeLayout);
                return relativeLayout;
            } else {
                return container.getChildAt(position);
            }
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    public boolean isSelectMode2() {
        return taskAdapter2.isSelectMode();
    }

    public boolean isSelectMode3() {
        return taskAdapter3.isSelectMode();
    }

    public void cancelSelect2() {
        taskAdapter2.cancelSelect();
    }

    public void cancelSelect3() {
        taskAdapter3.cancelSelect();
    }

    public void insertSelect2() {
        taskAdapter2.insertSelect();
    }

    public void insertSelect3() {
        taskAdapter3.insertSelect();
    }

    public void deleteSelect2(int position) {
        taskAdapter2.deleteSelect(position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        super.destroyItem(container, position, object);
    }

    public void scroll2ToPosition(int position) {
        recyclerView2.scrollToPosition(position);
    }

    public void scroll3ToPosition(int position) {
        recyclerView3.scrollToPosition(position);
    }

    public interface IOnScroll {
        /**
         * 往上滑
         */
        void onScrollUp();

        /**
         * 往下滑
         */
        void onScrollDown();
    }
}
