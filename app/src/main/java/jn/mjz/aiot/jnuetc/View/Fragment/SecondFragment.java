package jn.mjz.aiot.jnuetc.View.Fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jn.mjz.aiot.jnuetc.Greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.View.Activity.DetailsActivity;
import jn.mjz.aiot.jnuetc.View.Adapter.RecyclerView.TaskAdapter;
import jn.mjz.aiot.jnuetc.View.Adapter.ViewPager.SecondPagerAdapter;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import okhttp3.Response;

public class SecondFragment extends Fragment {

    private static final String TAG = "SecondFragment";
    private Unbinder unbinder;

    @BindView(R.id.tabLayout_fragment_second)
    TabLayout tabLayout;

    @BindView(R.id.viewPager_fragment_second)
    ViewPager viewPager;

    @BindView(R.id.smartRefreshLayout_fragment_second)
    SmartRefreshLayout smartRefreshLayout;

    private MainViewModel mainViewModel;
    private static SecondPagerAdapter pagerAdapter;
    private ISecondListener iSecondListener;

    private static TaskAdapter taskAdapter2;
    private static TaskAdapter taskAdapter3;
    private static List<Data> dataLists2 = new ArrayList<>();
    private static List<Data> dataLists3 = new ArrayList<>();

    public SecondFragment(ISecondListener iSecondListener) {
        this.iSecondListener = iSecondListener;
    }

    public SecondFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(MainViewModel.class);

        List<String> titles = new ArrayList<>();
        titles.add("处理中");
        titles.add("已维修");


        TaskAdapter.ITaskListener iTaskListener2 = new TaskAdapter.ITaskListener() {
            @Override
            public void OnItemClick(int position, Data data) {
                Intent intent = new Intent(getContext(), DetailsActivity.class);
                intent.putExtra("data", data.toString());
                intent.putExtra("position", position);
                startActivityForResult(intent, 0);
            }

            @Override
            public void OnStartSelect(int count) {
                iSecondListener.OnStartSelect(count, dataLists2.size());
            }

            @Override
            public void OnSelect(int count) {
                iSecondListener.OnSelect(count, dataLists2.size());
            }

            @Override
            public void OnConfirmSelect(SparseBooleanArray sparseBooleanArray) {

                AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.setTitle("注意");
                dialog.setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作");
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i1) {

                    }
                });

                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i1) {
                        XLoadingDialog.with(getContext()).setCanceled(false).setMessage("请求处理中,请稍后").show();
                        List<Integer> ids = new ArrayList<>();

                        for (int i = 0; i < sparseBooleanArray.size(); i++) {
                            int key = sparseBooleanArray.keyAt(i);
                            if (sparseBooleanArray.get(key)) {
                                ids.add(Integer.valueOf(dataLists2.get(key).getId().toString()));
                            }
                        }

                        mainViewModel.deleteMany(ids, new HttpUtil.HttpUtilCallBack<Boolean>() {
                            @Override
                            public void onResponse(Response response, Boolean result) {

                                taskAdapter2.clearSelect();
                                taskAdapter2.cancelSelect();
                                iSecondListener.OnCancelSelect();

                                List<Data> needToDelete = mainViewModel.dataDao.queryBuilder()
                                        .where(DataDao.Properties.Id.in(ids))
                                        .build()
                                        .list();
                                mainViewModel.dataDao.deleteInTx(needToDelete);

                                mainViewModel.queryDataListBySetting(null);

                                dialog.dismiss();
                                XToast.success("删除成功");
                                XLoadingDialog.with(getContext()).cancel();

                            }

                            @Override
                            public void onFailure(IOException e) {
                                XLoadingDialog.with(getContext()).cancel();
                                XToast.error("删除失败");
                                dialog.cancel();
                            }
                        });
                    }
                });

                dialog.show();


            }

            @Override
            public void OnCancelSelect() {
                iSecondListener.OnCancelSelect();
//                Log.e(TAG, "OnCancelSelect: ");
            }


            @Override
            public void OnConfirmClick(int position, Data data) {
            }
        };
        TaskAdapter.ITaskListener iTaskListener3 = new TaskAdapter.ITaskListener() {
            @Override
            public void OnItemClick(int position, Data data) {
                Intent intent = new Intent(getContext(), DetailsActivity.class);
                intent.putExtra("data", data.toString());
                startActivity(intent);
            }

            @Override
            public void OnStartSelect(int count) {
                iSecondListener.OnStartSelect(count, dataLists3.size());
//                Log.e(TAG, "OnStartSelect: " + count);
            }

            @Override
            public void OnSelect(int count) {
                iSecondListener.OnSelect(count, dataLists3.size());
//                Log.e(TAG, "OnSelect: " + count);
            }

            @Override
            public void OnConfirmSelect(SparseBooleanArray sparseBooleanArray) {
                AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.setTitle("注意");
                dialog.setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作");
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i1) {

                    }
                });

                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i1) {
                        XLoadingDialog.with(getContext()).setCanceled(false).setMessage("请求处理中,请稍后").show();
                        List<Integer> ids = new ArrayList<>();

                        for (int i = 0; i < sparseBooleanArray.size(); i++) {
                            int key = sparseBooleanArray.keyAt(i);
                            if (sparseBooleanArray.get(key)) {
                                ids.add(Integer.valueOf(dataLists3.get(key).getId().toString()));
                            }
                        }

                        mainViewModel.deleteMany(ids, new HttpUtil.HttpUtilCallBack<Boolean>() {
                            @Override
                            public void onResponse(Response response, Boolean result) {

                                // TODO: 2019/8/4 通知adapter更新数据 (dataList和booleanArray)

                                taskAdapter3.clearSelect();
                                taskAdapter3.cancelSelect();
                                iSecondListener.OnCancelSelect();

                                List<Data> needToDelete = mainViewModel.dataDao.queryBuilder()
                                        .where(DataDao.Properties.Id.in(ids))
                                        .build()
                                        .list();
                                mainViewModel.dataDao.deleteInTx(needToDelete);

                                mainViewModel.queryDataListBySetting(null);

                                dialog.dismiss();
                                XToast.success("删除成功");
                                XLoadingDialog.with(getContext()).cancel();

                            }

                            @Override
                            public void onFailure(IOException e) {
                                XLoadingDialog.with(getContext()).cancel();
                                XToast.error("删除失败");
                                dialog.cancel();
                            }
                        });
                    }
                });

                dialog.show();
            }

            @Override
            public void OnCancelSelect() {
                iSecondListener.OnCancelSelect();
//                Log.e(TAG, "OnCancelSelect: ");
            }

            @Override
            public void OnConfirmClick(int position, Data data) {
            }
        };

        mainViewModel.getCurrentState().observe(this, integer -> {
            if (integer == 1) {
                if (pagerAdapter.isSelectMode3()) {
                    pagerAdapter.cancelSelect3();
                }
            } else if (integer == 2) {
                if (pagerAdapter.isSelectMode2()) {
                    pagerAdapter.cancelSelect2();
                }
            } else {
                if (pagerAdapter.isSelectMode3()) {
                    pagerAdapter.cancelSelect3();
                } else if (pagerAdapter.isSelectMode2()) {
                    pagerAdapter.cancelSelect2();
                }
            }
        });

        taskAdapter2 = new TaskAdapter(dataLists2, getContext(), iTaskListener2);
        taskAdapter3 = new TaskAdapter(dataLists3, getContext(), iTaskListener3);

        pagerAdapter = new SecondPagerAdapter(getContext(), titles, taskAdapter2, taskAdapter3);
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mainViewModel.getCurrentState().setValue(position + 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tabLayout.setupWithViewPager(viewPager);

        smartRefreshLayout.finishLoadMoreWithNoMoreData();
        smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            updateData();
        });

        mainViewModel.getDataList2().observe(getActivity(), data -> {
//            Log.e(TAG, "onChanged2: " + data);
            taskAdapter2.notifyItemRangeRemoved(0, taskAdapter2.getItemCount());
            dataLists2.clear();
            dataLists2.addAll(data);
            taskAdapter2.notifyItemRangeInserted(0, data.size());
            pagerAdapter.IsNoData2();
        });
        mainViewModel.getDataList3().observe(getActivity(), data -> {
//            Log.e(TAG, "onChanged3: " + data);
            taskAdapter3.notifyItemRangeRemoved(0, taskAdapter3.getItemCount());
            dataLists3.clear();
            dataLists3.addAll(data);
            taskAdapter3.notifyItemRangeInserted(0, data.size());
            pagerAdapter.IsNoData3();
        });
    }

    private void updateData() {
        mainViewModel.queryAll(new HttpUtil.HttpUtilCallBack<List<Data>>() {
            @Override
            public void onResponse(Response response, List<Data> result) {

                if (result != null) {
                    XToast.success("数据更新成功");
                    smartRefreshLayout.finishRefreshWithNoMoreData();
                    mainViewModel.queryDataListBySetting(null);
                    if (getCurrentPosition() == 0) {
                        taskAdapter2.clearSelect();
                        taskAdapter2.cancelSelect();
                        iSecondListener.OnCancelSelect();
                    } else {
                        taskAdapter3.clearSelect();
                        taskAdapter3.cancelSelect();
                        iSecondListener.OnCancelSelect();
                    }
                } else {
                    XToast.error("数据更新失败");
                    smartRefreshLayout.finishRefresh(0, false, true);
                }
            }

            @Override
            public void onFailure(IOException e) {
                smartRefreshLayout.finishRefresh(0, false, true);
                XToast.error("数据更新失败");
            }
        });
    }

    public int getCurrentPosition() {
        return viewPager.getCurrentItem();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    public static void notifyDataList2Inserted(Data data) {
        dataLists2.add(0, data);
        pagerAdapter.insertSelect2();
        taskAdapter2.notifyItemInserted(0);
        pagerAdapter.scroll2ToPosition(0);
        pagerAdapter.IsNoData2();
    }

    public static void notifyDataList3Inserted(Data data) {
        dataLists3.add(0, data);
        pagerAdapter.insertSelect3();
        taskAdapter3.notifyItemInserted(0);
        pagerAdapter.scroll3ToPosition(0);
        pagerAdapter.IsNoData3();
    }

    public void autoRefresh() {
        smartRefreshLayout.autoRefresh(0, 200, (float) 1.2, false);
    }

    public interface ISecondListener {
        void OnStartSelect(int count, int total);

        void OnSelect(int count, int total);

        void OnCancelSelect();
    }

    public void OnConfirmSelect() {
        if (getCurrentPosition() == 0) {
            taskAdapter2.finishSelect();
        } else {
            taskAdapter3.finishSelect();
        }
    }

    public boolean selectNone() {
        if (getCurrentPosition() == 0) {
            return taskAdapter2.selectNone();
        } else {
            return taskAdapter3.selectNone();
        }
    }

    public boolean isSelectMode() {
        if (getCurrentPosition() == 0) {
            return pagerAdapter.isSelectMode2();
        } else {
            return pagerAdapter.isSelectMode3();
        }
    }

    public void cancelSelect() {
        if (getCurrentPosition() == 0) {
            pagerAdapter.cancelSelect2();
        } else {
            pagerAdapter.cancelSelect3();
        }
    }

}
