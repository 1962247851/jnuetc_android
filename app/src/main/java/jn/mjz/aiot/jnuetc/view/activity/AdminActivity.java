package jn.mjz.aiot.jnuetc.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.http.XHttp;
import com.youth.xframe.utils.permission.XPermission;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.entity.User;
import jn.mjz.aiot.jnuetc.util.DateUtil;
import jn.mjz.aiot.jnuetc.util.FileUtil;
import jn.mjz.aiot.jnuetc.util.GlideAppEngine;
import jn.mjz.aiot.jnuetc.util.GlobalUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class AdminActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AdminActivity";
    private MainViewModel mainViewModel;
    private boolean firstOpen = true;
    private static final int EXPORT2EXCEL = 1;

    @BindView(R.id.switch_admin)
    Switch aSwitch;
    @BindView(R.id.switch_admin_dayDP)
    Switch aSwitchDayPhoto;
    @BindView(R.id.switch_admin_register)
    Switch aSwitchRegister;
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

        initData();
        initView();
        initListener();
        firstOpen();
    }

    private void initListener() {
        buttonInsert.setOnClickListener(this);
        buttonExport.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
    }

    private void firstOpen() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("获取最新数据中").show();
        mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
            @Override
            public void onResponse(User result) {
                if (!(result.haveDeleteAccess())) {
                    XToast.info("您已不是管理员");
                    finish();
                } else {
                    mainViewModel.checkServerState(new HttpUtil.HttpUtilCallBack<Boolean>() {
                        @Override
                        public void onResponse(Boolean result) {
                            XLoadingDialog.with(AdminActivity.this).cancel();
                            aSwitch.setChecked(result);
                            firstOpen = false;
                            aSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                                if (!firstOpen) {
                                    XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
                                    mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                                        @Override
                                        public void onResponse(User result) {
                                            XLoadingDialog.with(AdminActivity.this).dismiss();
                                            if (result.haveDeleteAccess()) {
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
                                        public void onFailure(String error) {
                                        }
                                    });
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            XLoadingDialog.with(AdminActivity.this).cancel();
                            aSwitch.setChecked(false);
                            firstOpen = false;
                        }
                    });
                    mainViewModel.checkDayDayPhotoState(new HttpUtil.HttpUtilCallBack<Boolean>() {
                        @Override
                        public void onResponse(Boolean result) {
                            XLoadingDialog.with(AdminActivity.this).cancel();
                            aSwitchDayPhoto.setChecked(result);
                            firstOpen = false;
                            aSwitchDayPhoto.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                if (!firstOpen) {
                                    XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
                                    mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                                        @Override
                                        public void onResponse(User result1) {
                                            XLoadingDialog.with(AdminActivity.this).dismiss();
                                            if (result1.haveDeleteAccess()) {
                                                if (isChecked) {
                                                    startDayDpService();
                                                } else {
                                                    closeDayDpService();
                                                }
                                            } else {
                                                XToast.info("您已不是管理员");
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                        }
                                    });

                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            XLoadingDialog.with(AdminActivity.this).cancel();
                            aSwitchDayPhoto.setChecked(false);
                            firstOpen = false;
                        }
                    });
                    mainViewModel.checkRegisterState(new HttpUtil.HttpUtilCallBack<Boolean>() {
                        @Override
                        public void onResponse(Boolean result) {
                            XLoadingDialog.with(AdminActivity.this).cancel();
                            aSwitchRegister.setChecked(result);
                            firstOpen = false;
                            aSwitchRegister.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                if (!firstOpen) {
                                    XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
                                    mainViewModel.updateUserInfo(new HttpUtil.HttpUtilCallBack<User>() {
                                        @Override
                                        public void onResponse(User result1) {
                                            XLoadingDialog.with(AdminActivity.this).cancel();
                                            if (result1.haveDeleteAccess()) {
                                                if (isChecked) {
                                                    startRegisterService();
                                                } else {
                                                    closeRegisterService();
                                                }
                                            } else {
                                                XToast.info("您已不是管理员");
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                        }
                                    });
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            XLoadingDialog.with(AdminActivity.this).cancel();
                            aSwitchRegister.setChecked(false);
                            firstOpen = false;
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
            }
        });
    }

    private void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        XStatusBar.setColorNoTranslucent(this, XFrame.getColor(R.color.colorPrimary));

        if (MainViewModel.user.haveAdministratorAccess()) {
            aSwitch.setVisibility(View.VISIBLE);
            aSwitchDayPhoto.setVisibility(View.VISIBLE);
            buttonUpload.setVisibility(View.VISIBLE);
            aSwitchRegister.setVisibility(View.VISIBLE);
            relativeLayoutCode.setVisibility(View.VISIBLE);
        }
        if (MainViewModel.user.haveDeleteAccess()) {
            aSwitchDayPhoto.setVisibility(View.VISIBLE);
            buttonUpload.setVisibility(View.VISIBLE);
        }

        App.initToolbar(toolbar, this);
    }

    private void initData() {
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
                        FileUtil.createFile(AdminActivity.this, "text/*", String.format("报修单数据%s.xlsx", DateUtil.getDateAndTime(System.currentTimeMillis(), " ")));
                    }

                    @Override
                    public void onPermissionDenied() {
                        XToast.error("未获取读写权限");
                    }
                });
                break;
            case R.id.button_admin_upload_DP:
                XPermission.requestPermissions(AdminActivity.this, FileUtil.READ_REQUEST_CODE, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new XPermission.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Matisse.from(AdminActivity.this)
                                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG))
                                .countable(true)
                                .maxSelectable(1)
                                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                .thumbnailScale(0.85f)
                                .imageEngine(new GlideAppEngine())
                                .forResult(FileUtil.PIC_PICTURE);
                    }

                    @Override
                    public void onPermissionDenied() {
                        XToast.error("未获取读写权限");
                    }
                });

                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FileUtil.WRITE_REQUEST_CODE && data != null) {
            XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("导出中，请稍等...");
            FileUtil.exportDatasToExcel(AdminActivity.this, data.getData(), new FileUtil.IOnExportListener() {
                @Override
                public void onSuccess() {
                    XToast.success("导出成功");
                    XLoadingDialog.with(AdminActivity.this).cancel();
                }

                @Override
                public void onError() {
                    XToast.error("导出失败，请检查读写权限后重试");
                    XLoadingDialog.with(AdminActivity.this).dismiss();
                }
            });
        } else if (requestCode == FileUtil.PIC_PICTURE && resultCode == Activity.RESULT_OK && data != null) {

            List<Uri> uris = Matisse.obtainResult(data);
//            Log.e(TAG, uris.get(0).toString());
            File file = FileUtil.uriToFile(uris.get(0), this);
//            Log.e(TAG, "onActivityResult: " + file.getName());
//            Log.e(TAG, "onActivityResult: " + file.length());
            String fileName = DateUtil.getCurrentDate("yyyy-MM-dd") + ".jpg";
//            Log.e(TAG, "onActivityResult: " + fileName);

            XLoadingDialog.with(AdminActivity.this).setMessage("图片上传中，请稍后").setCanceled(false).show();
            FileUtil.uploadTipDp(fileName, file, new HttpCallBack<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    XLoadingDialog.with(AdminActivity.this).cancel();
                    if (aBoolean) {
                        XToast.success("上传成功，如果重启APP后看不到效果清下缓存就好了");
                    } else {
                        XToast.error("上传失败");
                    }
                }

                @Override
                public void onFailed(String error) {
                    XLoadingDialog.with(AdminActivity.this).cancel();
                    XToast.error("上传失败\n" + error);
                }
            });
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void insertCode(String code) {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("添加验证码中，请稍后").show();
        Map<String, Object> params = new HashMap<>(1);
        params.put("code", code);
        XHttp.obtain().post(GlobalUtil.Urls.Code.INSERT, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                String msg = jsonObject.get("msg").getAsString();
                if (error == 1) {
                    XToast.success(msg);
                } else if (error == -1) {
                    XToast.info(msg);
                } else {
                    XToast.error(msg);
                }
                XLoadingDialog.with(AdminActivity.this).cancel();
            }

            @Override
            public void onFailed(String error) {
                XToast.error("添加失败，请重试");
                XLoadingDialog.with(AdminActivity.this).cancel();
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
            public void onResponse(Boolean result) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                if (result) {
                    XToast.success("服务关闭成功");
                } else {
                    XToast.error("服务关闭失败");
                    aSwitch.setChecked(true);
                }

            }

            @Override
            public void onFailure(String message) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                XToast.error("服务关闭失败");
                aSwitch.setChecked(true);
            }
        });
    }

    private void closeDayDpService() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("关闭服务中").show();
        mainViewModel.closeDayDayPhotoService(new HttpUtil.HttpUtilCallBack<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                if (result) {
                    XToast.success("服务关闭成功");
                } else {
                    XToast.error("服务关闭失败");
                    aSwitchDayPhoto.setChecked(true);
                }

            }

            @Override
            public void onFailure(String error) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                XToast.error("服务关闭失败");
                aSwitchDayPhoto.setChecked(true);
            }
        });
    }

    private void startDayDpService() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("开启服务中").show();
        mainViewModel.openDayDayPhotoService(new HttpUtil.HttpUtilCallBack<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                if (result) {
                    XToast.success("服务开启成功");
                } else {
                    XToast.error("服务开启失败");
                    aSwitchDayPhoto.setChecked(false);
                }

            }

            @Override
            public void onFailure(String error) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                XToast.error("服务开启失败");
                aSwitchDayPhoto.setChecked(false);
            }
        });
    }

    private void startService() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("开启服务中").show();
        mainViewModel.openService(new HttpUtil.HttpUtilCallBack<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                if (result) {
                    XToast.success("服务开启成功");
                } else {
                    XToast.error("服务开启失败");
                    aSwitch.setChecked(false);
                }
                XLoadingDialog.with(AdminActivity.this).cancel();
            }

            @Override
            public void onFailure(String error) {
                XToast.error("服务开启失败");
                aSwitch.setChecked(false);
                XLoadingDialog.with(AdminActivity.this).cancel();
            }
        });
    }

    private void closeRegisterService() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("关闭服务中").show();
        mainViewModel.closeRegisterService(new HttpUtil.HttpUtilCallBack<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                if (result) {
                    XToast.success("服务关闭成功");
                } else {
                    XToast.error("服务关闭失败");
                    aSwitchRegister.setChecked(true);
                }

            }

            @Override
            public void onFailure(String error) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                XToast.error("服务关闭失败");
                aSwitchRegister.setChecked(true);
            }
        });
    }

    private void startRegisterService() {
        XLoadingDialog.with(AdminActivity.this).setCanceled(false).setMessage("开启服务中").show();
        mainViewModel.openRegisterService(new HttpUtil.HttpUtilCallBack<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                if (result) {
                    XToast.success("服务开启成功");
                } else {
                    XToast.error("服务开启失败");
                    aSwitchRegister.setChecked(false);
                }

            }

            @Override
            public void onFailure(String error) {
                XLoadingDialog.with(AdminActivity.this).cancel();
                XToast.error("服务开启失败");
                aSwitchRegister.setChecked(false);
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
