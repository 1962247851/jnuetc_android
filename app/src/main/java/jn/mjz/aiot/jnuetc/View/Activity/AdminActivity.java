package jn.mjz.aiot.jnuetc.View.Activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.Greendao.Entity.User;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import okhttp3.Response;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AdminActivity";
    private MainViewModel mainViewModel;
    private boolean firstOpen = true;

    @BindView(R.id.switch_admin)
    Switch aSwitch;
    @BindView(R.id.toolbar_admin)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ButterKnife.bind(this);

        InitData();
        InitView();
        InitListener();
        FirstOpen();
    }

    private void InitListener() {
    }

    private void FirstOpen() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("获取最新数据中").show();
        mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
            @Override
            public void onResponse(Response response, User result) {
                if (!(result.getRoot() == 1)) {
                    XToast.info("您已不是管理员");
                    finish();
                } else {
                    aSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                        if (!firstOpen) {
                            XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
                            mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                                @Override
                                public void onResponse(Response response, User result) {
                                    XLoadingDialog.with(AdminActivity.this).dismiss();
                                    if (result.getRoot() == 1) {
                                        if (b) {
                                            startService();
                                        } else {
                                            closeService();
                                        }
                                    } else {
                                        XToast.info("您已不是管理员");
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(IOException e) {
                                }
                            });

                        }
                    });
                    mainViewModel.checkState(new HttpUtil.HttpUtilCallBack<Boolean>() {
                        @Override
                        public void onResponse(Response response, Boolean result) {
                            XLoadingDialog.with(AdminActivity.this).dismiss();
                            aSwitch.setChecked(result);
                            firstOpen = false;
                        }

                        @Override
                        public void onFailure(IOException e) {
                            XLoadingDialog.with(AdminActivity.this).dismiss();
                            aSwitch.setChecked(false);
                            firstOpen = false;
                        }
                    });
                }
            }

            @Override
            public void onFailure(IOException e) {
            }
        });
    }

    private void InitView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        XStatusBar.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));
    }

    private void InitData() {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//        }
    }

    private void closeService() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("关闭服务中").show();
        mainViewModel.closeService(new HttpUtil.HttpUtilCallBack<Boolean>() {
            @Override
            public void onResponse(Response response, Boolean result) {
                XLoadingDialog.with(AdminActivity.this).dismiss();
                if (result) {
                    XToast.success("服务关闭成功");
                } else {
                    XToast.error("服务关闭失败");
                    aSwitch.setChecked(true);
                }

            }

            @Override
            public void onFailure(IOException e) {
                XLoadingDialog.with(AdminActivity.this).dismiss();
                XToast.error("服务关闭失败");
                aSwitch.setChecked(true);
            }
        });
    }

    private void startService() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("开启服务中").show();
        mainViewModel.openService(new HttpUtil.HttpUtilCallBack<Boolean>() {
            @Override
            public void onResponse(Response response, Boolean result) {
                if (result) {
                    XToast.success("服务开启成功");
                } else {
                    XToast.error("服务开启失败");
                    aSwitch.setChecked(false);
                }
                XLoadingDialog.with(AdminActivity.this).dismiss();
            }

            @Override
            public void onFailure(IOException e) {
                XToast.error("服务开启失败");
                aSwitch.setChecked(false);
                XLoadingDialog.with(AdminActivity.this).dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
