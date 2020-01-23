package jn.mjz.aiot.jnuetc.view.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.log.XLog;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.databinding.FragmentFeedbackBinding;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.view.adapter.pager.MyFragmentPagerAdapter;
import jn.mjz.aiot.jnuetc.view.custom.ScrollViewPager;
import jn.mjz.aiot.jnuetc.view.fragment.DataChangeLogFragment;
import jn.mjz.aiot.jnuetc.view.fragment.DetailsFragment;
import jn.mjz.aiot.jnuetc.view.fragment.FeedbackFragment;
import jn.mjz.aiot.jnuetc.viewmodel.DetailsViewModel;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class DetailsActivity extends AppCompatActivity implements DetailsFragment.OnFragmentInteractionListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "DetailsActivity";
    private DetailsViewModel detailsViewModel;
    private MyFragmentPagerAdapter myFragmentPagerAdapter;
    private Menu menu;
    @BindView(R.id.viewPager_details)
    ScrollViewPager scrollViewPager;
    @BindView(R.id.toolbar_details)
    Toolbar toolbar;
    @BindView(R.id.tabLayout_details)
    TabLayout tabLayout;
    @BindView(R.id.srl_details)
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        detailsViewModel = ViewModelProviders.of(DetailsActivity.this).get(DetailsViewModel.class);
        Intent intent = getIntent();
        if (intent != null) {
            long id = intent.getLongExtra("id", -1);
            swipeRefreshLayout.setRefreshing(true);
            Data.update(id, new HttpUtil.HttpUtilCallBack<Data>() {
                @Override
                public void onResponse(Data result) {
                    DataChangeLogFragment.sortLogByTimeDesc(result.getDataChangeLogs());
                    detailsViewModel.setDataStringBackup(result.toString());
                    detailsViewModel.getData().setValue(result);
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(String error) {
                    Data unique = App.getDaoSession().getDataDao().queryBuilder().where(DataDao.Properties.Id.eq(id)).build().unique();
                    DataChangeLogFragment.sortLogByTimeDesc(unique.getDataChangeLogs());
                    detailsViewModel.setDataStringBackup(unique.toString());
                    detailsViewModel.getData().setValue(unique);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        initListener();
        scrollViewPager.setScroll(true);
        scrollViewPager.setOffscreenPageLimit(detailsViewModel.getFragmentList().size());
        tabLayout.setupWithViewPager(scrollViewPager);
        XStatusBar.setColorNoTranslucent(this, XFrame.getColor(R.color.colorPrimary));
        App.initToolbar(toolbar, this);
    }

    private void initListener() {
        detailsViewModel.getData().observe(this, data -> {
            if (data != null) {
                if (scrollViewPager.getAdapter() == null) {
                    List<Fragment> fragmentList = detailsViewModel.getFragmentList();
                    List<String> pageTitleList = detailsViewModel.getPageTitleList();
                    if (data.getState() != 2 && fragmentList.size() == 4) {
                        fragmentList.remove(3);
                        pageTitleList.remove(3);
                    }
                    myFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList, pageTitleList);
                    scrollViewPager.setAdapter(myFragmentPagerAdapter);
                }
                int dataLogSize = data.getDataChangeLogs().size();
                TabLayout.Tab tabAt = tabLayout.getTabAt(2);
                if (tabAt != null) {
                    tabAt.setText(dataLogSize == 0 ? "日志" : "日志（" + dataLogSize + "）");
                }
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "%s - %d", data.getLocal(), data.getId()));
                if (menu != null) {
                    switch (data.getState()) {
                        case 0:
                            if (MainViewModel.getUser().haveModifyAccess()) {
                                menu.findItem(R.id.menu_details_modify).setVisible(true);
                            }
                            break;
                        case 1:
                        case 2:
                            if (MainViewModel.getUser().haveRelationWithData(data) || MainViewModel.getUser().haveModifyAccess()) {
                                menu.findItem(R.id.menu_details_modify).setVisible(true);
                            }
                            break;
                        default:
                    }
                    if (data.getState() == 1 && MainViewModel.getUser().haveRelationWithData(data)) {
                        menu.findItem(R.id.menu_details_make_over).setVisible(true);
                        menu.findItem(R.id.menu_details_input_repair_message).setVisible(true);
                    } else {
                        menu.findItem(R.id.menu_details_make_over).setVisible(false);
                        menu.findItem(R.id.menu_details_input_repair_message).setVisible(false);
                    }
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_details, menu);
        this.menu = menu;
        detailsViewModel.getModifyMode().observe(this, aBoolean -> {
            MenuItem item = menu.findItem(R.id.menu_details_modify);
            item.setIcon(aBoolean ? R.drawable.ic_done_white : R.drawable.ic_modify_white);
            item.setTitle(aBoolean ? XFrame.getString(R.string.Finish) : XFrame.getString(R.string.Modify));
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_details_make_over:
                XLoadingDialog.with(DetailsActivity.this).setCanceled(false).show();
                MainViewModel.queryAllUser(new HttpUtil.HttpUtilCallBack<List<String>>() {
                    @Override
                    public void onResponse(List<String> result) {
                        XLoadingDialog.with(DetailsActivity.this).cancel();
                        if (result.isEmpty()) {
                            XToast.info("暂无可转让的人");
                        } else {
                            result.remove(MainViewModel.getUser().getUserName());
                            String[] names = new String[result.size()];
                            result.toArray(names);
                            boolean[] checkItems = new boolean[result.size()];
                            AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                            builder.setTitle("请选择一个或多个被转让人")
                                    .setMultiChoiceItems(names, checkItems, (dialogInterface, i, b) -> {
                                    })
                                    .setPositiveButton("确定", null)
                                    .setNeutralButton("取消", null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                                StringBuilder names1 = new StringBuilder();
                                for (int j = 0; j < checkItems.length; j++) {
                                    if (checkItems[j]) {
                                        names1.append(result.get(j));
                                        names1.append("，");
                                    }
                                }
                                if (names1.toString().isEmpty()) {
                                    XToast.info("未选择");
                                } else {
                                    names1.deleteCharAt(names1.length() - 1);
                                    String repairer = names1.toString();
                                    if (!repairer.isEmpty() && !repairer.contains(MainViewModel.getUser().getUserName())) {
                                        XLoadingDialog.with(DetailsActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
                                        String oldJson = detailsViewModel.getData().getValue().toString();
                                        detailsViewModel.getData().getValue().setState((short) 1);
                                        detailsViewModel.getData().getValue().setRepairer(repairer);
                                        detailsViewModel.getData().getValue().modify(
                                                oldJson, new HttpUtil.HttpUtilCallBack<Data>() {
                                                    @Override
                                                    public void onResponse(Data result) {
                                                        dialog.cancel();
                                                        XLoadingDialog.with(DetailsActivity.this).cancel();
                                                        XToast.success("转让成功");
                                                        Intent intent = new Intent();
                                                        intent.putExtra("makeover", true);
                                                        intent.putExtra("position", getIntent().getIntExtra("position", -1));
                                                        intent.putExtra("data", result.toString());
                                                        setResult(Activity.RESULT_OK, intent);
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onFailure(String error) {
                                                        XLoadingDialog.with(DetailsActivity.this).cancel();
                                                        XToast.error("转让失败\n" + error);
                                                    }
                                                }
                                        );
                                    } else {
                                        XToast.info("请检查被转让人");
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        XLoadingDialog.with(DetailsActivity.this).cancel();
                        XToast.error("转让失败\n" + error);
                    }
                });
                return true;
            case android.R.id.home:
                preFinish();
                return true;
            case R.id.menu_details_input_repair_message:
                showFeedbackDialog(detailsViewModel.getData().getValue());
                return true;
            default:
                if (detailsViewModel.getModifyMode().getValue()) {
                    XLog.d("修改" + detailsViewModel.getData().getValue().toString() + "\n备份" + detailsViewModel.getDataStringBackup());
                    if (detailsViewModel.getData().getValue().toString().equals(detailsViewModel.getDataStringBackup())) {
                        XToast.info("未更改信息");
                        detailsViewModel.getModifyMode().setValue(false);
                    } else if (detailsViewModel.getData().getValue().isAllNotEmpty()) {
                        XLoadingDialog.with(DetailsActivity.this).setCanceled(false).show();
                        detailsViewModel.getData().getValue().modify(
                                detailsViewModel.getDataStringBackup(),
                                new HttpUtil.HttpUtilCallBack<Data>() {
                                    @Override
                                    public void onResponse(Data result) {
                                        DataChangeLogFragment.sortLogByTimeDesc(result.getDataChangeLogs());
                                        detailsViewModel.setDataStringBackup(result.toString());
                                        detailsViewModel.getModifyMode().setValue(false);
                                        detailsViewModel.getData().setValue(result);
                                        Intent intent = new Intent();
                                        intent.putExtra("modify", true);
                                        intent.putExtra("data", result.toString());
                                        intent.putExtra("position", getIntent().getIntExtra("position", -1));
                                        setResult(Activity.RESULT_OK, intent);
                                        XLoadingDialog.with(DetailsActivity.this).cancel();
                                        XToast.success("修改成功");
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        XLoadingDialog.with(DetailsActivity.this).cancel();
                                        XToast.error("修改失败\n" + error);
                                    }
                                }
                        );
                    } else {
                        XToast.info("字段不能全为空");
                    }
                } else {
                    if (detailsViewModel.getData().getValue().getState() != 2) {
                        if (scrollViewPager.getCurrentItem() != 0) {
                            scrollViewPager.setCurrentItem(0, true);
                        }
                    } else {
                        if (!(scrollViewPager.getCurrentItem() == 0 || scrollViewPager.getCurrentItem() == 3)) {
                            scrollViewPager.setCurrentItem(3, true);

                        }
                    }
                    detailsViewModel.getModifyMode().setValue(true);
                }
                return true;
        }
    }

    @Override
    public void onBackPressed() {
        preFinish();
    }

    private void preFinish() {
        if (detailsViewModel.getModifyMode().getValue()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
            builder.setTitle("退出编辑模式？")
                    .setMessage("更改将不会保存")
                    .setPositiveButton("取消", null)
                    .setNegativeButton("退出", (dialog, which) -> {
                        if (!detailsViewModel.getData().getValue().toString().equals(detailsViewModel.getDataStringBackup())) {
                            detailsViewModel.getData().setValue(
                                    GsonUtil.getInstance().fromJson(
                                            detailsViewModel.getDataStringBackup(),
                                            Data.class)
                            );
                        }
                        detailsViewModel.getModifyMode().setValue(false);
                    });
            builder.create().show();
        } else {
            finish();
        }
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(DetailsActivity.this, GalleryActivity.class);
        intent.putExtra(GalleryActivity.URLS,
                GsonUtil.getInstance().toJson(
                        detailsViewModel.getData().getValue().getPhotoUrlList()
                )
        );
        intent.putExtra(GalleryActivity.FIRST_INDEX,
                position
        );
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        Data.update(
                detailsViewModel.getData().getValue().getId(),
                new HttpUtil.HttpUtilCallBack<Data>() {
                    @Override
                    public void onResponse(Data result) {
                        if (!result.toString().equals(detailsViewModel.getDataStringBackup())) {
                            detailsViewModel.getData().setValue(result);
                            detailsViewModel.setDataStringBackup(result.toString());
                        }
                        XToast.success("数据更新成功");
                        swipeRefreshLayout.setRefreshing(false);
                        detailsViewModel.getModifyMode().setValue(false);
                    }

                    @Override
                    public void onFailure(String error) {
                        XToast.error("数据更新失败\n" + error);
                        detailsViewModel.getModifyMode().setValue(false);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

    }

    private void showFeedbackDialog(Data data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
        FragmentFeedbackBinding fragmentFeedbackBinding = DataBindingUtil.inflate(
                LayoutInflater.from(DetailsActivity.this), R.layout.fragment_feedback, null, false
        );
        fragmentFeedbackBinding.setData(data);
        fragmentFeedbackBinding.setModifyMode(true);
        fragmentFeedbackBinding.setFeedbackMode(true);
        fragmentFeedbackBinding.setOnRepairerClick(v -> changeRepairer());
        fragmentFeedbackBinding.setRepairMessageTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.setRepairMessage(s.toString());
            }
        });
        fragmentFeedbackBinding.setOnMarkSelectListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.setMark(Data.getMarks().get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        fragmentFeedbackBinding.setOnServiceSelectListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.setService(Data.getServices().get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        builder.setView(fragmentFeedbackBinding.getRoot())
                .setCancelable(false)
                .setPositiveButton("完成", null)
                .setNegativeButton("取消", (builder1, which) -> {
                    if (!detailsViewModel.getData().getValue().toString().equals(detailsViewModel.getDataStringBackup())) {
                        detailsViewModel.getData().setValue(
                                GsonUtil.getInstance().fromJson(detailsViewModel.getDataStringBackup(), Data.class)
                        );
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            data.setState((short) 2);
            if (data.isAllNotEmpty()) {
                if (!data.getRepairer().contains(MainViewModel.getUser().getUserName())) {
                    new AlertDialog.Builder(DetailsActivity.this)
                            .setMessage("维修人未含本人，确定提交反馈？")
                            .setTitle("注意")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确定", (dialogInterface, i) -> {
                                dialog.cancel();
                                feedback(dialog, data);
                            }).create().show();
                } else {
                    feedback(dialog, data);
                }
            } else {
                XToast.info("字段不能全为空");
            }
        });
    }

    private void changeRepairer() {
        XLoadingDialog.with(DetailsActivity.this).setCanceled(false).show();
        MainViewModel.queryAllUser(new HttpUtil.HttpUtilCallBack<List<String>>() {
            @Override
            public void onResponse(List<String> result) {
                XLoadingDialog.with(DetailsActivity.this).cancel();
                String[] names = new String[result.size()];
                result.toArray(names);
                boolean[] checkItems = new boolean[result.size()];
                AlertDialog.Builder repairerBuilder = new AlertDialog.Builder(DetailsActivity.this);
                repairerBuilder.setTitle("请选择一个或多个维修人")
                        .setMultiChoiceItems(names, checkItems, (dialogInterface, i, b) -> {
                        })
                        .setPositiveButton("确定", null)
                        .setNeutralButton("取消", null);
                AlertDialog dialog = repairerBuilder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                    StringBuilder names1 = new StringBuilder();
                    for (int j = 0; j < checkItems.length; j++) {
                        if (checkItems[j]) {
                            names1.append(result.get(j));
                            names1.append("，");
                        }
                    }
                    if (names1.toString().isEmpty()) {
                        XToast.info("未选择");
                    } else {
                        names1.deleteCharAt(names1.length() - 1);
                        String repairer = names1.toString();
                        if (!repairer.isEmpty()) {
                            detailsViewModel.getData().getValue().setRepairer(repairer);
                            dialog.dismiss();
                        } else {
                            XToast.info("请检查维修人");
                        }
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                XLoadingDialog.with(DetailsActivity.this).cancel();
                XToast.error("获取用户列表失败\n" + error);
            }
        });
    }

    private void feedback(AlertDialog dialog, Data data) {
        XLoadingDialog.with(DetailsActivity.this).setCanceled(false).show();
        data.setRepairDate(System.currentTimeMillis());
        data.modify(detailsViewModel.getDataStringBackup(), new HttpUtil.HttpUtilCallBack<Data>() {
            @Override
            public void onResponse(Data result) {
                XLoadingDialog.with(DetailsActivity.this).cancel();
                detailsViewModel.getData().setValue(result);
                detailsViewModel.setDataStringBackup(result.toString());
                dialog.cancel();
                Intent intent = new Intent();
                intent.putExtra("feedback", true);
                intent.putExtra("data", result.toString());
                intent.putExtra("position", getIntent().getIntExtra("position", -1));
                setResult(Activity.RESULT_OK, intent);
                List<Fragment> fragmentList = detailsViewModel.getFragmentList();
                List<String> pageTitleList = detailsViewModel.getPageTitleList();
                fragmentList.add(FeedbackFragment.newInstance(result.toString()));
                pageTitleList.add("维修反馈");
                myFragmentPagerAdapter.notifyDataSetChanged();
                scrollViewPager.setCurrentItem(3, true);
                XToast.success("反馈提交成功");
            }

            @Override
            public void onFailure(String error) {
                XLoadingDialog.with(DetailsActivity.this).cancel();
                XToast.error("反馈提交失败\n" + error);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        detailsViewModel.getData().setValue(
                GsonUtil.getInstance().fromJson(
                        detailsViewModel.getDataStringBackup(), Data.class
                )
        );
    }
}
