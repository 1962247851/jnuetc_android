package jn.mjz.aiot.jnuetc.View.Fragment;

import android.content.Context;
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
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.Greendao.Entity.User;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.View.Activity.AdminActivity;
import jn.mjz.aiot.jnuetc.View.Activity.HistoryActivity;
import jn.mjz.aiot.jnuetc.View.Activity.MainActivity;
import jn.mjz.aiot.jnuetc.View.Activity.SettingsActivity;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import okhttp3.Response;

public class MyselfFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MyselfFragment";
    private Unbinder unbinder;
    private MainViewModel mainViewModel;
    private IMyselfFragmentListener iMyselfFragmentListener;

    private static List<Data> dataList4 = new ArrayList<>();
    private List<Data> dataList5 = new ArrayList<>();

    @BindView(R.id.tv_fragment_myself_processing)
    TextView textViewProcessing;
    @BindView(R.id.tv_fragment_myself_done)
    TextView textViewDone;
    @BindView(R.id.tv_fragment_myself_admin)
    TextView textViewAdmin;
    @BindView(R.id.tv_fragment_myself_setting)
    TextView textViewSetting;
    @BindView(R.id.srl_myself)
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mainViewModel.getCurrentState().observe(this, integer -> {
            if (integer == 3) {
                textViewAdmin.setVisibility(MainViewModel.user.getRoot() != 0 ? View.VISIBLE : View.GONE);
            }
        });
        mainViewModel.queryAll(new HttpUtil.HttpUtilCallBack<List<Data>>() {
            @Override
            public void onResponse(Response response, List<Data> result) {
                mainViewModel.queryDataListAboutMyself(1);
                mainViewModel.queryDataListAboutMyself(2);
                mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                    @Override
                    public void onResponse(Response response, User result) {
                        textViewAdmin.setVisibility(result.getRoot() != 0 ? View.VISIBLE : View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(IOException e) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }

            @Override
            public void onFailure(IOException e) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        mainViewModel.getDataList4().observe(this, data -> {
            dataList4.clear();
            dataList4.addAll(data);
            if (textViewProcessing != null && dataList4.size() != 0) {
                textViewProcessing.setText(String.format(Locale.getDefault(), "处理中 %d", dataList4.size()));
            } else {
                textViewProcessing.setText("暂无处理中的报修单");
            }
        });
        mainViewModel.getDataList5().observe(this, data -> {
            dataList5.clear();
            dataList5.addAll(data);
            if (textViewDone != null && dataList5.size() != 0) {
                textViewDone.setText(String.format(Locale.getDefault(), "已维修 %d", dataList5.size()));
            } else {
                textViewDone.setText("暂无已维修的报修单");
            }
        });
        if (MainActivity.setting.getBoolean("show_reg_time", true)) {
            TimerFragment timerFragment = new TimerFragment("与你相识的第", MainViewModel.user.getRegDate());
            if (getChildFragmentManager().findFragmentByTag(TimerFragment.TAG) == null) {
                getChildFragmentManager().beginTransaction().add(R.id.frameLayout_myself_timer, timerFragment).commit();
            }
        }
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
            case R.id.tv_fragment_myself_setting:
                Intent intent3 = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent3);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.e(TAG, "onActivityResult: requestCode " + requestCode + "resultCode " + resultCode);
//        Log.e(TAG, "onActivityResult:data "+data.getStringExtra("data") );
//        Log.e(TAG, "onActivityResult:dataList "+data.getStringExtra("dataList") );
        if (data != null && requestCode == 4 && resultCode == 4) {
            //反馈（有自己名字、没有自己名字）、转让,
            String dataListString = data.getStringExtra("dataList");
            if (dataListString != null && !dataListString.isEmpty()) {
                List<Data> needDeleteOrUpdate = GsonUtil.parseJsonArray2ObejctList(dataListString, Data.class);
                for (Data data1 : needDeleteOrUpdate) {
                    for (Data data2 : dataList4) {
                        if (data2.getId().equals(data1.getId())) {
                            dataList4.remove(data2);
                            if (data1.getState() == 2) {//反馈（有自己名字、没有自己名字）
                                if (data1.getRepairer().contains(MainViewModel.user.getName())) {//反馈成功，有自己名字
                                    dataList5.add(0, data1);
                                } else {//反馈成功，没有自己名字
                                    SecondFragment.notifyDataList3Inserted(data1);
                                }
                            } else if (data1.getState() == 1) {//转让
                                SecondFragment.notifyDataList2Inserted(data1);
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

    public MyselfFragment(IMyselfFragmentListener iMyselfFragmentListener) {
        this.iMyselfFragmentListener = iMyselfFragmentListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myself, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);
        InitListener();
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
        textViewSetting.setOnClickListener(this);

        swipeRefreshLayout.setOnRefreshListener(this::updateData);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateData() {
        mainViewModel.queryAll(new HttpUtil.HttpUtilCallBack<List<Data>>() {
            @Override
            public void onResponse(Response response, List<Data> result) {
                mainViewModel.queryDataListAboutMyself(1);
                mainViewModel.queryDataListAboutMyself(2);
                mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                    @Override
                    public void onResponse(Response response, User result) {
                        textViewAdmin.setVisibility(result.getRoot() != 0 ? View.VISIBLE : View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        XToast.success("数据更新成功");
                    }

                    @Override
                    public void onFailure(IOException e) {
                        if (e == null) {
                            iMyselfFragmentListener.OnUserInfoChanged();
                        }
                        XToast.error("数据更新失败");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                XToast.error("数据更新失败");
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void autoRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        updateData();
    }

    public interface IMyselfFragmentListener {
        void OnUserInfoChanged();
    }
}
