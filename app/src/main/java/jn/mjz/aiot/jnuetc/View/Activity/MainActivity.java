package jn.mjz.aiot.jnuetc.View.Activity;

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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.utils.XDateUtils;
import com.youth.xframe.utils.XEmptyUtils;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.Application.MyApplication;
import jn.mjz.aiot.jnuetc.Greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.Greendao.Entity.User;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.Util.SharedPreferencesUtil;
import jn.mjz.aiot.jnuetc.Util.UpdateUtil;
import jn.mjz.aiot.jnuetc.View.Adapter.RecyclerView.CheckableAdapter;
import jn.mjz.aiot.jnuetc.View.Adapter.RecyclerView.TaskAdapter;
import jn.mjz.aiot.jnuetc.View.Adapter.ViewPager.MainPagerAdapter;
import jn.mjz.aiot.jnuetc.View.CustomView.ScrollViewPager;
import jn.mjz.aiot.jnuetc.View.Fragment.MyselfFragment;
import jn.mjz.aiot.jnuetc.View.Fragment.NewTaskFragment;
import jn.mjz.aiot.jnuetc.View.Fragment.SecondFragment;
import jn.mjz.aiot.jnuetc.View.Fragment.TimerFragment;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import me.sugarkawhi.bottomnavigationbar.BottomNavigationBar;
import me.sugarkawhi.bottomnavigationbar.BottomNavigationEntity;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar_main)
    Toolbar toolbar;
    @BindView(R.id.scrollViewPager_main)
    ScrollViewPager scrollViewPager;
    @BindView(R.id.navigationBar_main)
    BottomNavigationBar bottomNavigationBar;
    TextView textViewSelectAllNorth;
    TextView textViewSelectAllSouth;
    TextView textViewSubTitle;
    @BindView(R.id.recyclerView_main_search)
    RecyclerView recyclerView;
    @BindView(R.id.button_main_skip)
    Button textViewSkip;
    private List<Data> dataList = new ArrayList<>();
    private TaskAdapter taskAdapter;

    @BindView(R.id.fab_main_delete)
    FloatingActionButton floatingActionButtonDelete;
    @BindView(R.id.frameLayout_main_header)
    FrameLayout frameLayout;
    @BindView(R.id.relativeLayout_main_welcome)
    RelativeLayout relativeLayout;
    @BindView(R.id.imageView_main_welcome)
    ImageView imageView;

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
    private MainPagerAdapter pagerAdapter;

    private List<BottomNavigationEntity> entities = new ArrayList<>();

    private CheckableAdapter checkableAdapterNorth;
    private CheckableAdapter checkableAdapterSouth;
    private SharedPreferences setting = SharedPreferencesUtil.getSettingPreferences();

    int cnt = Integer.parseInt(setting.getString("show_time", "5"));
    MyHandler myHandler = new MyHandler();

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                myHandler.postDelayed(() -> {
                    textViewSkip.setText(String.format(Locale.getDefault(), "跳过%d", cnt));
                    if (--cnt > 0) {
                        myHandler.sendEmptyMessage(0);
                    } else {
                        if (!animation.hasStarted()) {
                            relativeLayout.startAnimation(animation);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    relativeLayout.setVisibility(View.GONE);
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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        animation.setDuration(1000);
        animation.setFillAfter(false);
        String currentDate = XDateUtils.getCurrentDate("yyyy-MM-dd");
        // TODO: 2019/10/20
        Glide.with(this).load(String.format("%s?url=/opt/dayDP&fileName=%s.jpg", GlobalUtil.URLS.FILE.DOWNLOAD, currentDate)).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                myHandler.sendEmptyMessage(0);
                return true;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                imageView.setImageDrawable(resource);
                boolean welcome = setting.getBoolean("welcome", true);
                if (welcome) {
                    textViewSkip.setText("跳过");
                    myHandler.sendEmptyMessage(0);
                    textViewSkip.setOnClickListener(v -> {
                        if (!animation.hasStarted()) {
                            relativeLayout.startAnimation(animation);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    relativeLayout.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }
                        cnt = 0;
                    });
                } else {
                    relativeLayout.setVisibility(View.GONE);
                    cnt = 0;
                }
                return true;
            }
        }).into(imageView);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        InitView();
        InitData();
        new Thread(() -> //耗时操作，需要在子线程中完成操作后通知主线程实现UI更新
                runOnUiThread(this::InitListener)).start();
    }

    private void InitData() {
        UpdateUtil.checkForUpdate(true, this, drawer);

        //初始化push推送服务
        if (shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }

        mainViewModel.getCurrentState().setValue(0);
        byte b = 0;
        while (b < GlobalUtil.TITLES.ACTIVITY.MAIN_ACTIVITY.length) {
            entities.add(new BottomNavigationEntity(GlobalUtil.TITLES.ACTIVITY.MAIN_ACTIVITY[b], GlobalUtil.unSelectedIconIds[b], GlobalUtil.selectedIconIds[b]));
            b++;
        }
        iNewTaskListener = new NewTaskFragment.INewTaskListener() {
            @Override
            public void OnStartSelect(int count, int total) {
                floatingActionButtonDelete.show();
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, total));
            }

            @Override
            public void OnSelect(int count, int total) {
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, total));
            }

            @Override
            public void OnCancelSelect() {
                floatingActionButtonDelete.hide();
                getSupportActionBar().setTitle("JNUETC");
            }
        };
        iSecondListener = new SecondFragment.ISecondListener() {
            @Override
            public void OnStartSelect(int count, int total) {
                floatingActionButtonDelete.show();
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, total));
            }

            @Override
            public void OnSelect(int count, int total) {
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "已选择%d/%d", count, total));
            }

            @Override
            public void OnCancelSelect() {
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
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), fragments);
//        scrollViewPager.setScroll(false);
        scrollViewPager.setAdapter(pagerAdapter);
        scrollViewPager.setOffscreenPageLimit(3);

        bottomNavigationBar.setEntities(entities);
        bottomNavigationBar.setCurrentPosition(0);

        mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
            @Override
            public void onResponse(Response response, User result) {
            }

            @Override
            public void onFailure(IOException e) {
            }
        });
    }

    private void InitListener() {
        textViewSelectAllNorth.setOnClickListener(this);
        textViewSelectAllSouth.setOnClickListener(this);
        textViewSubTitle.setOnClickListener(this);
        floatingActionButtonDelete.setOnClickListener(this);

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> mainViewModel.getTimeOrder().setValue(i == R.id.radioButton_asc));

        mainViewModel.getCurrentState().observe(this, integer -> {
            if (integer != 3) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//                navigationView.getHeaderView(0).findViewById(R.id.linearLayout_main_header).setVisibility(View.VISIBLE);
//                frameLayout.setVisibility(View.GONE);
                mainViewModel.loadAllSettings(integer);
                updateDrawer();
            } else {
//                navigationView.getHeaderView(0).findViewById(R.id.linearLayout_main_header).setVisibility(View.GONE);
//                frameLayout.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                if (position == 0) {
                    mainViewModel.getCurrentState().setValue(0);
                } else if (position == 1) {
                    mainViewModel.getCurrentState().setValue(secondFragment.getCurrentPosition() + 1);
                } else if (position == 2) {
                    mainViewModel.getCurrentState().setValue(3);
                }

                bottomNavigationBar.setCurrentPosition(position);
            }
        });

        bottomNavigationBar.setBnbItemSelectListener(position -> {
            scrollViewPager.setCurrentItem(position, true);
        });
        bottomNavigationBar.setBnbItemDoubleClickListener(position -> {
            if (position == 0) {
                newTaskFragment.autoRefresh();
            } else if (position == 1) {
                secondFragment.autoRefresh();
            } else {
                myselfFragment.autoRefresh();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //进入报修单详情
        if (requestCode == 4 && resultCode == 0 && data != null) {
            int position = data.getIntExtra("position", -1);
            Data data1 = GsonUtil.getInstance().fromJson(data.getStringExtra("data"), Data.class);
            if (position != -1 && data1 != null) {
                //转让成功
                boolean makeover = data.getBooleanExtra("makeover", false);
                //接单成功
                boolean order = data.getBooleanExtra("order", false);
                //反馈成功
                boolean feedback = data.getBooleanExtra("feedback", false);
                if (makeover || order || feedback) {
                    MyApplication.getDaoSession().getDataDao().update(data1);
                    mainViewModel.queryDataListBySetting((int) data1.getState());
                    mainViewModel.queryDataListAboutMyself(1);
                    mainViewModel.queryDataListAboutMyself(2);
                    dataList.remove(position);
                    taskAdapter.notifyDataSetChanged();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void InitView() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setPadding(0, getStatusHeight(this), 0, 0);
        XStatusBar.setTranslucentForDrawerLayout(this, drawer, 0);
        taskAdapter = new TaskAdapter(MainViewModel.user.getRoot() != 0 && MainViewModel.user.getRoot() != 1, dataList, this, new TaskAdapter.ITaskListener() {
            @Override
            public void OnItemClick(int position, Data data) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("data", data.toString());
                intent.putExtra("position", position);
                startActivityForResult(intent, 4);
            }

            @Override
            public void OnStartSelect(int count) {
                floatingActionButtonDelete.show();
            }

            @Override
            public void OnSelect(int count) {
            }

            @Override
            public void OnConfirmSelect(SparseBooleanArray sparseBooleanArray) {
                deleteSelect(sparseBooleanArray);
            }

            @Override
            public void OnCancelSelect() {
                floatingActionButtonDelete.hide();
            }

            @Override
            public void OnConfirmClick(int position, Data data) {
                XLoadingDialog.with(MainActivity.this).setMessage("请求处理中，请稍后").setCanceled(false).show();
                mainViewModel.queryById(String.valueOf(data.getId()), new HttpUtil.HttpUtilCallBack<Data>() {
                    @Override
                    public void onResponse(Response response, Data result) {
                        XLoadingDialog.with(MainActivity.this).cancel();
                        if (result.getState() == 0) {
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
                                        result.setRepairer(MainViewModel.user.getName());
                                        result.setState((short) 1);
                                        mainViewModel.feedback(result, new HttpUtil.HttpUtilCallBack<Data>() {
                                            @Override
                                            public void onResponse(Response response1, Data o) {
                                                XToast.success("接单成功");

                                                mainViewModel.dataDao.update(o);

                                                mainViewModel.queryDataListBySetting(0);
                                                mainViewModel.queryDataListAboutMyself(0);

                                                dataList.remove(position);
                                                taskAdapter.deleteSelect(position);
                                                taskAdapter.notifyDataSetChanged();

                                                if (booleans[0]) {
                                                    MainViewModel.copyToClipboard(MainActivity.this, result.getQq());
                                                    if (XAppUtils.isInstallApp("com.tencent.mobileqq")) {
                                                        XAppUtils.startApp("com.tencent.mobileqq");
                                                        XToast.success(String.format("QQ：%s已复制到剪切板", data.getQq()));
                                                    } else {
                                                        XToast.error("未安装手Q或安装的版本不支持");
                                                    }
                                                }

                                                XLoadingDialog.with(MainActivity.this).cancel();
                                            }

                                            @Override
                                            public void onFailure(IOException e) {
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
                    public void onFailure(IOException e) {
                        XToast.error("请求失败，请重试");
                        XLoadingDialog.with(MainActivity.this).cancel();
                    }
                });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);
        textViewSelectAllNorth = navigationView.getHeaderView(0).findViewById(R.id.textView_main_select_all_north);
        textViewSelectAllSouth = navigationView.getHeaderView(0).findViewById(R.id.textView_main_select_all_south);
        bottomNavigationBar.setEntities(entities);
        TimerFragment timerFragment = new TimerFragment("与你相识的第", MainViewModel.user.getRegDate());
        if (getSupportFragmentManager().findFragmentByTag(TimerFragment.TAG) == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main_header, timerFragment, TimerFragment.TAG).commitAllowingStateLoss();
        } else {
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main_header, timerFragment, TimerFragment.TAG).commitAllowingStateLoss();
        }
        RecyclerView recyclerViewNorth = navigationView.getHeaderView(0).findViewById(R.id.recyclerView_main_north);
        RecyclerView recyclerViewSouth = navigationView.getHeaderView(0).findViewById(R.id.recyclerView_main_south);

        radioGroup = navigationView.getHeaderView(0).findViewById(R.id.radioGroup_main);
        textViewSubTitle = navigationView.getHeaderView(0).findViewById(R.id.textView_main_sub_title);

        checkableAdapterNorth = new CheckableAdapter(GlobalUtil.titlesN, mainViewModel.getSelectedLocalsN().getValue(), () -> {
            textViewSelectAllNorth.setText(mainViewModel.isSelectAll(mainViewModel.getSelectedLocalsN().getValue()) ? "取消全选" : "全选");
        });
        checkableAdapterSouth = new CheckableAdapter(GlobalUtil.titlesS, mainViewModel.getSelectedLocalsS().getValue(), () -> {
            textViewSelectAllSouth.setText(mainViewModel.isSelectAll(mainViewModel.getSelectedLocalsS().getValue()) ? "取消全选" : "全选");
        });

        recyclerViewNorth.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNorth.setAdapter(checkableAdapterNorth);

        recyclerViewSouth.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSouth.setAdapter(checkableAdapterSouth);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_main, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) XFrame.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_main_search).getActionView();
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
                SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.LOGIN_ACTIVITY.FILE_NAME)
                        .edit()
                        .putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_JSON_STRING, "needLogin")
                        .putBoolean(GlobalUtil.KEYS.LOGIN_ACTIVITY.AUTO_LOGIN, false)
                        .apply();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
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


                materialEditTextSno.setHint("请输入学号");
                materialEditTextSno.setText(String.valueOf(MainViewModel.user.getSno()));
                materialEditTextSno.setShowClearButton(true);
                materialEditTextSno.setFloatingLabelText("请输入学号");
                materialEditTextSno.setFloatingLabel(MaterialEditText.FLOATING_LABEL_HIGHLIGHT);
                materialEditTextSno.setInputType(InputType.TYPE_CLASS_NUMBER);
                materialEditTextSno.setMinCharacters(10);
                materialEditTextSno.setMaxCharacters(10);


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
                        if (!password1[0].equals("")) {
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
                        if (!password2[0].equals("")) {
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
                        if (!password1[0].equals("")) {
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
                        if (!password1[0].equals("")) {
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
                        } else {
                            XLoadingDialog.with(this).setMessage("数据请求中,请稍后").setCanceled(false).show();
                            User user = new User();
                            user.setSno(sno[0]);
                            user.setPassword(password[0]);
                            user.setName(password1[0]);

                            Map<String, Object> params = new HashMap<>();
                            params.put("user", user);
                            HttpUtil.post.haveResponse(GlobalUtil.URLS.UPDATE.MODIFY_PASSWORD, params, new HttpUtil.HttpUtilCallBack<String>() {
                                @Override
                                public void onResponse(Response response, String result) {
                                    XLoadingDialog.with(MainActivity.this).dismiss();
                                    String s = response.header("state");
                                    if (s != null && s.equals("OK")) {

                                        XToast.success("密码修改成功,请重新登录");

                                        SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.LOGIN_ACTIVITY.FILE_NAME)
                                                .edit()
                                                .putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_JSON_STRING, "needLogin")
                                                .putBoolean(GlobalUtil.KEYS.LOGIN_ACTIVITY.AUTO_LOGIN, false)
                                                .apply();
                                        finish();
                                        startActivity(new Intent(MainActivity.this, LoginActivity.class));

                                        dialog.dismiss();
                                    } else {
                                        XToast.error("密码修改失败");
                                    }
                                }

                                @Override
                                public void onFailure(IOException e) {
                                    XLoadingDialog.with(MainActivity.this).dismiss();
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


    private void onSearchCommit(String s) {
        recyclerView.setVisibility(View.VISIBLE);
        List<Data> list = new ArrayList<>();
        if (XEmptyUtils.isEmpty(s)) {
            recyclerView.setVisibility(View.GONE);
        } else {
            QueryBuilder<Data> dataQueryBuilder = MyApplication.getDaoSession().getDataDao().queryBuilder();
            try {
                Integer idOrTelOrQq = Integer.valueOf(s);
                list = dataQueryBuilder.whereOr(
                        DataDao.Properties.Id.eq(idOrTelOrQq),
                        DataDao.Properties.Tel.eq(idOrTelOrQq),
                        DataDao.Properties.Qq.eq(idOrTelOrQq),
                        DataDao.Properties.Model.like("%s" + idOrTelOrQq + "%"),
                        DataDao.Properties.Message.like("%s" + idOrTelOrQq + "%"),
                        DataDao.Properties.RepairMessage.like("%s" + idOrTelOrQq + "%")
                ).build().list();
            } catch (NumberFormatException e) {
                String s1 = "%" + s + "%";
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
//        Log.e(TAG, "onSearchCommit: " + GsonUtil.getInstance().toJson(list));
        dataList.clear();
        dataList.addAll(list);
        taskAdapter.notifyDataSetChanged();
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
        if (MainViewModel.user.getRoot() == 0) {
            if (MainViewModel.user.getGroup() == 0) {
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
                            newTaskFragment.OnConfirmSelect();
                        }
                    } else {
                        if (secondFragment.selectNone()) {
                            XToast.info("请至少选择一项");
                        } else {
                            secondFragment.OnConfirmSelect();
                        }
                    }
                }
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onSearchCommit(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        onSearchCommit(newText);
        return true;
    }

    private void deleteSelect(SparseBooleanArray sparseBooleanArray) {
        mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
            @Override
            public void onResponse(Response response, User result) {
                if (result.getRoot() != 0 && result.getRoot() != 1) {
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

                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                        dialog.setTitle("注意");
                        dialog.setMessage("删除数据仅限无用的报修单，删除后无法还原，请谨慎操作。确认删除以下报修单？\n" + builder.toString());
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "取消", (dialogInterface, i1) -> {

                        });

                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "删除", (dialogInterface, i1) -> {
                            XLoadingDialog.with(MainActivity.this).setCanceled(false).setMessage("请求处理中,请稍后").show();

                            mainViewModel.deleteMany(ids, new HttpUtil.HttpUtilCallBack<Boolean>() {
                                @Override
                                public void onResponse(Response response1, Boolean result1) {

                                    mainViewModel.dataDao.deleteInTx(needToDelete);

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

                                    XLoadingDialog.with(MainActivity.this).cancel();
                                    dialog.dismiss();
                                    XToast.success("删除成功");
                                }

                                @Override
                                public void onFailure(IOException e) {
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
            public void onFailure(IOException e) {

            }
        });

    }
}
