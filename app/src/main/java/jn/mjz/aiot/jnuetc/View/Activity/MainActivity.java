package jn.mjz.aiot.jnuetc.View.Activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.Greendao.Entity.User;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.Util.SharedPreferencesUtil;
import jn.mjz.aiot.jnuetc.Util.UpdateUtil;
import jn.mjz.aiot.jnuetc.View.Adapter.RecyclerView.CheckableAdapter;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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

    @BindView(R.id.fab_main_delete)
    FloatingActionButton floatingActionButtonDelete;
    @BindView(R.id.frameLayout_main_header)
    FrameLayout frameLayout;

    /**
     * 小米推送用到的数据
     */
    public static final String APP_ID = "2882303761518054312";
    public static final String APP_KEY = "5181805484312";

    private long backTime = 0;

    public RadioGroup radioGroup;

    public static MainViewModel mainViewModel;

    private NewTaskFragment newTaskFragment;
    private SecondFragment secondFragment;
    private MyselfFragment myselfFragment;

    private List<Fragment> fragments = new ArrayList<>();
    private MainPagerAdapter pagerAdapter;

    private List<BottomNavigationEntity> entities = new ArrayList<>();

    private CheckableAdapter checkableAdapterNorth;
    private CheckableAdapter checkableAdapterSouth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalUtil.user = GsonUtil.getInstance().fromJson(getIntent().getStringExtra("user"), User.class);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        UpdateUtil.checkForUpdate(true, this, drawer);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        radioGroup = navigationView.getHeaderView(0).findViewById(R.id.radioGroup_main);
        RecyclerView recyclerViewNorth = navigationView.getHeaderView(0).findViewById(R.id.recyclerView_main_north);
        RecyclerView recyclerViewSouth = navigationView.getHeaderView(0).findViewById(R.id.recyclerView_main_south);
        textViewSubTitle = navigationView.getHeaderView(0).findViewById(R.id.textView_main_sub_title);
        textViewSelectAllNorth = navigationView.getHeaderView(0).findViewById(R.id.textView_main_select_all_north);
        textViewSelectAllSouth = navigationView.getHeaderView(0).findViewById(R.id.textView_main_select_all_south);

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

        InitView();
        InitData();
        InitListener();

        //初始化push推送服务
        if (shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }

        //打开Log
        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };
        Logger.setLogger(this, newLogger);

    }

    private void InitData() {

        mainViewModel.getCurrentState().setValue(0);

        byte b = 0;
        while (b < GlobalUtil.TITLES.ACTIVITY.MAIN_ACTIVITY.length) {
            entities.add(new BottomNavigationEntity(GlobalUtil.TITLES.ACTIVITY.MAIN_ACTIVITY[b], GlobalUtil.unSelectedIconIds[b], GlobalUtil.selectedIconIds[b]));
            b++;
        }
        newTaskFragment = new NewTaskFragment(new NewTaskFragment.INewTaskListener() {
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
        });
        secondFragment = new SecondFragment(new SecondFragment.ISecondListener() {
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
        });
        myselfFragment = new MyselfFragment();
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
                mainViewModel.saveAllSettings();
                mainViewModel.queryDataListBySetting(null);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
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

    private void InitView() {
        bottomNavigationBar.setEntities(entities);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setPadding(0, getStatusHeight(this), 0, 0);
        XStatusBar.setTranslucentForDrawerLayout(this, drawer, 0);
        TimerFragment timerFragment = new TimerFragment("与你相识的第", GlobalUtil.user.getRegDate());
        getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main_header, timerFragment).commitAllowingStateLoss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_update:
                UpdateUtil.checkForUpdate(false, this, drawer);
                break;
            case R.id.menu_main_logout:
                SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.LOGIN_ACTIVITY.FILE_NAME)
                        .edit()
                        .putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_JSON_STRING, "needLogin")
                        .putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.AUTO_LOGIN, "0")
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
                materialEditTextSno.setText(String.valueOf(GlobalUtil.user.getSno()));
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
                            user.setSno(Integer.parseInt(sno[0]));
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
                                                .putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.AUTO_LOGIN, "0")
                                                .putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_PASSWORD, "")
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
    public int getStatusHeight(Activity activity) {
        int statusHeight = 0;
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
        if (GlobalUtil.user.getRoot() != 1) {
            if (GlobalUtil.user.getGroup() == 0) {
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
                break;
        }
    }


}
