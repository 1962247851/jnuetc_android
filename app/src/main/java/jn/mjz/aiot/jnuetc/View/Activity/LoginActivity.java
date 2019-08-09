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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.youth.xframe.utils.permission.XPermission;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.Util.SharedPreferencesUtil;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity=====";

    private boolean isPasswordAvailable = false, isNumberAvailable = false;
    private String number = "", password = "", autoLogin = "", rememberPassword = "";
    private MaterialEditText materialEditTextNumber, materialEditTextPassword;
    private Button buttonLogin, buttonForgetPassword;
    private SharedPreferences sharedPreferences;
    private CheckBox checkBoxRememberPassword, checkBoxAutoLogin;

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
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user", userJson);
            startActivity(intent);
            finish();
        } else {
            InitView();
            InitListener();
        }
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


        rememberPassword = sharedPreferences.getString(GlobalUtil.KEYS.LOGIN_ACTIVITY.REMEMBER_PASSWORD, "");
        autoLogin = sharedPreferences.getString(GlobalUtil.KEYS.LOGIN_ACTIVITY.AUTO_LOGIN, "");

        if (rememberPassword.equals("1")) {
            checkBoxRememberPassword.setChecked(true);
            number = sharedPreferences.getString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_NUMBER, "");
            password = sharedPreferences.getString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_PASSWORD, "");
            materialEditTextNumber.setText(number);
            materialEditTextPassword.setText(password);
            isNumberAvailable = true;
            isPasswordAvailable = true;
        }
        if (autoLogin.equals("1")) {
            checkBoxAutoLogin.setChecked(true);
            login();
        }

    }

    private class MyOnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_login_login:
                    if (isPasswordAvailable && isNumberAvailable) {
                        // TODO: 2019/4/24 登录
                        login();
                        buttonLogin.setClickable(false);
                    } else if (!isNumberAvailable && !isPasswordAvailable) {
                        XToast.error("请检查账号和密码!");
                    } else if (!isNumberAvailable) {
                        XToast.error("请检查账号!");
                    } else if (!isPasswordAvailable) {
                        XToast.error("请检查密码!");
                    }
                    break;
                case R.id.button_login_forget:
                    String url = "mqqwpa://im/chat?chat_type=wpa&uin=1962247851";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    break;
            }
        }
    }

    private void login() {
        XLoadingDialog.with(this).setCanceled(false).setMessage("账号验证中，请稍等").show();

        Map<String, Object> params = new HashMap<>();
        params.put("sno", number);
        params.put("password", password);
        HttpUtil.post.haveResponse(GlobalUtil.URLS.QUERY.LOGIN, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, String result) {
                buttonLogin.setClickable(true);
                XLoadingDialog.with(LoginActivity.this).cancel();
                String state = response.headers().get("state");
                if (state != null && state.equals("OK")) {
                    // TODO: 2019/7/10 登录成功
                    XToast.success("登录成功");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_NUMBER, number);
                    editor.putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_PASSWORD, password);
                    editor.putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.AUTO_LOGIN, checkBoxAutoLogin.isChecked() ? "1" : "0");
                    if (checkBoxAutoLogin.isChecked()) {
                        editor.putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.USER_JSON_STRING, result);
                        editor.putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.REMEMBER_PASSWORD, "1");
                    } else {
                        editor.putString(GlobalUtil.KEYS.LOGIN_ACTIVITY.REMEMBER_PASSWORD, checkBoxRememberPassword.isChecked() ? "1" : "0");
                    }
                    editor.apply();
                    finish();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("user", result);
                    startActivity(intent);
                } else {
                    XToast.error("登录失败");
                }
            }

            @Override
            public void onFailure(IOException e) {
                buttonLogin.setClickable(true);
                XToast.error("登录失败");
                XLoadingDialog.with(LoginActivity.this).cancel();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        XPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
