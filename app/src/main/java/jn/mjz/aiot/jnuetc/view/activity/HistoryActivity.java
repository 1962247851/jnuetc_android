package jn.mjz.aiot.jnuetc.view.activity;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.MyApplication;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.User;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.view.adapter.RecyclerView.TaskAdapter;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;
import okhttp3.Response;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HistoryActivity";
    private List<Data> dataList = new ArrayList<>();
    private TaskAdapter adapter;
    private List<Data> needDelete = new ArrayList<>();
    private MainViewModel mainViewModel = MainActivity.mainViewModel;
    private int state;

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
        dataList.addAll(GsonUtil.parseJsonArray2ObejctList(dataListJsonString, Data.class));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        XStatusBar.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));

        getSupportActionBar().setTitle(state == 1 ? String.format(Locale.getDefault(), "处理中（%d）", dataList.size()) : String.format(Locale.getDefault(), "已维修（%d）", dataList.size()));

        adapter = new TaskAdapter(MainViewModel.user.haveDeleteAccess(), dataList, this, new TaskAdapter.ITaskListener() {
            @Override
            public void OnItemClick(int position, Data data) {
                Intent intent = new Intent(HistoryActivity.this, DetailsActivity.class);
                intent.putExtra("data", data.toString());
                intent.putExtra("position", position);
                startActivityForResult(intent, 4);
            }

            @Override
            public void OnStartSelect(int count) {
                floatingActionButtonDelete.show();
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, dataList.size()));
            }

            @Override
            public void OnSelect(int count) {
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, dataList.size()));
            }

            @Override
            public void OnConfirmSelect(SparseBooleanArray sparseBooleanArray) {
                deleteSelect(sparseBooleanArray);
            }

            @Override
            public void OnCancelSelect() {
                floatingActionButtonDelete.hide();
                getSupportActionBar().setTitle(state == 1 ? String.format(Locale.getDefault(), "处理中（%d）", dataList.size()) : String.format(Locale.getDefault(), "已维修（%d）", dataList.size()));
            }


            @Override
            public void OnConfirmClick(int position, Data data) {
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        InitListener();
    }


    private void deleteSelect(SparseBooleanArray sparseBooleanArray) {
        mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
            @Override
            public void onResponse(Response response, User result) {
                if (result.haveDeleteAccess()) {
                    if (adapter.selectNone()) {
                        XToast.info("请至少选择一项");
                    } else {

                        List<Integer> ids = new ArrayList<>();
                        List<Data> needToDelete = new ArrayList<>();

                        for (int i = 0; i < sparseBooleanArray.size(); i++) {
                            int key = sparseBooleanArray.keyAt(i);
                            if (sparseBooleanArray.get(key)) {
                                String id = dataList.get(key).getId().toString();
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

                        AlertDialog dialog = new AlertDialog.Builder(HistoryActivity.this).create();
                        dialog.setTitle("注意");
                        dialog.setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作。确认删除以下报修单？\n" + builder.toString());
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "取消", (dialogInterface, i1) -> {

                        });

                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "删除", (dialogInterface, i1) -> {
                            XLoadingDialog.with(HistoryActivity.this).setCanceled(false).setMessage("请求处理中,请稍后").show();

                            mainViewModel.deleteMany(ids, new HttpUtil.HttpUtilCallBack<Boolean>() {
                                @Override
                                public void onResponse(Response response1, Boolean result1) {

                                    mainViewModel.dataDao.deleteInTx(needToDelete);

                                    mainViewModel.queryDataListAboutMyself(state);

                                    for (int id : ids) {
                                        for (int i = 0; i < dataList.size(); i++) {
                                            if (dataList.get(i).getId() == id) {
                                                dataList.remove(i);
                                                adapter.notifyItemRemoved(i);
                                                adapter.notifyItemRangeChanged(i, adapter.getItemCount() - i);
                                                break;
                                            }
                                        }
                                    }

                                    adapter.clearSelect();
                                    adapter.cancelSelect();

                                    XLoadingDialog.with(HistoryActivity.this).cancel();
                                    dialog.dismiss();
                                    XToast.success("删除成功");
                                }

                                @Override
                                public void onFailure(IOException e) {
                                    XLoadingDialog.with(HistoryActivity.this).cancel();
                                    XToast.error("删除失败");
                                    dialog.cancel();
                                }
                            });
                        });

                        dialog.show();
                    }
                } else {
                    XToast.info("您已不是管理员");
                    adapter.setEnableSelect(false);
                    adapter.cancelSelect();
                }
            }

            @Override
            public void onFailure(IOException e) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, dataList.size() - position);
                    if (dataList.isEmpty()) {
                        Intent intent = new Intent();
                        intent.putExtra("dataList", GsonUtil.getInstance().toJson(needDelete));
                        setResult(4, intent);
                        finish();
                    } else {
                        getSupportActionBar().setTitle(String.format(Locale.getDefault(), "处理中（%d）", dataList.size()));
                    }
                }
            }
            //修改后更新
            if (resultCode == 1) {
                boolean success = data.getBooleanExtra("modify", false);
                if (success) {
                    Data data1 = GsonUtil.getInstance().fromJson(data.getStringExtra("data"), Data.class);
                    int position = data.getIntExtra("position", -1);
                    dataList.remove(position);
                    adapter.notifyItemRemoved(position);
                    dataList.add(position, data1);
                    adapter.notifyItemRangeChanged(position, dataList.size() - position);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (adapter.isSelectMode()) {
            adapter.cancelSelect();
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
            if (adapter.isSelectMode()) {
                adapter.cancelSelect();
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
                adapter.finishSelect();
                break;
        }
    }

    private void InitListener() {
        floatingActionButtonDelete.setOnClickListener(this);
    }
}