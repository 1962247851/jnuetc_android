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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.User;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.view.activity.DetailsActivity;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.TaskAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.WrapContentLinearLayoutManager;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class NewTaskFragment extends Fragment {

    private static final String TAG = "NewTaskFragment";
    @BindView(R.id.srl_fragment_new_task)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_fragment_new_task)
    RecyclerView recyclerView;

    private boolean firstOpen = true;

    private INewTaskListener iNewTaskListener;
    private MainViewModel mainViewModel;
    private Unbinder unbinder;
    private TaskAdapter taskAdapter;
    /**
     * 配合adapter需要一个容器存放当前的数据，livedata更新时改变容器里的内容
     */
    private List<Data> dataLis1 = new ArrayList<>();
    private int position = -1;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

        mainViewModel.getCurrentState().observe(this, integer -> {
            if (taskAdapter.isSelectMode()) {
                taskAdapter.cancelSelect();
            }
            taskAdapter.setEnableSelect(MainViewModel.user.getRootLevel() != 0 && MainViewModel.user.getRootLevel() != 1);
        });

        mainViewModel.getDrawerOpen().observe(getActivity(), aBoolean -> {
            if (aBoolean && taskAdapter.isSelectMode()) {
                taskAdapter.clearSelect();
                taskAdapter.cancelSelect();
            }
        });

        mainViewModel.getDataList1().observe(getActivity(), data -> {
//            Log.e(TAG, "onChanged1: " + data);
            if (position != -1) {
                taskAdapter.notifyItemRemoved(position);
                taskAdapter.notifyItemRangeChanged(position, taskAdapter.getItemCount() - position);
                position = -1;
            } else {
                taskAdapter.notifyItemRangeRemoved(0, dataLis1.size());
                dataLis1.clear();
                dataLis1.addAll(data);
                taskAdapter.notifyItemRangeInserted(0, data.size());
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(taskAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            }
        });

        mainViewModel.queryDataListBySetting(0);
        mainViewModel.queryDataListBySetting(1);
        mainViewModel.queryDataListBySetting(2);
        autoRefresh();
        swipeRefreshLayout.setOnRefreshListener(this::updateData);
    }

    private void updateData() {
        MainViewModel.queryAll(new HttpUtil.HttpUtilCallBack<List<Data>>() {
            @Override
            public void onResponse(List<Data> result) {
                if (result != null) {
                    XToast.success("数据更新成功");
                    if (firstOpen) {
                        mainViewModel.queryDataListBySetting(0);
                        mainViewModel.loadAllSettings(1);
                        mainViewModel.queryDataListBySetting(1);
                        mainViewModel.loadAllSettings(2);
                        mainViewModel.queryDataListBySetting(2);
                        mainViewModel.loadAllSettings(0);
                        mainViewModel.queryDataListAboutMyself(1);
                        mainViewModel.queryDataListAboutMyself(2);
                        firstOpen = false;
                    } else {
                        mainViewModel.queryDataListBySetting(0);
                    }
                    taskAdapter.clearSelect();
                    taskAdapter.cancelSelect();
                    iNewTaskListener.onCancelSelect();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    XToast.error("数据更新失败");
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(String error) {
                swipeRefreshLayout.setRefreshing(false);
                XToast.error("数据更新失败");
            }
        });
    }

    public void autoRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        updateData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    public NewTaskFragment() {
    }

    public NewTaskFragment(INewTaskListener iNewTaskListener) {
        this.iNewTaskListener = iNewTaskListener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);
        taskAdapter = new TaskAdapter(MainViewModel.user.haveDeleteAccess(), dataLis1, getContext(), new TaskAdapter.ITaskListener() {
            @Override
            public void onItemClick(int position, Data data) {
                Intent intent = new Intent(getContext(), DetailsActivity.class);
                intent.putExtra("id", data.getId());
                intent.putExtra("position", position);
                startActivityForResult(intent, 0);
            }

            @Override
            public void onStartSelect(int count) {
                iNewTaskListener.onStartSelect(count, dataLis1.size());
            }

            @Override
            public void onSelect(int count) {
                iNewTaskListener.onSelect(count, dataLis1.size());
            }

            @Override
            public void onConfirmSelect(SparseBooleanArray sparseBooleanArray) {
                mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                    @Override
                    public void onResponse(User result) {
                        //判断是否具有删单权限
                        if (result.getRootLevel() != 0 && result.getRootLevel() != 1) {

                            List<Integer> ids = new ArrayList<>();
                            List<Data> needToDelete = new ArrayList<>();

                            for (int i = 0; i < sparseBooleanArray.size(); i++) {
                                int key = sparseBooleanArray.keyAt(i);
                                if (sparseBooleanArray.get(key)) {
                                    ids.add(Integer.valueOf(dataLis1.get(key).getId().toString()));
                                    needToDelete.add(App.getDaoSession().getDataDao().queryBuilder().where(DataDao.Properties.Id.eq(dataLis1.get(key).getId())).build().unique());
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
                                    public void onResponse(Boolean result) {
                                        XLoadingDialog.with(getContext()).cancel();
                                        if (result) {
                                            taskAdapter.clearSelect();
                                            taskAdapter.cancelSelect();
                                            iNewTaskListener.onCancelSelect();
                                            mainViewModel.queryDataListBySetting(null);
                                            dialog.dismiss();
                                            XToast.success("删除成功");
                                        }
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        XLoadingDialog.with(getContext()).cancel();
                                        XToast.error("删除失败");
                                        dialog.cancel();
                                    }
                                });
                            });
                            dialog.show();
                        } else {
                            XToast.info("您已不具有删除报修单权限");
                            mainViewModel.loadAllSettings(0);
                            mainViewModel.queryDataListBySetting(0);
                            mainViewModel.loadAllSettings(null);
                            taskAdapter.setEnableSelect(false);
                            taskAdapter.cancelSelect();
                        }
                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });
            }

            @Override
            public void onCancelSelect() {
                iNewTaskListener.onCancelSelect();
            }

            @Override
            public void onConfirmClick(int position1, Data data) {
                XLoadingDialog.with(getContext()).setMessage("请求处理中，请稍后").setCanceled(false).show();
                MainViewModel.queryById(String.valueOf(data.getId()), new HttpUtil.HttpUtilCallBack<Data>() {
                    @Override
                    public void onResponse(Data result) {
                        XLoadingDialog.with(getContext()).cancel();
                        if (result.getState() == 0) {
                            String dataBackUpString = result.toString();
                            String[] items = {"接单成功后自动复制QQ号并且打开QQ"};
                            boolean[] booleans = {true};
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(data.getLocal() + " - " + data.getId())
                                    .setCancelable(false)
                                    .setMultiChoiceItems(items, booleans, (dialogInterface, i, b) -> {
                                    })
                                    .setNegativeButton("取消", (dialogInterface, i) -> XLoadingDialog.with(getContext()).cancel())
                                    .setPositiveButton("接单", (dialogInterface, i) -> {
                                        XLoadingDialog.with(getContext()).setMessage("请求处理中，请稍后").setCanceled(false).show();
                                        result.setOrderDate(System.currentTimeMillis());
                                        result.setRepairer(MainViewModel.user.getUserName());
                                        result.setState((short) 1);
                                        mainViewModel.modify(result, dataBackUpString, new HttpUtil.HttpUtilCallBack<Data>() {
                                            @Override
                                            public void onResponse(Data o) {
                                                XToast.success("接单成功");

                                                App.getDaoSession().getDataDao().update(o);

                                                mainViewModel.queryDataListAboutMyself(1);

                                                position = position1;
                                                dataLis1.remove(position1);
                                                taskAdapter.deleteSelect(position1);
                                                mainViewModel.getDataList1().setValue(dataLis1);

                                                if (booleans[0]) {
                                                    MainViewModel.copyToClipboard(getContext(), result.getQq());
                                                    if (XAppUtils.isInstallApp("com.tencent.mobileqq")) {
                                                        XAppUtils.startApp("com.tencent.mobileqq");
                                                        XToast.success(String.format("QQ：%s已复制到剪切板", data.getQq()));
                                                    } else {
                                                        XToast.error("未安装手Q或安装的版本不支持");
                                                    }
                                                }

                                                XLoadingDialog.with(getContext()).cancel();
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                XLoadingDialog.with(getContext()).cancel();
                                                XToast.error(error == null ? "接单失败" : error);
                                            }
                                        });
                                    })
                                    .create()
                                    .show();

                        } else {
                            XLoadingDialog.with(getContext()).cancel();
                            XToast.warning("唉呀，有人抢先了...\n该报修单已被 " + result.getRepairer() + " 处理");
                            dataLis1.remove(position1);
                            position = position1;
                            mainViewModel.getDataList1().setValue(dataLis1);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        XToast.error(error == null ? "请求失败，请重试" : error);
                        XLoadingDialog.with(getContext()).cancel();
                    }
                });
            }
        });
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == 0) {
                //修改后更新
                if (resultCode == 1) {
                    boolean success = data.getBooleanExtra("modify", false);
                    if (success) {
                        Data data1 = GsonUtil.getInstance().fromJson(data.getStringExtra("data"), Data.class);
                        position = data.getIntExtra("position", -1);
                        dataLis1.remove(position);
                        dataLis1.add(position, data1);
                        mainViewModel.getDataList1().setValue(dataLis1);
                    }
                }
                //接单成功后删除
                if (resultCode == 0) {
                    boolean success = data.getBooleanExtra("order", false);
                    if (success) {
                        Data data1 = GsonUtil.getInstance().fromJson(data.getStringExtra("data"), Data.class);
                        App.getDaoSession().getDataDao().update(data1);
                        mainViewModel.queryDataListAboutMyself(1);
                        position = data.getIntExtra("position", -1);
                        dataLis1.remove(position);
                        taskAdapter.deleteSelect(position);
                        mainViewModel.getDataList1().setValue(dataLis1);
                    }
                }
            }
        }
    }

    public interface INewTaskListener {
        void onStartSelect(int count, int total);

        void onSelect(int count, int total);

        void onCancelSelect();
    }

    public void onConfirmSelect() {
        taskAdapter.finishSelect();
    }

    public boolean isSelectMode() {
        return taskAdapter.isSelectMode();
    }

    public void cancelSelect() {
        taskAdapter.cancelSelect();
    }

    public boolean selectNone() {
        return taskAdapter.selectNone();
    }

}
