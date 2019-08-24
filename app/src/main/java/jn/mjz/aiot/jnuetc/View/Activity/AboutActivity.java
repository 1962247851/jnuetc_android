package jn.mjz.aiot.jnuetc.View.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.UpdateUtil;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

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
        XStatusBar.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));

        textViewVersion.setText(String.format("版本：%s", UpdateUtil.getLocalVersionName(this)));

        UpdateUtil.checkForUpdate(new UpdateUtil.IUpdateListener() {
            @Override
            public void HaveNewVersion(String date, String url, String message, float newVersion) {

            }

            @Override
            public void NoUpdate() {
                textViewVersion.setText(String.format("版本：%s（当前是最新版本）", UpdateUtil.getLocalVersionName(AboutActivity.this)));
            }

            @Override
            public void Error() {

            }
        });


        textViewVersion.setOnClickListener(this);
        textViewGithub.setOnClickListener(this);
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
            case R.id.menu_about_feedback:
                String url = "mqqwpa://im/chat?chat_type=wpa&uin=1962247851";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    XToast.error("未安装手Q或安装的版本不支持");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textView_about_version:
                UpdateUtil.checkForUpdate(false, AboutActivity.this, view);
                break;
            case R.id.textView_about_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(((TextView) view).getText().toString())));
                break;
        }
    }
}
