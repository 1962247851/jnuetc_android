package jn.mjz.aiot.jnuetc.View.Activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
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
    @BindView(R.id.button_admin_delete)
    Button buttonDelete;
    @BindView(R.id.editText_admin_delete)
    EditText editTextDelete;
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
        buttonDelete.setOnClickListener(AdminActivity.this);
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
        switch (view.getId()) {
            case R.id.button_admin_delete:
                String id = editTextDelete.getText().toString();
                if (id.isEmpty()) {
                    XToast.error("Id不能为空");
                } else {
                    mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                        @Override
                        public void onResponse(Response response, User result) {
                            if (result.getRoot() == 1) {
                                XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("删除中").show();
                                mainViewModel.queryById(id, new HttpUtil.HttpUtilCallBack<Data>() {
                                    @Override
                                    public void onResponse(Response response, Data result) {
                                        if (result != null) {
                                            mainViewModel.delete(id, new HttpUtil.HttpUtilCallBack<Boolean>() {
                                                @Override
                                                public void onResponse(Response response, Boolean result) {
                                                    XLoadingDialog.with(AdminActivity.this).dismiss();
                                                    if (result) {
                                                        XToast.success("删除成功");
                                                    } else {
                                                        XToast.error("删除失败");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(IOException e) {
                                                    XToast.error("删除失败");
                                                    XLoadingDialog.with(AdminActivity.this).dismiss();
                                                }
                                            });
                                        } else {
                                            XToast.error("报修单不存在");
                                            XLoadingDialog.with(AdminActivity.this).dismiss();
                                        }
                                    }

                                    @Override
                                    public void onFailure(IOException e) {
                                        if (e == null) {
                                            XToast.error("该单不存在");
                                        } else {
                                            XToast.error("删除失败");
                                        }
                                        XLoadingDialog.with(AdminActivity.this).dismiss();
                                    }
                                });
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
                break;
        }
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
