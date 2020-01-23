package jn.mjz.aiot.jnuetc.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.http.XHttp;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.util.HashMap;
import java.util.Map;

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.User;
import jn.mjz.aiot.jnuetc.util.GlobalUtil;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.SharedPreferencesUtil;
import jn.mjz.aiot.jnuetc.util.UpdateUtil;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity=====";

    private boolean isPasswordAvailable = false, isNumberAvailable = false;
    private String number = "", password = "";
    private boolean autoLogin = true, rememberPassword = false;
    private MaterialEditText materialEditTextNumber, materialEditTextPassword;
    private Button buttonLogin, buttonForgetPassword;
    private SharedPreferences sharedPreferences;
    private CheckBox checkBoxRememberPassword, checkBoxAutoLogin;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.LoginActivity.FILE_NAME);
        String userJson = sharedPreferences.getString(GlobalUtil.Keys.LoginActivity.USER_JSON_STRING, "needLogin");
        if (!"needLogin".equals(userJson)) {
            MainViewModel.user = GsonUtil.getInstance().fromJson(userJson, User.class);
        }
        initView();
        UpdateUtil.checkForUpdate(true, this, new UpdateUtil.IServerAvailableListener() {
            @Override
            public void onServerInvalid() {

            }

            @Override
            public void onServerValid() {
                initListener();
                //退出到这个界面不用自动登录
                boolean logout = getIntent().getBooleanExtra("logout", false);
                if (!logout) {
                    if (MainViewModel.user != null && autoLogin) {
                        materialEditTextNumber.setText(String.valueOf(MainViewModel.user.getSno()));
                        materialEditTextPassword.setText(String.valueOf(MainViewModel.user.getPassword()));
                        login(false);
                    } else {
                        linearLayout.setVisibility(View.GONE);
                    }
                } else {
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });

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

    private void initListener() {
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

    private void initView() {

        materialEditTextNumber = findViewById(R.id.materialEditText_login_number);
        materialEditTextPassword = findViewById(R.id.materialEditText_login_password);
        checkBoxRememberPassword = findViewById(R.id.checkBox_login_remember_password);
        checkBoxAutoLogin = findViewById(R.id.checkBox_login_auto_login);
        buttonLogin = findViewById(R.id.button_login_login);
        buttonForgetPassword = findViewById(R.id.button_login_forget);
        linearLayout = findViewById(R.id.linearLayout_login_logo_welcome);

        rememberPassword = sharedPreferences.getBoolean(GlobalUtil.Keys.LoginActivity.REMEMBER_PASSWORD, false);
        autoLogin = sharedPreferences.getBoolean(GlobalUtil.Keys.LoginActivity.AUTO_LOGIN, true);

        checkBoxRememberPassword.setChecked(rememberPassword);
        checkBoxAutoLogin.setChecked(autoLogin);
        if (rememberPassword && MainViewModel.user != null) {
            number = MainViewModel.user.getSno();
            password = MainViewModel.user.getPassword();
            materialEditTextNumber.setText(number);
            materialEditTextPassword.setText(password);
            isNumberAvailable = true;
            isPasswordAvailable = true;
        }
        XStatusBar.setTranslucent(this);
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
                    Data.openQq("1962247851");
                    break;
                default:
            }
        }
    }

    private void login(boolean firstOpen) {
        XLoadingDialog.with(this).setCanceled(false).setMessage("账号验证中，请稍等").show();
        Map<String, Object> params = new HashMap<>(2);
        params.put("sno", firstOpen ? number : MainViewModel.user.getSno());
        params.put("password", firstOpen ? password : MainViewModel.user.getPassword());
        XHttp.obtain().post(GlobalUtil.Urls.User.LOGIN, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                XLoadingDialog.with(LoginActivity.this).cancel();
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    MainViewModel.user = GsonUtil.getInstance().fromJson(jsonObject.get("body").getAsString(), User.class);
                    XToast.success(firstOpen ? "登录成功" : String.format("欢迎回来 %s", MainViewModel.user.getUserName()));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(GlobalUtil.Keys.LoginActivity.AUTO_LOGIN, checkBoxAutoLogin.isChecked());
                    editor.putBoolean(GlobalUtil.Keys.LoginActivity.REMEMBER_PASSWORD, checkBoxRememberPassword.isChecked());
                    if (checkBoxAutoLogin.isChecked()) {
                        editor.putString(GlobalUtil.Keys.LoginActivity.USER_JSON_STRING, MainViewModel.user.toString());
                    }
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (error == 0) {
                    linearLayout.setVisibility(View.GONE);
                    XToast.error(jsonObject.get("msg").getAsString());
                } else if (error == -1) {
                    linearLayout.setVisibility(View.GONE);
                    XToast.error(jsonObject.get("msg").getAsString());
                }
            }

            @Override
            public void onFailed(String error) {
                XToast.error("登录失败");
                linearLayout.setVisibility(View.GONE);
                XLoadingDialog.with(LoginActivity.this).cancel();
            }
        });
    }
}
