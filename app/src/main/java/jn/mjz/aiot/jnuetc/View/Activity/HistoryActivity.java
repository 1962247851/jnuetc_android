package jn.mjz.aiot.jnuetc.View.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.xframe.utils.statusbar.XStatusBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.View.Adapter.RecyclerView.TaskAdapter;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private List<Data> dataList = new ArrayList<>();
    private TaskAdapter adapter;
    private List<Data> needDelete = new ArrayList<>();

    @BindView(R.id.recyclerView_history)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar_history)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        XStatusBar.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));

        int state = getIntent().getIntExtra("state", 0);
        String dataListJsonString = getIntent().getStringExtra("dataList");
//        Log.d(TAG, "onCreate: dataListJsonString\n" + dataListJsonString);

        dataList.addAll(GsonUtil.parseJsonArray2ObejctList(dataListJsonString, Data.class));

        getSupportActionBar().setTitle(state == 1 ? String.format(Locale.getDefault(), "处理中（%d）", dataList.size()) : String.format(Locale.getDefault(), "已维修（%d）", dataList.size()));

        adapter = new TaskAdapter(false, dataList, this, new TaskAdapter.ITaskListener() {
            @Override
            public void OnItemClick(int position, Data data) {
                Intent intent = new Intent(HistoryActivity.this, DetailsActivity.class);
                intent.putExtra("data", data.toString());
                Log.e(TAG, "OnItemClick(position): " + position);
                intent.putExtra("position", position);
                startActivityForResult(intent, 4);
            }

            @Override
            public void OnStartSelect(int count) {
            }

            @Override
            public void OnSelect(int count) {

            }

            @Override
            public void OnConfirmSelect(SparseBooleanArray sparseBooleanArray) {

            }

            @Override
            public void OnCancelSelect() {

            }


            @Override
            public void OnConfirmClick(int position, Data data) {
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null && requestCode == 4 && resultCode == 0) {
            String needDeleteData = data.getStringExtra("data");
            int position = data.getIntExtra("position", -1);
            Log.e(TAG, "onActivityResult(position): " + position);
            Log.e(TAG, "onActivityResult: " + needDeleteData);
            if (needDeleteData != null && !needDeleteData.isEmpty()) {
                Data dataToDelete = GsonUtil.getInstance().fromJson(needDeleteData, Data.class);
                needDelete.add(dataToDelete);
                dataList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, dataList.size() - position);
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "处理中（%d）", dataList.size()));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!needDelete.isEmpty()) {
            Intent intent = new Intent();
            intent.putExtra("dataList", GsonUtil.getInstance().toJson(needDelete));
            Log.e(TAG, "onBackPressed: " + needDelete);
            setResult(4, intent);
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!needDelete.isEmpty()) {
                Intent intent = new Intent();
                intent.putExtra("dataList", GsonUtil.getInstance().toJson(needDelete));
                Log.e(TAG, "onOptionsItemSelected: " + needDelete);
                setResult(4, intent);
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
