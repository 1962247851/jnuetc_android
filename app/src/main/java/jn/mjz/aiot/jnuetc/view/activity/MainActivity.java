package jn.mjz.aiot.jnuetc.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.youth.xframe.XFrame;
import com.youth.xframe.common.XActivityStack;
import com.youth.xframe.utils.XDateUtils;
import com.youth.xframe.utils.XEmptyUtils;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.http.XHttp;
import com.youth.xframe.utils.permission.XPermission;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.User;
import jn.mjz.aiot.jnuetc.util.GlobalUtil;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.util.SharedPreferencesUtil;
import jn.mjz.aiot.jnuetc.view.adapter.pager.MyFragmentPagerAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.CheckableAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.TaskAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.WrapContentLinearLayoutManager;
import jn.mjz.aiot.jnuetc.view.custom.ScrollViewPager;
import jn.mjz.aiot.jnuetc.view.fragment.MyselfFragment;
import jn.mjz.aiot.jnuetc.view.fragment.NewTaskFragment;
import jn.mjz.aiot.jnuetc.view.fragment.SecondFragment;
import jn.mjz.aiot.jnuetc.view.fragment.TimerFragment;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener {

    private static final String TAG = "MainActivity";

    private String inputString = "";

    /**
     * 进入报修单详情界面，可能会有接单、反馈、修改、转让操作
     */
    public static final Integer REQUEST_DATA_CHANGE = 4;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar_main)
    Toolbar toolbar;
    @BindView(R.id.scrollViewPager_main)
    ScrollViewPager scrollViewPager;
    @BindView(R.id.bottomNavigationView)
    BottomNavigationView bottomNavigationView;
    TextView textViewSelectAllNorth;
    TextView textViewSelectAllSouth;
    TextView textViewSubTitle;
    TextView textViewTitleHost;
    @BindView(R.id.recyclerView_main_search)
    RecyclerView recyclerView;
    @BindView(R.id.button_main_skip)
    Button textViewSkip;
    private List<Data> dataList = new ArrayList<>();
    private TaskAdapter taskAdapter;

    private SearchView searchView;

    @BindView(R.id.fab_main_delete)
    FloatingActionButton floatingActionButtonDelete;
    @BindView(R.id.frameLayout_main_header)
    FrameLayout frameLayout;
    @BindView(R.id.frameLayout_main_welcome)
    FrameLayout frameLayoutWelcome;
    @BindView(R.id.imageView_main_welcome)
    ImageView imageView;
    @BindView(R.id.progressBar_main_welcome)
    ProgressBar progressBar;

    /**
     * 小米推送用到的数据
     */
    public static final String APP_ID = "2882303761518054312";
    public static final String APP_KEY = "5181805484312";

    private Animation animation = new AlphaAnimation(1, 0.1f);

    private long backTime = 0;

    public RadioGroup radioGroup;

    public static MainViewModel mainViewModel;

    private NewTaskFragment newTaskFragment;
    private SecondFragment secondFragment;
    private MyselfFragment myselfFragment;
    private NewTaskFragment.INewTaskListener iNewTaskListener;
    private SecondFragment.ISecondListener iSecondListener;
    private MyselfFragment.IMyselfFragmentListener iMyselfFragmentListener;

    private List<Fragment> fragments = new ArrayList<>();
    private MyFragmentPagerAdapter pagerAdapter;

    private CheckableAdapter checkableAdapterNorth;
    private CheckableAdapter checkableAdapterSouth;
    public static SharedPreferences setting = SharedPreferencesUtil.getSettingPreferences();

    int cnt = Integer.parseInt(setting.getString("show_time", "5"));
    MyHandler myHandler = new MyHandler();

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                myHandler.postDelayed(() -> {
                    textViewSkip.setText(String.format(Locale.getDefault(), "跳过%d", cnt));
                    if (--cnt >= 0) {
                        myHandler.sendEmptyMessage(0);
                    } else {
                        if (!animation.hasStarted()) {
                            frameLayoutWelcome.startAnimation(animation);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    frameLayoutWelcome.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }
                    }
                }, 1000);
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XActivityStack.getInstance().addActivity(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.checkDayDayPhotoState(new HttpUtil.HttpUtilCallBack<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                if (result) {
                    animation.setDuration(1000);
                    animation.setFillAfter(false);
                    String currentDate = XDateUtils.getCurrentDate("yyyy-MM-dd");
                    textViewSkip.setOnClickListener(v -> {
                        if (!animation.hasStarted()) {
                            frameLayoutWelcome.startAnimation(animation);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    frameLayoutWelcome.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }
                        cnt = 0;
                    });
                    Glide.with(MainActivity.this)
                            .load(String.format("%s?path=/opt/dayDP/&fileName=%s.jpg", GlobalUtil.Urls.File.DOWNLOAD, currentDate))
                            .error(R.drawable.xloading_error)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                    myHandler.sendEmptyMessage(0);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    imageView.setImageDrawable(resource);
                                    boolean welcome = setting.getBoolean("welcome", true);
                                    if (welcome) {
                                        textViewSkip.setText("跳过");
                                        myHandler.sendEmptyMessage(0);
                                    } else {
                                        frameLayoutWelcome.setVisibility(View.GONE);
                                        cnt = 0;
                                    }
                                    return true;
                                }
                            }).into(imageView);
                } else {
                    frameLayoutWelcome.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                frameLayoutWelcome.setVisibility(View.GONE);
            }
        });
        initView();
        initData();
        XPermission.requestPermissions(this, 111, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,}, new XPermission.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示信息")
                        .setMessage("权限说明：\n读写设备上的照片及文件用于展示报修单的故障图片和启动图片、选择照片上传到服务器作为当日的启动图片\n直接拨打电话用于省去复制电话号码粘贴到拨号界面的过程，最后拨号还是需要手动触发的\n\n注意：非MIUI如果想收到新的报修单通知则必须都授权这两个权限。\n如若需要，请单击【确定】按钮前往设置中心进行权限授权。")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                            MainActivity.this.startActivity(intent);
                        }).show();
            }
        });
        //耗时操作，需要在子线程中完成操作后通知主线程实现UI更新
        new Thread(() ->
                runOnUiThread(this::initListener)).start();
    }

    private void initData() {

        //初始化push推送服务
        if (shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }

        mainViewModel.getCurrentState().setValue(0);
        iNewTaskListener = new NewTaskFragment.INewTaskListener() {
            @Override
            public void onStartSelect(int count, int total) {
                floatingActionButtonDelete.show();
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, total));
            }

            @Override
            public void onSelect(int count, int total) {
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, total));
            }

            @Override
            public void onCancelSelect() {
                floatingActionButtonDelete.hide();
                getSupportActionBar().setTitle("JNUETC");
            }
        };
        iSecondListener = new SecondFragment.ISecondListener() {
            @Override
            public void onStartSelect(int count, int total) {
                floatingActionButtonDelete.show();
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, total));
            }

            @Override
            public void onSelect(int count, int total) {
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, total));
            }

            @Override
            public void onCancelSelect() {
                floatingActionButtonDelete.hide();
                getSupportActionBar().setTitle("JNUETC");
            }
        };
        iMyselfFragmentListener = () -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("学号或密码变更，请重新登录")
                    .setCancelable(false)
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        finish();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    })
                    .show();
        };
        newTaskFragment = new NewTaskFragment(iNewTaskListener);
        secondFragment = new SecondFragment(iSecondListener);
        myselfFragment = new MyselfFragment(iMyselfFragmentListener);
        fragments.add(newTaskFragment);
        fragments.add(secondFragment);
        fragments.add(myselfFragment);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments, null);
//        scrollViewPager.setScroll(false);
        scrollViewPager.setAdapter(pagerAdapter);
        scrollViewPager.setOffscreenPageLimit(3);

        mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
            @Override
            public void onResponse(User result) {
            }

            @Override
            public void onFailure(String error) {
            }
        });
    }

    private void initListener() {
        textViewSelectAllNorth.setOnClickListener(this);
        textViewSelectAllSouth.setOnClickListener(this);
        textViewSubTitle.setOnClickListener(this);
        textViewTitleHost.setOnClickListener(this);
        floatingActionButtonDelete.setOnClickListener(this);

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> mainViewModel.getTimeOrder().setValue(i == R.id.radioButton_asc));

        mainViewModel.getCurrentState().observe(this, integer -> {
            if (integer != 3) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                navigationView.getHeaderView(0).findViewById(R.id.linearLayout_main_header).setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE);
                mainViewModel.loadAllSettings(integer);
                updateDrawer();
            } else {
                if (setting.getBoolean("lock", true)) {
                    navigationView.getHeaderView(0).findViewById(R.id.linearLayout_main_header).setVisibility(View.GONE);
                    frameLayout.setVisibility(View.VISIBLE);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    updateDrawer();
                } else {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        });

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {

            @Override
            public void onDrawerClosed(View drawerView) {
                mainViewModel.getDrawerOpen().setValue(false);
                mainViewModel.saveAllSettings();
                mainViewModel.queryDataListBySetting(null);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mainViewModel.getDrawerOpen().setValue(true);
                updateDrawer();
            }
        });

        scrollViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_new_task);
                    mainViewModel.getCurrentState().setValue(0);
                } else if (position == 1) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_processing);
                    mainViewModel.getCurrentState().setValue(secondFragment.getCurrentPosition() + 1);
                } else if (position == 2) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_my_self);
                    mainViewModel.getCurrentState().setValue(3);
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            boolean reselect = menuItem.isChecked();
            switch (menuItem.getItemId()) {
                case R.id.navigation_new_task:
                    if (reselect) {
                        newTaskFragment.autoRefresh();
                    } else {
                        scrollViewPager.setCurrentItem(0, true);
                    }
                    break;
                case R.id.navigation_processing:
                    if (reselect) {
                        secondFragment.autoRefresh();
                    } else {
                        scrollViewPager.setCurrentItem(1, true);
                    }
                    break;
                case R.id.navigation_my_self:
                    if (reselect) {
                        myselfFragment.autoRefresh();
                    } else {
                        scrollViewPager.setCurrentItem(2, true);
                    }
                    break;
                default:
                    return false;
            }
            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_DATA_CHANGE && resultCode == Activity.RESULT_OK && data != null) {
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
                    App.getDaoSession().getDataDao().update(data1);
                    mainViewModel.queryDataListBySetting((int) data1.getState());
                    mainViewModel.queryDataListAboutMyself(1);
                    mainViewModel.queryDataListAboutMyself(2);
                    dataList.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                    dataList.add(position, data1);
                    taskAdapter.notifyItemInserted(position);
                    taskAdapter.notifyItemRangeChanged(position, taskAdapter.getItemCount() - position);
                    onSearchChange(inputString);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        XPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initView() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setPadding(0, getStatusHeight(this), 0, 0);
        XStatusBar.setTranslucentForDrawerLayout(this, drawer, 0);
        taskAdapter = new TaskAdapter(MainViewModel.user.haveDeleteAccess(), dataList, this, new TaskAdapter.ITaskListener() {
            @Override
            public void onItemClick(int position, Data data) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("id", data.getId());
                intent.putExtra("position", position);
                startActivityForResult(intent, REQUEST_DATA_CHANGE);
            }

            @Override
            public void onStartSelect(int count) {
                floatingActionButtonDelete.show();
            }

            @Override
            public void onSelect(int count) {
            }

            @Override
            public void onConfirmSelect(SparseBooleanArray sparseBooleanArray) {
                deleteSelect(sparseBooleanArray);
            }

            @Override
            public void onCancelSelect() {
                floatingActionButtonDelete.hide();
            }

            @Override
            public void onConfirmClick(int position, Data data) {
                XLoadingDialog.with(MainActivity.this).setMessage("请求处理中，请稍后").setCanceled(false).show();
                MainViewModel.queryById(String.valueOf(data.getId()), new HttpUtil.HttpUtilCallBack<Data>() {
                    @Override
                    public void onResponse(Data result) {
                        XLoadingDialog.with(MainActivity.this).cancel();
                        if (result.getState() == 0) {
                            String dataBackUpString = result.toString();
                            String[] items = {"接单成功后自动复制QQ号并且打开QQ"};
                            boolean[] booleans = {true};
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(data.getLocal() + " - " + data.getId())
                                    .setCancelable(false)
                                    .setMultiChoiceItems(items, booleans, (dialogInterface, i, b) -> {
                                    })
                                    .setNegativeButton("取消", (dialogInterface, i) -> XLoadingDialog.with(MainActivity.this).cancel())
                                    .setPositiveButton("接单", (dialogInterface, i) -> {
                                        XLoadingDialog.with(MainActivity.this).setMessage("请求处理中，请稍后").setCanceled(false).show();
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

                                                        dataList.remove(position);
                                                        taskAdapter.deleteSelect(position);
                                                        taskAdapter.notifyDataSetChanged();

                                                        if (booleans[0]) {
                                                            Data.openQq(result.getQq());
                                                        }

                                                        XLoadingDialog.with(MainActivity.this).cancel();
                                                    }

                                                    @Override
                                                    public void onFailure(String error) {
                                                        XToast.success("接单失败\n" + error);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                XLoadingDialog.with(MainActivity.this).cancel();
                                                XToast.error("接单失败");
                                            }
                                        });
                                    })
                                    .create()
                                    .show();

                        } else {
                            XLoadingDialog.with(MainActivity.this).cancel();
                            XToast.warning("唉呀，有人抢先了...\n该报修单已被 " + result.getRepairer() + " 处理");
                            dataList.remove(position);
                            taskAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        XToast.error("请求失败，请重试");
                        XLoadingDialog.with(MainActivity.this).cancel();
                    }
                });
            }
        });
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);
        textViewSelectAllNorth = navigationView.getHeaderView(0).findViewById(R.id.textView_main_select_all_north);
        textViewSelectAllSouth = navigationView.getHeaderView(0).findViewById(R.id.textView_main_select_all_south);
        TimerFragment timerFragment = new TimerFragment("与你相识的第", MainViewModel.user.getRegDate());
        if (getSupportFragmentManager().findFragmentByTag(TimerFragment.TAG) == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main_header, timerFragment, TimerFragment.TAG).commitAllowingStateLoss();
        }
        RecyclerView recyclerViewNorth = navigationView.getHeaderView(0).findViewById(R.id.recyclerView_main_north);
        RecyclerView recyclerViewSouth = navigationView.getHeaderView(0).findViewById(R.id.recyclerView_main_south);

        radioGroup = navigationView.getHeaderView(0).findViewById(R.id.radioGroup_main);
        textViewSubTitle = navigationView.getHeaderView(0).findViewById(R.id.textView_main_sub_title);
        textViewTitleHost = navigationView.getHeaderView(0).findViewById(R.id.textView_main_host);
        if (!setting.getBoolean("show_host", true)) {
            textViewTitleHost.setVisibility(View.GONE);
        }
        checkableAdapterNorth = new CheckableAdapter(GlobalUtil.TITLES_N, mainViewModel.getSelectedLocalsN().getValue(), () -> {
            textViewSelectAllNorth.setText(mainViewModel.isSelectAll(mainViewModel.getSelectedLocalsN().getValue()) ? "取消全选" : "全选");
        });
        checkableAdapterSouth = new CheckableAdapter(GlobalUtil.TITLES_S, mainViewModel.getSelectedLocalsS().getValue(), () -> {
            textViewSelectAllSouth.setText(mainViewModel.isSelectAll(mainViewModel.getSelectedLocalsS().getValue()) ? "取消全选" : "全选");
        });

        recyclerViewNorth.setLayoutManager(new WrapContentLinearLayoutManager(this));
        recyclerViewNorth.setAdapter(checkableAdapterNorth);

        recyclerViewSouth.setLayoutManager(new WrapContentLinearLayoutManager(this));
        recyclerViewSouth.setAdapter(checkableAdapterSouth);

        App.initToolbar(toolbar, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_main, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) XFrame.getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_main_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.menu_main_logout:
                SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.LoginActivity.FILE_NAME)
                        .edit()
                        .putString(GlobalUtil.Keys.LoginActivity.USER_JSON_STRING, "needLogin")
                        .putBoolean(GlobalUtil.Keys.LoginActivity.AUTO_LOGIN, true)
                        .apply();
                finish();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra("logout", true);
                MainViewModel.user = null;
                startActivity(intent);
                return true;
            case R.id.menu_main_modify_password:
                AlertDialog dialog = new AlertDialog.Builder(this).create();
                final String[] sno = new String[1];
                final String[] password = new String[1];
                final String[] password1 = new String[1];
                final String[] password2 = new String[1];

                password1[0] = "";
                password2[0] = "";

                LinearLayout linearLayout = new LinearLayout(this);
                MaterialEditText materialEditTextSno = new MaterialEditText(this);
                MaterialEditText materialEditTextOldPassword = new MaterialEditText(this);
                MaterialEditText materialEditTextNewPassword1 = new MaterialEditText(this);
                MaterialEditText materialEditTextNewPassword2 = new MaterialEditText(this);

                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setPadding(50, 20, 50, 20);


                materialEditTextSno.setHint("学号");
                materialEditTextSno.setText(String.valueOf(MainViewModel.user.getSno()));
                materialEditTextSno.setFloatingLabelText("学号");
                materialEditTextSno.setFloatingLabel(MaterialEditText.FLOATING_LABEL_HIGHLIGHT);
                materialEditTextSno.setInputType(InputType.TYPE_CLASS_NUMBER);
                materialEditTextSno.setMinCharacters(10);
                materialEditTextSno.setMaxCharacters(10);
                materialEditTextSno.setEnabled(false);

                materialEditTextOldPassword.setHint("原密码");
                materialEditTextOldPassword.setShowClearButton(true);
                materialEditTextOldPassword.setFloatingLabelText("原密码");
                materialEditTextOldPassword.setFloatingLabel(MaterialEditText.FLOATING_LABEL_HIGHLIGHT);
                materialEditTextOldPassword.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD | InputType.TYPE_CLASS_TEXT);
                materialEditTextOldPassword.setMinCharacters(7);
                materialEditTextOldPassword.setMaxCharacters(18);


                materialEditTextNewPassword1.setHint("请输入新密码");
                materialEditTextNewPassword1.setShowClearButton(true);
                materialEditTextNewPassword1.setFloatingLabelText("请输入新密码");
                materialEditTextNewPassword1.setFloatingLabel(MaterialEditText.FLOATING_LABEL_HIGHLIGHT);
                materialEditTextNewPassword1.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD | InputType.TYPE_CLASS_TEXT);
                materialEditTextNewPassword1.setMinCharacters(7);
                materialEditTextNewPassword1.setMaxCharacters(18);
                materialEditTextNewPassword1.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        password2[0] = materialEditTextNewPassword2.getText().toString();
                        if (!"".equals(password1[0])) {
                            if (!password1[0].startsWith(charSequence.toString())) {
                                materialEditTextNewPassword1.setError("密码不一致");
                            } else {
                                materialEditTextNewPassword1.setError(null);
                                materialEditTextNewPassword2.setError(null);
                            }
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        password2[0] = materialEditTextNewPassword2.getText().toString();
                        if (!"".equals(password2[0])) {
                            if (!password2[0].startsWith(editable.toString())) {
                                materialEditTextNewPassword1.setError("密码不一致");
                            } else {
                                materialEditTextNewPassword1.setError(null);
                                materialEditTextNewPassword2.setError(null);
                            }
                        }
                    }
                });


                materialEditTextNewPassword2.setHint("请再输一遍新密码");
                materialEditTextNewPassword2.setShowClearButton(true);
                materialEditTextNewPassword2.setFloatingLabelText("请再输一遍新密码");
                materialEditTextNewPassword2.setFloatingLabel(MaterialEditText.FLOATING_LABEL_HIGHLIGHT);
                materialEditTextNewPassword2.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD | InputType.TYPE_CLASS_TEXT);

                materialEditTextNewPassword2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        password1[0] = materialEditTextNewPassword1.getText().toString();
                        if (!"".equals(password1[0])) {
                            if (!password1[0].startsWith(charSequence.toString())) {
                                materialEditTextNewPassword2.setError("密码不一致");
                            } else {
                                materialEditTextNewPassword1.setError(null);
                                materialEditTextNewPassword2.setError(null);
                            }
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        password1[0] = materialEditTextNewPassword1.getText().toString();
                        if (!"".equals(password1[0])) {
                            if (!password1[0].startsWith(editable.toString())) {
                                materialEditTextNewPassword2.setError("密码不一致");
                            } else {
                                materialEditTextNewPassword1.setError(null);
                                materialEditTextNewPassword2.setError(null);
                            }
                        }
                    }
                });

                Button button = new Button(this);
                button.setText("修改");
                button.setOnClickListener(view -> {
                    sno[0] = materialEditTextSno.getText().toString();
                    password[0] = materialEditTextOldPassword.getText().toString();
                    password1[0] = materialEditTextNewPassword1.getText().toString();
                    password2[0] = materialEditTextNewPassword2.getText().toString();
                    if (sno[0].length() == 10
                            && password[0].length() >= 7 && password[0].length() <= 18
                            && password1[0].length() >= 7 && password1[0].length() <= 18
                            && password2[0].equals(password1[0])) {
                        if (password1[0].equals(password[0])) {
                            XToast.error("新密码不能和旧密码相同...");
                        } else if (!password[0].equals(MainViewModel.user.getPassword())) {
                            XToast.error("原密码有误");
                        } else {
                            XLoadingDialog.with(this).setMessage("数据请求中,请稍后").setCanceled(false).show();
                            User userToModify = GsonUtil.getInstance().fromJson(MainViewModel.user.toString(), User.class);
                            userToModify.setPassword(password1[0]);
                            Map<String, Object> params = new HashMap<>(2);
                            params.put("userJson", MainViewModel.user.toString());
                            params.put("userToModifyJson", userToModify.toString());
                            XHttp.obtain().post(GlobalUtil.Urls.User.MODIFY, params, new HttpCallBack<JsonObject>() {
                                @Override
                                public void onSuccess(JsonObject jsonObject) {
                                    XLoadingDialog.with(MainActivity.this).cancel();
                                    int error = jsonObject.get("error").getAsInt();
                                    if (error == 1) {
                                        XToast.success("密码修改成功,请重新登录");
                                        SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.LoginActivity.FILE_NAME)
                                                .edit()
                                                .putString(GlobalUtil.Keys.LoginActivity.USER_JSON_STRING, "needLogin")
                                                .putBoolean(GlobalUtil.Keys.LoginActivity.AUTO_LOGIN, false)
                                                .apply();
                                        dialog.dismiss();
                                        finish();
                                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    } else {
                                        XToast.error(jsonObject.get("msg").getAsString());
                                    }
                                }

                                @Override
                                public void onFailed(String error) {
                                    XLoadingDialog.with(MainActivity.this).cancel();
                                    XToast.error("密码修改失败");
                                }
                            });
                        }
                    } else {
                        XToast.error("请检查输入");
                    }
                });

                linearLayout.addView(materialEditTextSno);
                linearLayout.addView(materialEditTextOldPassword);
                linearLayout.addView(materialEditTextNewPassword1);
                linearLayout.addView(materialEditTextNewPassword2);
                linearLayout.addView(button);

                dialog.setView(linearLayout);
                dialog.show();

                return true;
            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (recyclerView.getVisibility() == View.VISIBLE) {
            if (taskAdapter.isSelectMode()) {
                taskAdapter.cancelSelect();
            } else {
                recyclerView.setVisibility(View.GONE);
                //清空输入，并隐藏输入框
                searchView.setQuery("", false);
                searchView.setIconified(true);
            }
        } else {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (newTaskFragment.isSelectMode()) {
                    newTaskFragment.cancelSelect();
                } else if (secondFragment.isSelectMode()) {
                    secondFragment.cancelSelect();
                } else {
                    if (System.currentTimeMillis() - backTime < 2 * 1000) {
//                    Intent i = new Intent(Intent.ACTION_MAIN);
//                    i.addCategory(Intent.CATEGORY_HOME);
//                    startActivity(i);
                        finish();
                    } else {
                        backTime = System.currentTimeMillis();
                        XToast.info("再次点击退出");
                    }
                }
            }
        }
    }

    private void onSearchChange(String s) {
        List<Data> list;
        if (XEmptyUtils.isEmpty(s)) {
            recyclerView.setVisibility(View.GONE);
        } else {
            inputString = s;
            QueryBuilder<Data> dataQueryBuilder = App.getDaoSession().getDataDao().queryBuilder();
            recyclerView.setVisibility(View.VISIBLE);
            if ("有图".equals(s)) {
                list = dataQueryBuilder.where(
                        DataDao.Properties.Photo.notEq("")
                ).build().list();
            } else {
                String s1 = "%" + s + "%";
                try {
                    Integer idOrTelOrQq = Integer.valueOf(s);
                    list = dataQueryBuilder.whereOr(
                            DataDao.Properties.Id.eq(idOrTelOrQq),
                            DataDao.Properties.Id.like(s1),
                            DataDao.Properties.Tel.eq(idOrTelOrQq),
                            DataDao.Properties.Tel.like(s1),
                            DataDao.Properties.Qq.eq(idOrTelOrQq),
                            DataDao.Properties.Qq.like(s1),
                            DataDao.Properties.Model.like(s1),
                            DataDao.Properties.Message.like(s1),
                            DataDao.Properties.RepairMessage.like(s1)
                    ).build().list();
                } catch (NumberFormatException e) {
                    list = dataQueryBuilder.whereOr(
                            DataDao.Properties.Name.like(s1),
                            DataDao.Properties.Local.like(s1),
                            DataDao.Properties.College.like(s1),
                            DataDao.Properties.Grade.like(s1),
                            DataDao.Properties.Model.like(s1),
                            DataDao.Properties.Message.like(s1),
                            DataDao.Properties.Repairer.like(s1),
                            DataDao.Properties.Mark.like(s1),
                            DataDao.Properties.Service.like(s1),
                            DataDao.Properties.RepairMessage.like(s1)
                    ).build().list();
                }
            }
            taskAdapter.notifyItemRangeRemoved(0, dataList.size());
            dataList.clear();
            dataList.addAll(list);
            taskAdapter.notifyDataSetChanged();
        }
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getApplicationInfo().processName;
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取状态栏高度
     *  
     *
     * @param activity
     * @return
     */
    public int getStatusHeight(AppCompatActivity activity) {
        int statusHeight;
        Rect rect = new Rect();
        activity.getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(rect);
        statusHeight = rect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object object = localClass.newInstance();
                int height = Integer.parseInt(localClass
                        .getField("status_bar_height").get(object)
                        .toString());
                statusHeight = activity.getResources()
                        .getDimensionPixelSize(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    private void updateDrawer() {
        textViewTitleHost.setVisibility(setting.getBoolean("show_host", true) ? View.VISIBLE : View.GONE);
        if (checkableAdapterNorth != null && checkableAdapterSouth != null) {
            checkableAdapterNorth.notifyItemRangeChanged(0, 8, 0);
            checkableAdapterSouth.notifyItemRangeChanged(0, 8, 0);
        }
        Boolean b = mainViewModel.getTimeOrder().getValue();
        radioGroup.check(b != null && b ? R.id.radioButton_asc : R.id.radioButton_desc);
        textViewSelectAllNorth.setText(mainViewModel.isSelectAll(mainViewModel.getSelectedLocalsN().getValue()) ? "取消全选" : "全选");
        textViewSelectAllSouth.setText(mainViewModel.isSelectAll(mainViewModel.getSelectedLocalsS().getValue()) ? "取消全选" : "全选");
        LinearLayout linearLayoutNorth = navigationView.getHeaderView(0).findViewById(R.id.linearLayout_main_north);
        LinearLayout linearLayoutSouth = navigationView.getHeaderView(0).findViewById(R.id.linearLayout_main_south);
        TextView textView = navigationView.getHeaderView(0).findViewById(R.id.textView_main_state);
        switch (mainViewModel.getCurrentState().getValue()) {
            case 0:
                textView.setText("未处理");
                break;
            case 1:
                textView.setText("处理中");
                break;
            case 2:
                textView.setText("已维修");
                break;
            default:
                String showText = setting.getString("show_text", "永远相信美好的事情即将发生");
                textView.setText(showText);
        }
        if (mainViewModel.getCurrentState().getValue() == 0) {
            if (!MainViewModel.user.haveWholeSchoolAccess()) {
                if (MainViewModel.user.getWhichGroup() == 0) {
                    linearLayoutSouth.setVisibility(View.GONE);
                    linearLayoutNorth.setVisibility(View.VISIBLE);
                } else {
                    linearLayoutNorth.setVisibility(View.GONE);
                    linearLayoutSouth.setVisibility(View.VISIBLE);
                }
            } else {
                linearLayoutNorth.setVisibility(View.VISIBLE);
                linearLayoutSouth.setVisibility(View.VISIBLE);
            }
        } else {
            linearLayoutNorth.setVisibility(View.VISIBLE);
            linearLayoutSouth.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textView_main_select_all_north:
                if (mainViewModel.isSelectAll(mainViewModel.getSelectedLocalsN().getValue())) {
                    mainViewModel.selectOrCancelAll(false, mainViewModel.getSelectedLocalsN().getValue());
                } else {
                    mainViewModel.selectOrCancelAll(true, mainViewModel.getSelectedLocalsN().getValue());
                }
                updateDrawer();
                break;
            case R.id.textView_main_select_all_south:
                if (mainViewModel.isSelectAll(mainViewModel.getSelectedLocalsS().getValue())) {
                    mainViewModel.selectOrCancelAll(false, mainViewModel.getSelectedLocalsS().getValue());
                } else {
                    mainViewModel.selectOrCancelAll(true, mainViewModel.getSelectedLocalsS().getValue());
                }
                updateDrawer();
                break;
            case R.id.textView_main_sub_title:
                String url = "http://qm.qq.com/cgi-bin/qm/qr?k=Jc1GFeE-TU3kSawFNUwgVJ1Vmquuu5f1&group_code=459121889";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                break;
            case R.id.fab_main_delete:
                if (recyclerView.getVisibility() == View.VISIBLE && taskAdapter.isSelectMode()) {
                    if (taskAdapter.selectNone()) {
                        XToast.info("请至少选择一项");
                    } else {
                        taskAdapter.finishSelect();
                    }
                } else {

                    if (mainViewModel.getCurrentState().getValue() == 0) {
                        if (newTaskFragment.selectNone()) {
                            XToast.info("请至少选择一项");
                        } else {
                            newTaskFragment.onConfirmSelect();
                        }
                    } else {
                        if (secondFragment.selectNone()) {
                            XToast.info("请至少选择一项");
                        } else {
                            secondFragment.onConfirmSelect();
                        }
                    }
                }
                break;
            case R.id.textView_main_host:
                MainViewModel.copyToClipboard(MainActivity.this, "https://www.jiangnan-dzjsb.club:444");
                XToast.success("https://www.jiangnan-dzjsb.club:444已复制到剪切板");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.jiangnan-dzjsb.club:444")));
                break;
            default:
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onSearchChange(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        onSearchChange(newText);
        return true;
    }

    private void deleteSelect(SparseBooleanArray sparseBooleanArray) {
        mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
            @Override
            public void onResponse(User result) {
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

                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                        dialog.setTitle("注意");
                        dialog.setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作。确认删除以下报修单？\n" + builder.toString());
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "取消", (dialogInterface, i1) -> {

                        });

                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "删除", (dialogInterface, i1) -> {
                            XLoadingDialog.with(MainActivity.this).setCanceled(false).setMessage("请求处理中,请稍后").show();

                            mainViewModel.deleteMany(ids, new HttpUtil.HttpUtilCallBack<Boolean>() {
                                @Override
                                public void onResponse(Boolean result1) {
                                    XLoadingDialog.with(MainActivity.this).cancel();
                                    if (result1) {
                                        mainViewModel.queryDataListBySetting(0);
                                        mainViewModel.queryDataListBySetting(1);
                                        mainViewModel.queryDataListBySetting(2);
                                        mainViewModel.queryDataListAboutMyself(1);
                                        mainViewModel.queryDataListAboutMyself(2);

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
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    XLoadingDialog.with(MainActivity.this).cancel();
                                    XToast.error("删除失败");
                                    dialog.cancel();
                                }
                            });
                        });

                        dialog.show();
                    }
                } else {
                    XToast.info("您已不是管理员");
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
    protected void onDestroy() {
        super.onDestroy();
        XActivityStack.getInstance().finishActivity();
    }
}
