package jn.mjz.aiot.jnuetc.View.Activity;

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

import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.UpdateUtil;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int READ_REQUEST_CODE = 42;
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
        XStatusBar.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));

        textViewVersion.setText(String.format("版本：%s", UpdateUtil.getLocalVersionName(this)));

        UpdateUtil.checkForUpdate(new UpdateUtil.IUpdateListener() {
            @Override
            public void HaveNewVersion(String date, String url, String message, float newVersion) {
                textViewVersion.setText(String.format(Locale.getDefault(), "版本：%s（有新版本%.1f）", UpdateUtil.getLocalVersionName(AboutActivity.this), newVersion));
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
            case R.id.menu_about_history:
                history();
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

    private void history() {
        XLoadingDialog.with(AboutActivity.this).setMessage("获取最新数据中，请稍后").setCanceled(false).show();
        UpdateUtil.checkHistory(new UpdateUtil.IHistoryListener() {
            @Override
            public void Success(String historyString) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
                builder.setMessage(historyString)
                        .setTitle("历史更新日志")
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                        })
                        .show();
                XLoadingDialog.with(AboutActivity.this).dismiss();
            }

            @Override
            public void Error() {
                XToast.error("数据获取失败");
                XLoadingDialog.with(AboutActivity.this).dismiss();
            }
        });
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


    public void performFileSearch(String mimeType) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType(mimeType);
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

}
