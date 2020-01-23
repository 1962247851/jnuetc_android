package jn.mjz.aiot.jnuetc.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.youth.xframe.XFrame;
import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.Version;
import jn.mjz.aiot.jnuetc.util.UpdateUtil;

/**
 * @author 19622
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AboutActivity";
    @BindView(R.id.toolbar_about)
    Toolbar toolbar;
    @BindView(R.id.textView_about_version)
    TextView textViewVersion;
    @BindView(R.id.textView_about_github)
    TextView textViewGithub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        XStatusBar.setColorNoTranslucent(this, XFrame.getColor(R.color.colorPrimary));

        textViewVersion.setText(String.format("版本：%s", XAppUtils.getVersionName(this)));

        UpdateUtil.checkForUpdate(new UpdateUtil.IUpdateListener() {
            @Override
            public void haveNewVersion(Version version) {
                textViewVersion.setText(String.format(Locale.getDefault(), "版本：%s（有新版本%s）", XAppUtils.getVersionName(AboutActivity.this), version.getVersion()));
            }

            @Override
            public void noUpdate() {
                textViewVersion.setText(String.format("版本：%s（当前是最新版本）", XAppUtils.getVersionName(AboutActivity.this)));
            }

            @Override
            public void error() {

            }

            @Override
            public void develop() {
                textViewVersion.setText(String.format("版本：%s（当前是开发版本）", XAppUtils.getVersionName(AboutActivity.this)));
            }
        });

        textViewVersion.setOnClickListener(this);
        textViewGithub.setOnClickListener(this);

        App.initToolbar(toolbar, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_about_history:
                history();
                return true;
            case R.id.menu_about_feedback:
                Data.openQq("1962247851");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void history() {
        XLoadingDialog.with(AboutActivity.this).setMessage("获取最新数据中，请稍后").setCanceled(false).show();
        UpdateUtil.checkHistory(new UpdateUtil.IHistoryListener() {
            @Override
            public void success(String historyString) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
                builder.setMessage(historyString)
                        .setTitle("历史更新日志")
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                        })
                        .show();
                XLoadingDialog.with(AboutActivity.this).cancel();
            }

            @Override
            public void error() {
                XToast.error("数据获取失败");
                XLoadingDialog.with(AboutActivity.this).cancel();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textView_about_version:
                UpdateUtil.checkForUpdate(false, AboutActivity.this, null);
                break;
            case R.id.textView_about_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(((TextView) view).getText().toString())));
                break;
            default:
        }
    }

}
