package jn.mjz.aiot.jnuetc.View.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.permission.XPermission;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.Greendao.Entity.User;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.DateUtil;
import jn.mjz.aiot.jnuetc.Util.FileUtil;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import okhttp3.Response;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AdminActivity";
    private MainViewModel mainViewModel;
    private boolean firstOpen = true;
    private static final int EXPORT2EXCEL = 1;

    @BindView(R.id.switch_admin)
    Switch aSwitch;
    @BindView(R.id.toolbar_admin)
    Toolbar toolbar;
    @BindView(R.id.button_admin_insert)
    Button buttonInsert;
    @BindView(R.id.button_admin_export)
    Button buttonExport;
    @BindView(R.id.button_admin_upload_DP)
    Button buttonUpload;

    @BindView(R.id.tidt_admin_code)
    TextInputEditText textInputEditTextCode;
    @BindView(R.id.relativeLayout_admin_code)
    RelativeLayout relativeLayoutCode;

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
        buttonInsert.setOnClickListener(this);
        buttonExport.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
    }

    private void FirstOpen() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("获取最新数据中").show();
        mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
            @Override
            public void onResponse(Response response, User result) {
                if ((result.getRoot() == 0)) {
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
                                    if (result.getRoot() != 0) {
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

        if (MainViewModel.user.getRoot() == 3) {
            relativeLayoutCode.setVisibility(View.VISIBLE);
        }
    }

    private void InitData() {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_admin_insert:
                String code = textInputEditTextCode.getText().toString();
                if (!code.isEmpty()) {
                    insertCode(code);
                } else {
                    XToast.error("邀请码不能为空");
                }
                break;
            case R.id.button_admin_export:
                XPermission.requestPermissions(AdminActivity.this, EXPORT2EXCEL, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new XPermission.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        FileUtil.createFile(AdminActivity.this, "text/*", String.format("报修单数据%s.xls", DateUtil.getDateAndTime(System.currentTimeMillis(), " ")));
                    }

                    @Override
                    public void onPermissionDenied() {
                        XToast.error("未获取读写权限");
                    }
                });
                break;
            case R.id.button_admin_upload_DP:
                XPermission.requestPermissions(AdminActivity.this, FileUtil.READ_REQUEST_CODE, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new XPermission.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        FileUtil.toPicture(AdminActivity.this);
                    }

                    @Override
                    public void onPermissionDenied() {
                        XToast.error("未获取读写权限");
                    }
                });

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FileUtil.WRITE_REQUEST_CODE && data != null) {
            XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("导出中，请稍等...");
            FileUtil.ExportDatasToExcel(AdminActivity.this, data.getData(), new FileUtil.IOnExportListener() {
                @Override
                public void OnSuccess() {
                    XToast.success("导出成功");
                    XLoadingDialog.with(AdminActivity.this).dismiss();
                }

                @Override
                public void OnError() {
                    XToast.error("导出失败，请重试");
                    XLoadingDialog.with(AdminActivity.this).dismiss();
                }
            });
        } else if (requestCode == FileUtil.PIC_PICTURE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                // TODO: 2019/10/20 上传图片
                Uri uri = data.getData();
                Log.e(TAG, "onActivityResult: " + uri.getPath());
                String[] split = uri.getPath().split("/");
                String fileName = split[split.length - 1];
                final File file = new File(uri.getPath());
                Log.e(TAG, "onActivityResult: " + file.getName());
                Log.e(TAG, "onActivityResult: " + file.length());
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                FileUtil.UploadTipDp(fileName, file, new HttpCallBack<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {

                    }

                    @Override
                    public void onFailed(String error) {

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void insertCode(String code) {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("添加验证码中，请稍后").show();
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        HttpUtil.post.haveResponse(GlobalUtil.URLS.INSERT.INSERT_CODE, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, String result) {
                String state = response.headers().get("state");
                if (state != null && state.equals("OK")) {
                    XToast.success("添加成功");
                } else {
                    XToast.error("该邀请码已存在");
                }
                XLoadingDialog.with(AdminActivity.this).dismiss();
            }

            @Override
            public void onFailure(IOException e) {
                XLoadingDialog.with(AdminActivity.this).dismiss();
                XToast.error("添加失败，请重试");
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        XPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
