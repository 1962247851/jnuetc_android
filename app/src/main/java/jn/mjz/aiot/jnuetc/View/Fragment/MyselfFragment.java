package jn.mjz.aiot.jnuetc.View.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jn.mjz.aiot.jnuetc.Greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.View.Activity.AdminActivity;
import jn.mjz.aiot.jnuetc.View.Activity.HistoryActivity;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import okhttp3.Response;

public class MyselfFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MyselfFragment";
    private Unbinder unbinder;
    private MainViewModel mainViewModel;

    private List<Data> dataList4 = new ArrayList<>();
    private List<Data> dataList5 = new ArrayList<>();

    @BindView(R.id.tv_fragment_myself_processing)
    TextView textViewProcessing;
    @BindView(R.id.tv_fragment_myself_done)
    TextView textViewDone;
    @BindView(R.id.tv_fragment_myself_admin)
    TextView textViewAdmin;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

        mainViewModel.getDataList2().observe(this, new Observer<List<Data>>() {
            @Override
            public void onChanged(List<Data> data) {
                mainViewModel.getDataList4().setValue(mainViewModel.dataDao.queryBuilder()
                        .where(DataDao.Properties.Repairer.like(GlobalUtil.user.getName())
                                , DataDao.Properties.State.eq(1))
                        .orderAsc(DataDao.Properties.Date)
                        .build()
                        .list());
            }
        });

        mainViewModel.getDataList3().observe(this, new Observer<List<Data>>() {
            @Override
            public void onChanged(List<Data> data) {
                mainViewModel.getDataList5().setValue(mainViewModel.dataDao.queryBuilder()
                        .where(DataDao.Properties.Repairer.like(GlobalUtil.user.getName())
                                , DataDao.Properties.State.eq(2))
                        .orderDesc(DataDao.Properties.RepairDate)
                        .build()
                        .list());
            }
        });

        mainViewModel.getDataList4().observe(this, data -> {
            Log.e(TAG, "onChanged4: " + data);
            dataList4.clear();
            dataList4.addAll(data);
            if (textViewProcessing != null) {
                textViewProcessing.setText(String.format(Locale.getDefault(), "处理中 %d", dataList4.size()));
            }
        });
        mainViewModel.getDataList5().observe(this, data -> {
            Log.e(TAG, "onChanged5: " + data);
            dataList5.clear();
            dataList5.addAll(data);
            if (textViewDone != null) {
                textViewDone.setText(String.format(Locale.getDefault(), "已维修 %d", dataList5.size()));
            }
        });

        mainViewModel.haveRoot(new HttpUtil.HttpUtilCallBack<Boolean>() {
            @Override
            public void onResponse(Response response, Boolean result) {
                textViewAdmin.setVisibility(result ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(IOException e) {
            }
        });

        InitListener();

        getChildFragmentManager().beginTransaction().add(R.id.frameLayout_myself_timer, new TimerFragment("与你相识的第", GlobalUtil.user.getRegDate())).commitAllowingStateLoss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_fragment_myself_admin:
                Intent intent = new Intent(getContext(), AdminActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_fragment_myself_processing:
                if (dataList4.isEmpty()) {
                    XToast.info("暂无数据");
                } else {
                    Intent intent1 = new Intent(getContext(), HistoryActivity.class);
                    intent1.putExtra("dataList", GsonUtil.getInstance().toJson(dataList4));
                    intent1.putExtra("state", 1);
                    //删除反馈的报修单，同时添加到已维修里
                    startActivityForResult(intent1, 4);
                }
                break;
            case R.id.tv_fragment_myself_done:
                if (dataList5.isEmpty()) {
                    XToast.info("暂无数据");
                } else {
                    Intent intent2 = new Intent(getContext(), HistoryActivity.class);
                    intent2.putExtra("dataList", GsonUtil.getInstance().toJson(dataList5));
                    intent2.putExtra("state", 2);
                    startActivity(intent2);
                }

                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult: dataList5 " + dataList5);
//        Log.e(TAG, "onActivityResult: requestCode " + requestCode + "resultCode " + resultCode);
//        Log.e(TAG, "onActivityResult:data "+data.getStringExtra("data") );
//        Log.e(TAG, "onActivityResult:dataList "+data.getStringExtra("dataList") );
        if (data != null && requestCode == 4 && resultCode == 4) {
            String dataListString = data.getStringExtra("dataList");
            if (dataListString != null && !dataListString.isEmpty()) {
                List<Data> needDelete = GsonUtil.parseJsonArray2ObejctList(dataListString, Data.class);
                for (Data data1 : needDelete) {
                    for (Data data2 : dataList4) {
                        if (data2.getId().equals(data1.getId())) {
                            dataList4.remove(data2);
                            // TODO: 2019/8/6 通知dataList2和dataList3
//                            mainViewModel.getDataList2().getValue().remove(data2);
                            if (data1.getRepairer().contains(GlobalUtil.user.getName())) {//转让操作

                                dataList5.add(0, data1);
                            } else {
                                // TODO: 2019/8/6 更新dataList2

                            }
                            break;
                        }
                    }
                }
                //因为是setValue，地址不变，如果clear则dataList4也会清空
                List<Data> dataList4Temp = new ArrayList<>(dataList4);
                List<Data> dataList5Temp = new ArrayList<>(dataList5);
                mainViewModel.getDataList4().setValue(dataList4Temp);
                mainViewModel.getDataList5().setValue(dataList5Temp);
            }
        }
    }

    public MyselfFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myself, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }


    private void InitListener() {
        textViewProcessing.setOnClickListener(this);
        textViewDone.setOnClickListener(this);
        textViewAdmin.setOnClickListener(this);
    }

}
