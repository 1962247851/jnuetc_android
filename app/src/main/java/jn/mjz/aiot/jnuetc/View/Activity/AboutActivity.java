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

//                XPermission.requestPermissions(this, 66, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new XPermission.OnPermissionListener() {
//                    @Override
//                    public void onPermissionGranted() {
//                        createNewFile("image/png", "jnuetc");
//                    }
//
//                    @Override
//                    public void onPermissionDenied() {
//
//                    }
//                });

//                XPermission.requestPermissions(this, READ_REQUEST_CODE, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new XPermission.OnPermissionListener() {
//                    @Override
//                    public void onPermissionGranted() {
//                        performFileSearch("image/*");
//                    }
//
//                    @Override
//                    public void onPermissionDenied() {
//
//                    }
//                });

                UpdateUtil.checkForUpdate(false, AboutActivity.this, view);
                break;
            case R.id.textView_about_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(((TextView) view).getText().toString())));
                break;
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        Log.e(TAG, "onActivityResult: requestCode = " + requestCode);
//        Log.e(TAG, "onActivityResult: resultCode = " + resultCode);
//        Log.e(TAG, "onActivityResult: data = " + data);
//        Log.e(TAG, "onActivityResult: data.getData() = " + data.getData());
//        switch (requestCode) {
//            case 66:
//                Map<String, Object> params = new HashMap<>();
//                params.put("url", "/opt/userDP");
//                params.put("version", "1");
//                HttpUtil.get.downloadFile(this, data.getData(), GlobalUtil.URLS.FILE.DOWNLOAD, params, new HttpUtil.IFileDownloadListener() {
//                    @Override
//                    public void onStart() {
//                        Log.e(TAG, "onStart: ");
//                    }
//
//                    @Override
//                    public void onDownloading(int progress) {
//                        Log.e(TAG, "onDownloading: " + progress);
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        Log.e(TAG, "onFinish: ");
////                Log.e(TAG, "onFinish: " + file.exists());
////                Log.e(TAG, "onFinish: " + file.length());
////                Log.e(TAG, "onFinish: " + file.getAbsolutePath());
//                    }
//
//                    @Override
//                    public void onError(String errorMessage) {
//                        Log.e(TAG, "onError: " + errorMessage);
//                    }
//                });
//                break;
//            case READ_REQUEST_CODE:
//
//                break;
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        XPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

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

    public void createNewFile(String mimeType, String fileName) {
        // Here are some examples of how you might call this method.
        // The first parameter is the MIME type, and the second parameter is the name
        // of the file you are creating:
        //
        // createFile("text/plain", "foobar.txt");
        // createFile("image/png", "mypicture.png");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, 6);
    }
}
