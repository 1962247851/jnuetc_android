package jn.mjz.aiot.jnuetc.view.adapter.ViewPager;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

import jn.mjz.aiot.jnuetc.view.adapter.RecyclerView.TaskAdapter;
import jn.mjz.aiot.jnuetc.R;

public class SecondPagerAdapter extends PagerAdapter {
    private static final String TAG = "SecondPagerAdapter";
    private Context context;
    private List<String> titles;
    private TaskAdapter taskAdapter2;
    private TaskAdapter taskAdapter3;
    private RecyclerView recyclerView2;
    private RecyclerView recyclerView3;

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

    //设置viewpage内部东西的方法，如果viewpage内没有子空间滑动产生不了动画效果
    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (position == 0) {
            if (container.getChildAt(position) == null) {
                RelativeLayout relativeLayout = new RelativeLayout(context);
                TextView textViewNoData = new TextView(context);
                textViewNoData.setText(R.string.NoData);
                textViewNoData.setTextSize(20);
                textViewNoData.setGravity(Gravity.CENTER);

                recyclerView2 = new RecyclerView(context);
                recyclerView2.setBackground(context.getDrawable(R.color.white));
                recyclerView2.setLayoutManager(new LinearLayoutManager(context));
                recyclerView2.setAdapter(taskAdapter2);
                relativeLayout.addView(textViewNoData);
                relativeLayout.addView(recyclerView2);

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) textViewNoData.getLayoutParams();
                layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                textViewNoData.setLayoutParams(layoutParams);
                recyclerView2.setLayoutParams(layoutParams);

                container.addView(relativeLayout);
                return relativeLayout;
            } else {
                return container.getChildAt(position);
            }
        } else {
            if (container.getChildAt(position) == null) {
                RelativeLayout relativeLayout = new RelativeLayout(context);
                TextView textViewNoData = new TextView(context);
                textViewNoData.setText(R.string.NoData);
                textViewNoData.setTextSize(20);
                textViewNoData.setGravity(Gravity.CENTER);

                recyclerView3 = new RecyclerView(context);
                recyclerView3.setBackground(context.getDrawable(R.color.white));
                recyclerView3.setLayoutManager(new LinearLayoutManager(context));
                recyclerView3.setAdapter(taskAdapter3);
                relativeLayout.addView(textViewNoData);
                relativeLayout.addView(recyclerView3);

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) textViewNoData.getLayoutParams();
                layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                textViewNoData.setLayoutParams(layoutParams);
                recyclerView3.setLayoutParams(layoutParams);

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

    public void IsNoData2() {
        if (recyclerView2 != null) {
            recyclerView2.setVisibility(taskAdapter2.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        }
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


    public void IsNoData3() {
        if (recyclerView3 != null) {
            recyclerView3.setVisibility(taskAdapter3.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        }
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
}
