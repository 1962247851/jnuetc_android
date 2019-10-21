package jn.mjz.aiot.jnuetc.View.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.youth.xframe.utils.permission.XPermission;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jn.mjz.aiot.jnuetc.Greendao.Entity.User;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.Util.SharedPreferencesUtil;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity=====";

    private boolean isPasswordAvailable = false, isNumberAvailable = false;
    private String number = "", password = "";
    private boolean autoLogin = false, rememberPassword = false;
    private MaterialEditText materialEditTextNumber, materialEditTextPassword;
    private Button buttonLogin, buttonForgetPassword;
    private SharedPreferences sharedPreferences;
    private CheckBox checkBoxRememberPassword, checkBoxAutoLogin;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            XPermission.requestPermissions(this, 111, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE}, null);
        } else {
            XPermission.requestPermissions(this, 111, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, null);
        }
        setContentView(R.layout.activity_login);
        sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.LOGIN_ACTIVITY.FILE_NAME);
        String userJson = sharedPreferences.getString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_JSON_STRING, "needLogin");
        if (!userJson.equals("needLogin")) {
            MainViewModel.user = GsonUtil.getInstance().fromJson(userJson, User.class);
        }
        InitView();
        InitListener();
        if (MainViewModel.user != null && autoLogin) {
            materialEditTextNumber.setText(String.valueOf(MainViewModel.user.getSno()));
            materialEditTextPassword.setText(String.valueOf(MainViewModel.user.getPassword()));
            login(false);
        }else {
            linearLayout.setVisibility(View.GONE);
        }
//        else {
//            checkBoxRememberPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    if (!b) {
//                        checkBoxRememberPassword.setChecked(false);
//                    }
//                }
//            });
//            checkBoxAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    if (b) {
//                        checkBoxRememberPassword.setChecked(true);
//                    }
//                }
//            });
//        }
    }

    private void InitListener() {
        MyOnClick myOnClick = new MyOnClick();

        buttonLogin.setOnClickListener(myOnClick);
        buttonForgetPassword.setOnClickListener(myOnClick);

        materialEditTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(" ")) {
                    materialEditTextPassword.setHelperText("密码不能带空格!");
                    isPasswordAvailable = false;
                } else if (s.length() > 18 || s.length() < 7) {
                    materialEditTextPassword.setHelperText("密码长度有误!");
                    isPasswordAvailable = false;
                } else {
                    materialEditTextPassword.setHelperText("");
                    isPasswordAvailable = true;
                    password = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialEditTextNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 5 || s.length() > 10) {
                    materialEditTextNumber.setHelperText("账号长度有误!");
                    isNumberAvailable = false;
                } else {
                    isNumberAvailable = true;
                    materialEditTextNumber.setHelperText("");
                    number = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void InitView() {

        materialEditTextNumber = findViewById(R.id.materialEditText_login_number);
        materialEditTextPassword = findViewById(R.id.materialEditText_login_password);
        checkBoxRememberPassword = findViewById(R.id.checkBox_login_remember_password);
        checkBoxAutoLogin = findViewById(R.id.checkBox_login_auto_login);
        buttonLogin = findViewById(R.id.button_login_login);
        buttonForgetPassword = findViewById(R.id.button_login_forget);
        linearLayout = findViewById(R.id.linearLayout_login_logo_welcome);

        rememberPassword = sharedPreferences.getBoolean(GlobalUtil.KEYS.LOGIN_ACTIVITY.REMEMBER_PASSWORD, false);
        autoLogin = sharedPreferences.getBoolean(GlobalUtil.KEYS.LOGIN_ACTIVITY.AUTO_LOGIN, false);

        checkBoxRememberPassword.setChecked(rememberPassword);
        checkBoxAutoLogin.setChecked(autoLogin);
        if (rememberPassword) {
            number = MainViewModel.user.getSno();
            password = MainViewModel.user.getPassword();
            materialEditTextNumber.setText(number);
            materialEditTextPassword.setText(password);
            isNumberAvailable = true;
            isPasswordAvailable = true;
        }
    }

    private class MyOnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_login_login:
                    if (isPasswordAvailable && isNumberAvailable) {
                        login(true);
                    } else if (!isNumberAvailable && !isPasswordAvailable) {
                        XToast.error("请检查账号和密码!");
                    } else if (!isNumberAvailable) {
                        XToast.error("请检查账号!");
                    } else {
                        XToast.error("请检查密码!");
                    }
                    break;
                case R.id.button_login_forget:
                    String url = "mqqwpa://im/chat?chat_type=wpa&uin=1962247851";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception e) {
                        XToast.error("未安装手Q或安装的版本不支持");
                    }
                    break;
            }
        }
    }

    private void login(boolean firstOpen) {
        XLoadingDialog.with(this).setCanceled(false).setMessage("账号验证中，请稍等").show();

        Map<String, Object> params = new HashMap<>();
        params.put("sno", firstOpen ? number : MainViewModel.user.getSno());
        params.put("password", firstOpen ? password : MainViewModel.user.getPassword());
        HttpUtil.post.haveResponse(GlobalUtil.URLS.QUERY.LOGIN, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, String result) {
                MainViewModel.user = GsonUtil.getInstance().fromJson(result, User.class);
                XLoadingDialog.with(LoginActivity.this).dismiss();
                String state = response.headers().get("state");
                if (state != null && state.equals("OK")) {
                    XToast.success(firstOpen ? "登录成功" : String.format("欢迎回来 %s", MainViewModel.user.getName()));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(GlobalUtil.KEYS.LOGIN_ACTIVITY.AUTO_LOGIN, checkBoxAutoLogin.isChecked());
                    editor.putBoolean(GlobalUtil.KEYS.LOGIN_ACTIVITY.REMEMBER_PASSWORD, checkBoxRememberPassword.isChecked());
                    if (checkBoxAutoLogin.isChecked()) {
                        editor.putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_JSON_STRING, result);
                    }
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    XToast.error("登录失败");
                    linearLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(IOException e) {
                XToast.error("登录失败");
                linearLayout.setVisibility(View.GONE);
                XLoadingDialog.with(LoginActivity.this).dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        XPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
