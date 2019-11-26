package jn.mjz.aiot.jnuetc.view.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.MyApplication;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.User;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.view.activity.DetailsActivity;
import jn.mjz.aiot.jnuetc.view.adapter.RecyclerView.TaskAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.ViewPager.SecondPagerAdapter;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;
import okhttp3.Response;

public class SecondFragment extends Fragment {

    private static final String TAG = "SecondFragment";
    private Unbinder unbinder;
    private int position = -1;

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

                mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                    @Override
                    public void onResponse(Response response, User result) {
                        if (result.getRoot() != 0 && result.getRoot() != 1) {

                            List<Integer> ids = new ArrayList<>();
                            List<Data> needToDelete = new ArrayList<>();

                            for (int i = 0; i < sparseBooleanArray.size(); i++) {
                                int key = sparseBooleanArray.keyAt(i);
                                if (sparseBooleanArray.get(key)) {
                                    String id = dataLists2.get(key).getId().toString();
                                    ids.add(Integer.valueOf(id));
                                    needToDelete.add(MyApplication.getDaoSession().getDataDao().queryBuilder().where(DataDao.Properties.Id.eq(id)).build().unique());
                                }
                            }

                            StringBuilder builder = new StringBuilder();
                            for (Data data : needToDelete) {
                                builder.append(data.getLocal());
                                builder.append(" - ");
                                builder.append(data.getId());
                                builder.append("\n");
                            }

                            AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                            dialog.setTitle("注意");
                            dialog.setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作。确认删除以下报修单？\n" + builder.toString());
                            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "取消", (dialogInterface, i1) -> {

                            });

                            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "删除", (dialogInterface, i1) -> {
                                XLoadingDialog.with(getContext()).setCanceled(false).setMessage("请求处理中,请稍后").show();

                                mainViewModel.deleteMany(ids, new HttpUtil.HttpUtilCallBack<Boolean>() {
                                    @Override
                                    public void onResponse(Response response12, Boolean result12) {

                                        taskAdapter2.clearSelect();
                                        taskAdapter2.cancelSelect();
                                        iSecondListener.OnCancelSelect();

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
                            });

                            dialog.show();

                        } else {
                            XToast.info("您已不是管理员");
                            taskAdapter2.setEnableSelect(false);
                            taskAdapter2.cancelSelect();
                        }
                    }

                    @Override
                    public void onFailure(IOException e) {

                    }
                });


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
                intent.putExtra("position", position);
                startActivityForResult(intent, 1);
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
                mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                    @Override
                    public void onResponse(Response response, User result) {
                        if (result.getRoot() != 0 && result.getRoot() != 1) {

                            List<Integer> ids = new ArrayList<>();
                            List<Data> needToDelete = new ArrayList<>();

                            for (int i = 0; i < sparseBooleanArray.size(); i++) {
                                int key = sparseBooleanArray.keyAt(i);
                                if (sparseBooleanArray.get(key)) {
                                    String id = dataLists3.get(key).getId().toString();
                                    ids.add(Integer.valueOf(id));
                                    needToDelete.add(MyApplication.getDaoSession().getDataDao().queryBuilder().where(DataDao.Properties.Id.eq(id)).build().unique());
                                }
                            }

                            StringBuilder builder = new StringBuilder();
                            for (Data data : needToDelete) {
                                builder.append(data.getLocal());
                                builder.append(" - ");
                                builder.append(data.getId());
                                builder.append("\n");
                            }

                            AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                            dialog.setTitle("注意");
                            dialog.setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作。确认删除以下报修单？\n" + builder.toString());
                            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "取消", (dialogInterface, i1) -> {

                            });

                            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "删除", (dialogInterface, i1) -> {
                                XLoadingDialog.with(getContext()).setCanceled(false).setMessage("请求处理中,请稍后").show();

                                mainViewModel.deleteMany(ids, new HttpUtil.HttpUtilCallBack<Boolean>() {
                                    @Override
                                    public void onResponse(Response response1, Boolean result1) {

                                        taskAdapter3.clearSelect();
                                        taskAdapter3.cancelSelect();
                                        iSecondListener.OnCancelSelect();

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
                            });

                            dialog.show();
                        } else {
                            XToast.info("您已不是管理员");
                            taskAdapter3.setEnableSelect(false);
                            taskAdapter3.cancelSelect();
                        }
                    }

                    @Override
                    public void onFailure(IOException e) {

                    }
                });


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
            taskAdapter2.setEnableSelect(MainViewModel.user.getRoot() != 0 && MainViewModel.user.getRoot() != 1);
            taskAdapter3.setEnableSelect(MainViewModel.user.getRoot() != 0 && MainViewModel.user.getRoot() != 1);
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

        taskAdapter2 = new TaskAdapter(MainViewModel.user.haveDeleteAccess(), dataLists2, getContext(), iTaskListener2);
        taskAdapter3 = new TaskAdapter(MainViewModel.user.haveDeleteAccess(), dataLists3, getContext(), iTaskListener3);

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

        mainViewModel.getDrawerOpen().observe(getActivity(), aBoolean -> {
            if (aBoolean) {
                if (taskAdapter2.isSelectMode()) {
                    taskAdapter2.clearSelect();
                    taskAdapter2.cancelSelect();
                } else {
                    taskAdapter3.clearSelect();
                    taskAdapter3.cancelSelect();
                }
            }
        });

        mainViewModel.getDataList2().observe(getActivity(), data -> {
            if (position != -1) {
                taskAdapter2.notifyItemRemoved(position);
                taskAdapter2.notifyItemRangeChanged(position, taskAdapter2.getItemCount() - position);
                position = -1;
            } else {
                taskAdapter2.notifyItemRangeRemoved(0, taskAdapter2.getItemCount());
                dataLists2.clear();
                dataLists2.addAll(data);
                taskAdapter2.notifyItemRangeInserted(0, data.size());
                pagerAdapter.IsNoData2();
                TabLayout.Tab tabAt = tabLayout.getTabAt(0);
                if (tabAt != null) {
                    tabAt.setText(taskAdapter2.getItemCount() != 0 ? String.format(Locale.getDefault(), "处理中（%d）", data.size()) : "处理中");
                }
            }
        });
        mainViewModel.getDataList3().observe(getActivity(), data -> {
            if (position != -1) {
                taskAdapter3.notifyItemRemoved(position);
                taskAdapter3.notifyItemRangeChanged(position, taskAdapter3.getItemCount() - position);
                position = -1;
            } else {
                taskAdapter3.notifyItemRangeRemoved(0, taskAdapter3.getItemCount());
                dataLists3.clear();
                dataLists3.addAll(data);
                taskAdapter3.notifyItemRangeInserted(0, data.size());
                pagerAdapter.IsNoData3();
                TabLayout.Tab tabAt = tabLayout.getTabAt(1);
                if (tabAt != null) {
                    tabAt.setText(taskAdapter3.getItemCount() != 0 ? String.format(Locale.getDefault(), "已维修（%d）", data.size()) : "已维修");
                }
            }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            if (requestCode == 0) {
                //修改处理中的报修单后更新
                if (resultCode == 1) {
                    boolean success = data.getBooleanExtra("modify", false);
                    if (success) {
                        Data data1 = GsonUtil.getInstance().fromJson(data.getStringExtra("data"), Data.class);
                        position = data.getIntExtra("position", -1);
                        dataLists2.remove(position);
                        dataLists2.add(position, data1);
                        mainViewModel.getDataList2().setValue(dataLists2);
                    }
                }
                //修改已维修的报修单后更新
            } else if (requestCode == 1) {
                if (resultCode == 1) {
                    boolean success = data.getBooleanExtra("modify", false);
                    if (success) {
                        Data data1 = GsonUtil.getInstance().fromJson(data.getStringExtra("data"), Data.class);
                        position = data.getIntExtra("position", -1);
                        dataLists3.remove(position);
                        dataLists3.add(position, data1);
                        mainViewModel.getDataList3().setValue(dataLists3);
                    }
                }
            }
        }
    }
}
