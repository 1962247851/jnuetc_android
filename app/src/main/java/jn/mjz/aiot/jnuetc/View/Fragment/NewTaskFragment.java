package jn.mjz.aiot.jnuetc.View.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jn.mjz.aiot.jnuetc.Application.MyApplication;
import jn.mjz.aiot.jnuetc.Greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.View.Activity.DetailsActivity;
import jn.mjz.aiot.jnuetc.View.Adapter.RecyclerView.TaskAdapter;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import okhttp3.Response;

public class NewTaskFragment extends Fragment {

    private static final String TAG = "NewTaskFragment";
    @BindView(R.id.srl_fragment_new_task)
    SmartRefreshLayout smartRefreshLayout;
    @BindView(R.id.rv_fragment_new_task)
    RecyclerView recyclerView;

    private INewTaskListener iNewTaskListener;
    private MainViewModel mainViewModel;
    private Unbinder unbinder;
    private TaskAdapter taskAdapter;
    //配合adapter需要一个容器存放当前的数据，livedata更新时改变容器里的内容
    private List<Data> dataLis1 = new ArrayList<>();
    private int position = -1;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

        taskAdapter = new TaskAdapter(dataLis1, getContext(), new TaskAdapter.ITaskListener() {
            @Override
            public void OnItemClick(int position, Data data) {
                Intent intent = new Intent(getContext(), DetailsActivity.class);
                intent.putExtra("data", data.toString());
                intent.putExtra("position", position);
                startActivityForResult(intent, 0);
//                Log.d(TAG, "OnItemClick: " + data);
            }

            @Override
            public void OnStartSelect(int count) {
                iNewTaskListener.OnStartSelect(count, dataLis1.size());
//                Log.e(TAG, "OnStartSelect: " + count);
            }

            @Override
            public void OnSelect(int count) {
                iNewTaskListener.OnSelect(count, dataLis1.size());
//                Log.e(TAG, "OnSelect: " + count);
            }

            @Override
            public void OnConfirmSelect(SparseBooleanArray sparseBooleanArray) {
                AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.setTitle("注意");
                dialog.setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作");
                dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i1) {

                    }
                });

                dialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i1) {
                        XLoadingDialog.with(getContext()).setCanceled(false).setMessage("请求处理中,请稍后").show();
                        List<Integer> ids = new ArrayList<>();

                        for (int i = 0; i < sparseBooleanArray.size(); i++) {
                            int key = sparseBooleanArray.keyAt(i);
                            if (sparseBooleanArray.get(key)) {
                                ids.add(Integer.valueOf(dataLis1.get(key).getId().toString()));
                            }
                        }

                        mainViewModel.deleteMany(ids, new HttpUtil.HttpUtilCallBack<Boolean>() {
                            @Override
                            public void onResponse(Response response, Boolean result) {

                                // TODO: 2019/8/4 通知adapter更新数据 (dataList和booleanArray)

                                taskAdapter.clearSelect();
                                taskAdapter.cancelSelect();
                                iNewTaskListener.OnCancelSelect();

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
                iNewTaskListener.OnCancelSelect();
//                Log.e(TAG, "OnCancelSelect: ");
            }

            @Override
            public void OnConfirmClick(int position1, Data data) {
                XLoadingDialog.with(getContext()).setMessage("请求处理中，请稍后").setCanceled(false).show();
                mainViewModel.queryById(String.valueOf(data.getId()), new HttpUtil.HttpUtilCallBack<Data>() {
                    @Override
                    public void onResponse(Response response, Data result) {
                        if (result.getState() == 0) {
                            String[] items = {"接单成功后自动打开QQ会话"};
                            boolean[] booleans = {true};
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            // TODO: 2019/7/10 设置customTitle
                            builder.setTitle(data.getLocal() + " - " + data.getId())
                                    .setCancelable(false)
                                    .setMultiChoiceItems(items, booleans, (dialogInterface, i, b) -> {
                                    })
                                    .setNegativeButton("取消", (dialogInterface, i) -> XLoadingDialog.with(getContext()).cancel())
                                    .setPositiveButton("确定", (dialogInterface, i) -> {
                                        XLoadingDialog.with(getContext()).cancel();
                                        // TODO: 2019/7/10
                                        result.setRepairer(GlobalUtil.user.getName());
                                        result.setState((short) 1);
                                        result.setRepairDate(System.currentTimeMillis());
                                        mainViewModel.feedback(result, new HttpUtil.HttpUtilCallBack<Data>() {
                                            @Override
                                            public void onResponse(Response response1, Data o) {
                                                XToast.success("接单成功");

                                                updateMyselfDataList(o);

                                                SecondFragment.notifyDataList2Inserted(result);

                                                MyApplication.getDaoSession().getDataDao().update(result);

                                                position = position1;
                                                dataLis1.remove(position1);
                                                taskAdapter.deleteSelect(position1);
                                                mainViewModel.getDataList1().setValue(dataLis1);

                                                if (booleans[0]) {
                                                    String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + result.getQq();
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                        intent.setAction(Intent.ACTION_VIEW);
                                                    try {
                                                        startActivity(intent);
                                                    } catch (Exception e) {
                                                        XToast.error("未安装手Q或安装的版本不支持");
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onFailure(IOException e) {
                                                XToast.error("接单失败");
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
                    public void onFailure(IOException e) {
                        Log.e(TAG, "onFailure: " + e);
                        XToast.error("请求失败，请重试");
                        XLoadingDialog.with(getContext()).cancel();
                    }
                });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);

        mainViewModel.getCurrentState().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (taskAdapter.isSelectMode()) {
                    taskAdapter.cancelSelect();
                }
            }
        });

        mainViewModel.getDataList1().observe(getActivity(), data -> {
            Log.e(TAG, "onChanged1: " + data);
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
//        mainViewModel.queryDataListBySetting();

        smartRefreshLayout.finishLoadMoreWithNoMoreData();
        autoRefresh();
        smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            XLoadingDialog.with(getContext()).setMessage("获取最新数据中，请稍后").setCanceled(false).show();
            mainViewModel.queryAll(new HttpUtil.HttpUtilCallBack<List<Data>>() {
                @Override
                public void onResponse(Response response, List<Data> result) {
                    XLoadingDialog.with(getContext()).cancel();
                    if (result != null) {
                        XToast.success("数据更新成功");
                        smartRefreshLayout.finishRefreshWithNoMoreData();
                        mainViewModel.queryDataListBySetting(0);
                        taskAdapter.clearSelect();
                        taskAdapter.cancelSelect();
                        iNewTaskListener.OnCancelSelect();
                    } else {
                        XToast.error("数据更新失败");
                        smartRefreshLayout.finishRefresh(0, false, true);
                    }
                }

                @Override
                public void onFailure(IOException e) {
                    XLoadingDialog.with(getContext()).cancel();
                    smartRefreshLayout.finishRefresh(0, false, true);
                    XToast.error("数据更新失败");
                }
            });
        });
    }

    public void autoRefresh() {
        smartRefreshLayout.autoRefresh(0, 200, (float) 1.2, false);
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && requestCode == 0) {
            boolean success = data.getBooleanExtra("order", false);
            if (success) {
                Data data1 = GsonUtil.getInstance().fromJson(data.getStringExtra("data"), Data.class);
                updateMyselfDataList(data1);
                position = data.getIntExtra("position", -1);
                dataLis1.remove(position);
                taskAdapter.deleteSelect(position);
                mainViewModel.getDataList1().setValue(dataLis1);
            }
        }
    }

    public interface INewTaskListener {
        void OnStartSelect(int count, int total);

        void OnSelect(int count, int total);

        void OnCancelSelect();
    }

    public void OnConfirmSelect() {
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

    //单独更新处理中和已维修
    private void updateMyselfDataList(Data data) {
        List<Data> dataList4 = mainViewModel.getDataList4().getValue();
        dataList4.add(0, data);
        List<Data> dataList = new ArrayList<>(dataList4);
         mainViewModel.getDataList4().setValue(dataList);
    }
}
