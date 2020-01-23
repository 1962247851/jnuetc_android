package jn.mjz.aiot.jnuetc.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.User;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.TaskAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.WrapContentLinearLayoutManager;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HistoryActivity";
    private List<Data> dataList = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private List<Data> needDelete = new ArrayList<>();
    private MainViewModel mainViewModel = MainActivity.mainViewModel;
    private int state;
    private String title;
    private boolean titleWithCount;

    @BindView(R.id.recyclerView_history)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar_history)
    Toolbar toolbar;
    @BindView(R.id.fab_history_delete)
    FloatingActionButton floatingActionButtonDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        state = getIntent().getIntExtra("state", 0);
        String dataListJsonString = getIntent().getStringExtra("dataList");
        dataList.addAll(GsonUtil.parseJsonArray2ObjectList(dataListJsonString, Data.class));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        XStatusBar.setColorNoTranslucent(this, XFrame.getColor(R.color.colorPrimary));
        title = getIntent().getStringExtra("title");
        titleWithCount = getIntent().getBooleanExtra("titleWithCount", true);
        updateTitle(state, dataList.size());

        taskAdapter = new TaskAdapter(MainViewModel.user.haveDeleteAccess(), dataList, this, new TaskAdapter.ITaskListener() {
            @Override
            public void onItemClick(int position, Data data) {
                Intent intent = new Intent(HistoryActivity.this, DetailsActivity.class);
                intent.putExtra("id", data.getId());
                intent.putExtra("position", position);
                startActivityForResult(intent, MainActivity.REQUEST_DATA_CHANGE);
            }

            @Override
            public void onStartSelect(int count) {
                floatingActionButtonDelete.show();
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, dataList.size()));
            }

            @Override
            public void onSelect(int count) {
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, dataList.size()));
            }

            @Override
            public void onConfirmSelect(SparseBooleanArray sparseBooleanArray) {
                deleteSelect(sparseBooleanArray);
            }

            @Override
            public void onCancelSelect() {
                floatingActionButtonDelete.hide();
                updateTitle(state, dataList.size());
            }


            @Override
            public void onConfirmClick(int position, Data data) {
                XLoadingDialog.with(HistoryActivity.this).setMessage("请求处理中，请稍后").setCanceled(false).show();
                MainViewModel.queryById(String.valueOf(data.getId()), new HttpUtil.HttpUtilCallBack<Data>() {
                    @Override
                    public void onResponse(Data result) {
                        XLoadingDialog.with(HistoryActivity.this).cancel();
                        if (result.getState() == 0) {
                            String dataBackUpString = result.toString();
                            String[] items = {"接单成功后自动复制QQ号并且打开QQ"};
                            boolean[] booleans = {true};
                            AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                            builder.setTitle(data.getLocal() + " - " + data.getId())
                                    .setCancelable(false)
                                    .setMultiChoiceItems(items, booleans, (dialogInterface, i, b) -> {
                                    })
                                    .setNegativeButton("取消", (dialogInterface, i) -> XLoadingDialog.with(HistoryActivity.this).cancel())
                                    .setPositiveButton("接单", (dialogInterface, i) -> {
                                        XLoadingDialog.with(HistoryActivity.this).setMessage("请求处理中，请稍后").setCanceled(false).show();
                                        result.setOrderDate(System.currentTimeMillis());
                                        result.setRepairer(MainViewModel.user.getUserName());
                                        result.setState((short) 1);
                                        mainViewModel.modify(result, dataBackUpString, new HttpUtil.HttpUtilCallBack<Data>() {
                                            @Override
                                            public void onResponse(Data o) {

                                                Data.update(o.getId(), new HttpUtil.HttpUtilCallBack<Data>() {
                                                    @Override
                                                    public void onResponse(Data result) {
                                                        XToast.success("接单成功");

                                                        App.getDaoSession().getDataDao().update(result);

                                                        mainViewModel.queryDataListBySetting(0);
                                                        mainViewModel.queryDataListAboutMyself(1);

                                                        notidyOneDataChange(result, position);

                                                        if (booleans[0]) {
                                                            Data.openQq(result.getQq());
                                                        }

                                                        XLoadingDialog.with(HistoryActivity.this).cancel();
                                                    }

                                                    @Override
                                                    public void onFailure(String error) {
                                                        XToast.success("接单失败\n" + error);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                XLoadingDialog.with(HistoryActivity.this).cancel();
                                                XToast.error("接单失败\n" + error);
                                            }
                                        });
                                    })
                                    .create()
                                    .show();

                        } else {
                            XLoadingDialog.with(HistoryActivity.this).cancel();
                            XToast.warning("唉呀，有人抢先了...\n该报修单已被 " + result.getRepairer() + " 处理");
                            notidyOneDataChange(result, position);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        XLoadingDialog.with(HistoryActivity.this).cancel();
                        XToast.error("请求失败\n" + error);
                    }
                });
            }
        });

        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);
        initListener();
        App.initToolbar(toolbar, this);
    }

    private void deleteSelect(SparseBooleanArray sparseBooleanArray) {
        XLoadingDialog.with(HistoryActivity.this).setCanceled(false).show();
        mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
            @Override
            public void onResponse(User result) {
                XLoadingDialog.with(XFrame.getContext()).cancel();
                if (result.haveDeleteAccess()) {
                    if (taskAdapter.selectNone()) {
                        XToast.info("请至少选择一项");
                    } else {

                        List<Integer> ids = new ArrayList<>();
                        List<Data> needToDelete = new ArrayList<>();

                        for (int i = 0; i < sparseBooleanArray.size(); i++) {
                            int key = sparseBooleanArray.keyAt(i);
                            if (sparseBooleanArray.get(key)) {
                                String id = dataList.get(key).getId().toString();
                                ids.add(Integer.valueOf(id));
                                needToDelete.add(App.getDaoSession().getDataDao().queryBuilder().where(DataDao.Properties.Id.eq(id)).build().unique());
                            }
                        }

                        StringBuilder builder = new StringBuilder();
                        for (Data data : needToDelete) {
                            builder.append(data.getLocal());
                            builder.append(" - ");
                            builder.append(data.getId());
                            builder.append("\n");
                        }

                        AlertDialog dialog = new AlertDialog.Builder(HistoryActivity.this).create();
                        dialog.setTitle("注意");
                        dialog.setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作。确认删除以下报修单？\n" + builder.toString());
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "取消", (dialogInterface, i1) -> {

                        });

                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "删除", (dialogInterface, i1) -> {
                            XLoadingDialog.with(HistoryActivity.this).setCanceled(false).setMessage("请求处理中,请稍后").show();

                            mainViewModel.deleteMany(ids, new HttpUtil.HttpUtilCallBack<Boolean>() {
                                @Override
                                public void onResponse(Boolean result1) {
                                    XLoadingDialog.with(HistoryActivity.this).cancel();
                                    if (result1) {
                                        mainViewModel.queryDataListAboutMyself(state);
                                        for (int id : ids) {
                                            for (int i = 0; i < dataList.size(); i++) {
                                                if (dataList.get(i).getId() == id) {
                                                    dataList.remove(i);
                                                    taskAdapter.notifyItemRemoved(i);
                                                    taskAdapter.notifyItemRangeChanged(i, taskAdapter.getItemCount() - i);
                                                    break;
                                                }
                                            }
                                        }
                                        taskAdapter.clearSelect();
                                        taskAdapter.cancelSelect();
                                        dialog.dismiss();
                                        XToast.success("删除成功");
                                        if (taskAdapter.getItemCount() == 0) {
                                            finish();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    XLoadingDialog.with(HistoryActivity.this).cancel();
                                    XToast.error("删除失败");
                                    dialog.cancel();
                                }
                            });
                        });

                        dialog.show();
                    }
                } else {
                    XLoadingDialog.with(XFrame.getContext()).cancel();
                    XToast.info("您已不是管理员");
                    taskAdapter.setEnableSelect(false);
                    taskAdapter.cancelSelect();
                }
            }

            @Override
            public void onFailure(String error) {
                XLoadingDialog.with(XFrame.getContext()).cancel();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //进入报修单详情
        if (requestCode == MainActivity.REQUEST_DATA_CHANGE && resultCode == Activity.RESULT_OK && data != null) {
            int position = data.getIntExtra("position", -1);
            Data data1 = GsonUtil.getInstance().fromJson(data.getStringExtra("data"), Data.class);
            if (position != -1 && data1 != null) {
                //转让成功
                boolean makeover = data.getBooleanExtra("makeover", false);
                //接单成功
                boolean order = data.getBooleanExtra("order", false);
                //反馈成功
                boolean feedback = data.getBooleanExtra("feedback", false);
                //修改成功
                boolean modify = data.getBooleanExtra("modify", false);
                if (makeover || order || feedback || modify) {
                    notidyOneDataChange(data1, position);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (data != null) {
            if (requestCode == 4 && resultCode == 0) {
                String needDeleteData = data.getStringExtra("data");
                int position = data.getIntExtra("position", -1);
//            Log.e(TAG, "onActivityResult(position): " + position);
//            Log.e(TAG, "onActivityResult: " + needDeleteData);
                if (needDeleteData != null && !needDeleteData.isEmpty()) {
                    Data dataToDelete = GsonUtil.getInstance().fromJson(needDeleteData, Data.class);
                    needDelete.add(dataToDelete);
                    dataList.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                    taskAdapter.notifyItemRangeChanged(position, dataList.size() - position);
                    if (dataList.isEmpty()) {
                        Intent intent = new Intent();
                        intent.putExtra("dataList", GsonUtil.getInstance().toJson(needDelete));
                        setResult(4, intent);
                        finish();
                    } else {
                        updateTitle(state, dataList.size());
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (taskAdapter.isSelectMode()) {
            taskAdapter.cancelSelect();
        } else if (!needDelete.isEmpty()) {
            Intent intent = new Intent();
            intent.putExtra("dataList", GsonUtil.getInstance().toJson(needDelete));
//            Log.e(TAG, "onBackPressed: " + needDelete);
            setResult(4, intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (taskAdapter.isSelectMode()) {
                taskAdapter.cancelSelect();
            } else {
                if (!needDelete.isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("dataList", GsonUtil.getInstance().toJson(needDelete));
//                Log.e(TAG, "onOptionsItemSelected: " + needDelete);
                    setResult(4, intent);
                }
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_history_delete:
                taskAdapter.finishSelect();
                break;
            default:
        }
    }

    private void initListener() {
        floatingActionButtonDelete.setOnClickListener(this);
    }

    private void updateTitle(int state, int size) {
        if (size == 0) {
            finish();
        } else {
            String realTitle;
            if (titleWithCount) {
                //没传title
                if (title == null) {
                    switch (state) {
                        case 1:
                            title = "处理中";
                            break;
                        case 2:
                            title = "已维修";
                            break;
                        default:
                            //没传state
                            title = "报修单列表";
                    }
                }
                realTitle = String.format(Locale.getDefault(), "%s（%d单）", title, size);
            } else {
                realTitle = title;
            }
            getSupportActionBar().setTitle(realTitle);
        }
    }

    private void notidyOneDataChange(Data data, int position) {
        dataList.remove(position);
        taskAdapter.notifyItemRemoved(position);
        dataList.add(position, data);
        taskAdapter.notifyItemInserted(position);
        taskAdapter.notifyItemRangeChanged(position, dataList.size() - position);
    }

}
