package jn.mjz.aiot.jnuetc.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.XDateUtils;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.RankingInfo;
import jn.mjz.aiot.jnuetc.util.DateUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.view.adapter.pager.RankingPagerAdapter;
import jn.mjz.aiot.jnuetc.view.custom.ScrollViewPager;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class RankingActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.tabLayout_ranking)
    TabLayout tabLayout;
    @BindView(R.id.toolbar_ranking)
    Toolbar toolbar;
    @BindView(R.id.viewPager_ranking)
    ScrollViewPager viewPagerRanking;
    @BindView(R.id.srl_ranking)
    SwipeRefreshLayout swipeRefreshLayout;

    private RankingPagerAdapter rankingPagerAdapter;
    private List<RankingInfo> rankingInfos1 = new ArrayList<>();
    private List<RankingInfo> rankingInfos2 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        ButterKnife.bind(this);
        String title;
        title = MainViewModel.getUser().getGroupStringIfNotAll();
        toolbar.setTitle(toolbar.getTitle() + "（" + title + "）");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        XStatusBar.setColorNoTranslucent(this, XFrame.getColor(R.color.colorPrimary));
        rankingPagerAdapter = new RankingPagerAdapter(rankingInfos1, rankingInfos2, this);
        viewPagerRanking.setScroll(false);
        viewPagerRanking.setAdapter(rankingPagerAdapter);
        tabLayout.setupWithViewPager(viewPagerRanking);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        updateRanking(true);
        App.initToolbar(toolbar, this);
    }

    /**
     * 滚动到自己的位置
     *
     * @param which 哪个排行榜
     * @deprecated 已被绿色边界线替代
     */
    private void scrollToSelf(int which) {
        if (which == 1) {
            for (int i = 0; i < rankingInfos1.size(); i++) {
                if (rankingInfos1.get(i).getUserName().equals(MainViewModel.user.getUserName())) {
                    rankingPagerAdapter.getRecyclerView1().smoothScrollToPosition(i);
                    return;
                }
            }
        } else {
            for (int i = 0; i < rankingInfos2.size(); i++) {
                if (rankingInfos2.get(i).getUserName().equals(MainViewModel.user.getUserName())) {
                    rankingPagerAdapter.getRecyclerView2().smoothScrollToPosition(i);
                    return;
                }
            }
        }
        XToast.info("暂未上榜，继续努力哦！");
    }

    private void updateRanking(boolean updateAll) {
        MainViewModel.queryAllUser(new HttpUtil.HttpUtilCallBack<List<String>>() {
            @Override
            public void onResponse(List<String> result) {
                if (updateAll) {
                    getRankingInfo1(result);
                    getRankingInfo2(result);
                } else {
                    if (viewPagerRanking.getCurrentItem() == 0) {
                        getRankingInfo1(result);
                    } else {
                        getRankingInfo2(result);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                XToast.error(error);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_ranking, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_ranking_refresh) {
            MainViewModel.queryAll(new HttpUtil.HttpUtilCallBack<List<Data>>() {
                @Override
                public void onResponse(List<Data> result) {
                    updateRanking(false);
                }

                @Override
                public void onFailure(String error) {
                    XToast.error(error);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    /**
     * 更新周排行榜
     *
     * @param result userNameList
     */
    private void getRankingInfo1(List<String> result) {
        List<String> dateToWeek = DateUtil.getDateToWeek(new Date(System.currentTimeMillis()));
        String startDate = dateToWeek.get(0);
        String endDate = dateToWeek.get(6);
        tabLayout.getTabAt(0).setText("周排行榜\n" + startDate + "-" + endDate);
        long startTimeMills = XDateUtils.date2Millis(XDateUtils.string2Date(startDate + " 00:00:00", "yyyy/MM/dd HH:mm:ss"));
        long endTimeMills = XDateUtils.date2Millis(XDateUtils.string2Date(endDate + " 23:59:59", "yyyy/MM/dd HH:mm:ss"));
        int count = rankingInfos1.size();
        rankingInfos1.clear();
        rankingPagerAdapter.getRankingAdapter1().notifyItemRangeRemoved(0, count);
        if (result != null && !result.isEmpty()) {
            for (String userName : result) {
                List<Data> list = App.getDaoSession().getDataDao().queryBuilder().where(
                        DataDao.Properties.State.eq(2),
                        DataDao.Properties.RepairDate.ge(startTimeMills),
                        DataDao.Properties.RepairDate.le(endTimeMills),
                        DataDao.Properties.Repairer.like("%" + userName + "%")
                ).build().list();
                if (!list.isEmpty()) {
                    rankingInfos1.add(new RankingInfo(userName, list));
                    rankingPagerAdapter.getRankingAdapter1().notifyItemInserted(rankingInfos1.size());
                }
            }
            Collections.sort(rankingInfos1, (o1, o2) -> o2.getCount().compareTo(o1.getCount()));
        }
        XToast.success("周排行榜更新成功");
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 更新总排行榜
     *
     * @param result userNameList
     */
    private void getRankingInfo2(List<String> result) {
        int count = rankingInfos2.size();
        rankingInfos2.clear();
        rankingPagerAdapter.getRankingAdapter2().notifyItemRangeRemoved(0, count);
        if (result != null && !result.isEmpty()) {
            for (String userName : result) {
                List<Data> list = App.getDaoSession().getDataDao().queryBuilder().where(
                        DataDao.Properties.State.eq(2),
                        DataDao.Properties.Repairer.like("%" + userName + "%")
                ).build().list();
                if (!list.isEmpty()) {
                    rankingInfos2.add(new RankingInfo(userName, list));
                    rankingPagerAdapter.getRankingAdapter2().notifyItemInserted(rankingInfos2.size());
                }
            }
            Collections.sort(rankingInfos2, (o1, o2) -> o2.getCount().compareTo(o1.getCount()));
        }
        XToast.success("总排行榜更新成功");
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        updateRanking(false);
    }
}
