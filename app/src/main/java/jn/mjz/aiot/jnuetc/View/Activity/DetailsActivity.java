package jn.mjz.aiot.jnuetc.View.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.bm.library.Info;
import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.utils.XNetworkUtils;
import com.youth.xframe.utils.permission.XPermission;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.DateUtil;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import okhttp3.Response;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DetailsActivity";
    private Data data;
    private MainViewModel mainViewModel;
    private Info mRectF;

    @BindView(R.id.toolbar_details)
    Toolbar toolbar;
    @BindView(R.id.button_details_feedback)
    Button buttonFeedback;
    @BindView(R.id.button_details_confirm)
    Button buttonConfirm;

    @BindView(R.id.textView_details_location)
    TextView textViewLocation;
    @BindView(R.id.textView_details_id)
    TextView textViewId;
    @BindView(R.id.textView_details_date)
    TextView textViewDate;
    @BindView(R.id.textView_details_name)
    TextView textViewName;
    @BindView(R.id.textView_details_tel)
    TextView textViewTel;
    @BindView(R.id.textView_details_qq)
    TextView textViewQQ;
    @BindView(R.id.textView_details_college)
    TextView textViewCollege;
    @BindView(R.id.textView_details_grade)
    TextView textViewGrade;
    @BindView(R.id.textView_details_model)
    TextView textViewModel;
    @BindView(R.id.textView_details_message)
    TextView textViewMessage;
    @BindView(R.id.textView_details_photo)
    TextView textViewPhoto;
    @BindView(R.id.photoView_details_1)
    PhotoView imageViewPhoto1;
    @BindView(R.id.photoView_details_2)
    PhotoView imageViewPhoto2;

    @BindView(R.id.textView_details_feedback)
    TextView textViewFeedback;

    @BindView(R.id.tidt_details_repairer)
    TextInputEditText textInputEditTextRepairer;
    @BindView(R.id.tidt_details_repairDate)
    TextInputEditText textInputEditTextRepairRepairDate;
    @BindView(R.id.til_details_repairDate)
    TextInputLayout textInputLayoutRepairDate;
    @BindView(R.id.tidt_details_repair_message)
    TextInputEditText textInputEditTextRepairMessage;

    @BindView(R.id.spinner_mark)
    Spinner spinnerMark;
    @BindView(R.id.spinner_service)
    Spinner spinnerService;

    @BindView(R.id.linearLayout_details_feedback)
    LinearLayout linearLayoutFeedback;
    @BindView(R.id.linearLayout_details_photo)
    LinearLayout linearLayoutPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        data = GsonUtil.getInstance().fromJson(getIntent().getStringExtra("data"), Data.class);

        buttonFeedback.setOnClickListener(this);
        buttonConfirm.setOnClickListener(this);
        textViewQQ.setOnClickListener(this);
        textViewTel.setOnClickListener(this);

        textViewQQ.setOnClickListener(this);

        ArrayAdapter<String> adapterMark = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, GlobalUtil.MARKS);
        spinnerMark.setAdapter(adapterMark);
        ArrayAdapter<String> adapterService = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, GlobalUtil.SERVICES);
        spinnerService.setAdapter(adapterService);

        textViewLocation.setText(data.getLocal());
        textViewId.setText(String.valueOf(data.getId()));
        textViewDate.setText(DateUtil.getDateAndTime(data.getDate(), " "));
        textViewName.setText(data.getName());
        textViewTel.setText(String.valueOf(data.getTel()));
        textViewQQ.setText(String.valueOf(data.getQq()));
        textViewCollege.setText(data.getCollege());
        textViewGrade.setText(data.getGrade());
        textViewModel.setText(data.getModel());
        textViewMessage.setText(data.getMessage());

        String photo = data.getPhoto();
        if (photo != null && !photo.isEmpty()) {
            textViewPhoto.setText("图片加载中");
            linearLayoutPhoto.setVisibility(View.VISIBLE);
            String[] photos = photo.split("。");
            Glide.with(DetailsActivity.this)
                    .load(GlobalUtil.URLS.FILE.DOWNLOAD + "?url=/opt/dataDP&fileName=" + data.getUuid() + photos[0])
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            XToast.error("图片加载失败，请稍后重试");
                            linearLayoutPhoto.setVisibility(View.GONE);
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            linearLayoutPhoto.setVisibility(View.GONE);
                            imageViewPhoto1.setImageDrawable(resource);
                            imageViewPhoto2.setImageDrawable(resource);
                            imageViewPhoto1.disenable();
                            imageViewPhoto1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    imageViewPhoto1.setVisibility(View.GONE);
                                    imageViewPhoto2.setVisibility(View.VISIBLE);
                                    //获取img1的信息
                                    mRectF = imageViewPhoto1.getInfo();
                                    //让img2从img1的位置变换到他本身的位置
                                    imageViewPhoto2.animaFrom(mRectF);
                                }
                            });
                            return true;
                        }
                    }).into(imageViewPhoto1);
        } else {
            imageViewPhoto1.setVisibility(View.GONE);
        }

        // 需要启动缩放需要手动开启
        imageViewPhoto2.enable();
        imageViewPhoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 让img2从自身位置变换到原来img1图片的位置大小
                imageViewPhoto2.animaTo(mRectF, new Runnable() {
                    @Override
                    public void run() {
                        imageViewPhoto2.setVisibility(View.GONE);
                        imageViewPhoto1.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        switch (data.getState()) {
            case 0://未处理，只可以接单
                buttonConfirm.setVisibility(View.VISIBLE);
                break;
            case 1://处理中，只有自己参与的单子才可以可填写反馈，否则也只能查看
                if (data.getRepairer() != null && data.getRepairer().contains(GlobalUtil.user.getName())) {
                    linearLayoutFeedback.setVisibility(View.VISIBLE);
                    textInputEditTextRepairer.setEnabled(true);
                    textInputEditTextRepairer.setText(data.getRepairer());
                }
                break;
            case 2://已维修，除了管理员外，所有内容均不可修改
                linearLayoutFeedback.setVisibility(View.VISIBLE);
                buttonFeedback.setVisibility(View.GONE);

                spinnerMark.setEnabled(false);
                spinnerService.setEnabled(false);
                textInputEditTextRepairMessage.setEnabled(false);

                textViewFeedback.setText("查看反馈");

                textInputLayoutRepairDate.setVisibility(View.VISIBLE);
                textInputEditTextRepairRepairDate.setText(DateUtil.getDateAndTime(data.getRepairDate(), " "));

                for (int i = 0; i < GlobalUtil.MARKS.length; i++) {
                    if (data.getMark().equals(GlobalUtil.MARKS[i])) {
                        spinnerMark.setSelection(i);
                        break;
                    }
                }

                textInputEditTextRepairer.setText(data.getRepairer());

                for (int i = 0; i < GlobalUtil.SERVICES.length; i++) {
                    if (data.getService().equals(GlobalUtil.SERVICES[i])) {
                        spinnerService.setSelection(i);
                        break;
                    }
                }
                textInputEditTextRepairMessage.setText(data.getRepairMessage());
                break;
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        XStatusBar.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));

    }

    @Override
    public void onBackPressed() {
        if (imageViewPhoto2.getVisibility() == View.VISIBLE) {
            imageViewPhoto2.animaTo(mRectF, new Runnable() {
                @Override
                public void run() {
                    imageViewPhoto2.setVisibility(View.GONE);
                    imageViewPhoto1.setVisibility(View.VISIBLE);
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    private void setSystemUIVisible(boolean show) {
        if (show) {
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            uiFlags |= 0x00001000;
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        } else {
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiFlags |= 0x00001000;
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_details_feedback:
                feedback(data);
                break;
            case R.id.button_details_confirm:
                order(data);
                break;
            case R.id.textView_details_qq:
                String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + data.getQq();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    XToast.error("未安装手Q或安装的版本不支持");
                }
                break;
            case R.id.textView_details_tel:
                openTel(data.getTel());
                break;
        }

    }

    private void openTel(String tel) {
        XPermission.requestPermissions(DetailsActivity.this, 0, new String[]{Manifest.permission.CALL_PHONE}, new XPermission.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(String.format(Locale.getDefault(), "tel:%s", tel)));
                startActivity(intent);
            }

            @Override
            public void onPermissionDenied() {
                XPermission.showTipsDialog(DetailsActivity.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        XPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void makeOver(Data data) {
        XLoadingDialog.with(DetailsActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
        mainViewModel.queryAllUser(new HttpUtil.HttpUtilCallBack<List<String>>() {
            @Override
            public void onResponse(Response response, List<String> result) {
                XLoadingDialog.with(DetailsActivity.this).dismiss();
                result.remove(GlobalUtil.user.getName());
                String[] names = new String[result.size()];
                result.toArray(names);
                boolean[] checkItems = new boolean[result.size()];
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                builder.setTitle("请选择一个或多个被转让人")
                        .setMultiChoiceItems(names, checkItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                StringBuilder names = new StringBuilder();
                                for (int j = 0; j < checkItems.length; j++) {
                                    if (checkItems[j]) {
                                        names.append(result.get(j));
                                        names.append("，");
                                    }
                                }
                                if (names.toString().isEmpty()) {
                                    XToast.info("未选择");
                                } else {
                                    names.deleteCharAt(names.length() - 1);
                                    String repairer = names.toString();
                                    if (!repairer.isEmpty() && !repairer.contains(GlobalUtil.user.getName())) {
                                        data.setState((short) 1);
                                        data.setRepairer(repairer);
                                        data.setOrderDate(data.getOrderDate());
                                        data.setRepairDate(System.currentTimeMillis());
                                        makeOver();
                                    } else {
                                        XToast.info("请检查被转让人");
                                    }
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setNeutralButton("我再想想", null)
                        .show();
            }

            @Override
            public void onFailure(IOException e) {
                XLoadingDialog.with(DetailsActivity.this).cancel();
            }
        });
    }

    private void makeOver() {
        XLoadingDialog.with(this).setCanceled(false).setMessage("请求处理中，请稍后").show();
        mainViewModel.feedback(data, new HttpUtil.HttpUtilCallBack<Data>() {
            @Override
            public void onResponse(Response response, Data result) {
                XLoadingDialog.with(DetailsActivity.this).dismiss();
                XToast.success("转让成功");
                Intent intent = new Intent();
                intent.putExtra("makeover", true);
                intent.putExtra("position", getIntent().getIntExtra("position", -1));
                intent.putExtra("data", result.toString());
                setResult(0, intent);
                finish();
            }

            @Override
            public void onFailure(IOException e) {
                XLoadingDialog.with(DetailsActivity.this).dismiss();
                XToast.error("转让失败");
            }
        });
    }

    private void order(Data data) {
        XLoadingDialog.with(this).setMessage("请求处理中，请稍后").setCanceled(false).show();
        mainViewModel.queryById(String.valueOf(data.getId()), new HttpUtil.HttpUtilCallBack<Data>() {

            @Override
            public void onResponse(Response response, Data result) {
                XLoadingDialog.with(DetailsActivity.this).cancel();
                if (result.getState() == 0) {
                    String[] items = {"接单成功后自动打开QQ会话"};
                    boolean[] booleans = {true};
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    builder.setTitle(data.getLocal() + " - " + data.getId())
                            .setCancelable(false)
                            .setMultiChoiceItems(items, booleans, (dialogInterface, i, b) -> {
                            })
                            .setNegativeButton("取消", (dialogInterface, i) -> XLoadingDialog.with(DetailsActivity.this).cancel())
                            .setPositiveButton("确定", (dialogInterface, i) -> {
                                XLoadingDialog.with(DetailsActivity.this).setMessage("请求处理中，请稍后").setCanceled(false).show();
                                result.setRepairer(GlobalUtil.user.getName());
                                result.setState((short) 1);
                                mainViewModel.feedback(result, new HttpUtil.HttpUtilCallBack<Data>() {
                                    @Override
                                    public void onResponse(Response response1, Data o) {
                                        XLoadingDialog.with(DetailsActivity.this).dismiss();
                                        XToast.success("接单成功");

                                        mainViewModel.dataDao.update(o);

                                        Intent intent = new Intent();
                                        intent.putExtra("order", true);
                                        intent.putExtra("data", o.toString());
                                        intent.putExtra("position", getIntent().getIntExtra("position", -1));
                                        setResult(0, intent);
                                        if (booleans[0]) {
                                            String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + result.getQq();
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                            } catch (Exception e) {
                                                XToast.error("未安装手Q或安装的版本不支持");
                                            }
                                        }
                                        finish();

                                    }

                                    @Override
                                    public void onFailure(IOException e) {
                                        XLoadingDialog.with(DetailsActivity.this).cancel();
                                        XToast.error("接单失败");
                                    }
                                });
                            })
                            .create()
                            .show();

                } else {
                    XLoadingDialog.with(DetailsActivity.this).dismiss();
                    XToast.warning("唉呀，有人抢先了...\n该报修单已被 " + result.getRepairer() + " 处理");
                    Intent intent = new Intent();
                    intent.putExtra("order", true);
                    intent.putExtra("position", getIntent().getIntExtra("position", -1));
                    setResult(0, intent);
                }
            }

            @Override
            public void onFailure(IOException e) {
                Log.e(TAG, "onFailure: " + e);
                XToast.error("请求失败，请重试");
                XLoadingDialog.with(DetailsActivity.this).dismiss();
            }
        });
    }

    private void feedback(Data data) {
        data.setState((short) 2);
        data.setRepairer(textInputEditTextRepairer.getText().toString());
        data.setMark(spinnerMark.getSelectedItem().toString());
        data.setService(spinnerService.getSelectedItem().toString());
        data.setRepairMessage(textInputEditTextRepairMessage.getText().toString());

        if (data.getRepairer().isEmpty() || data.getRepairMessage().isEmpty()) {
            XToast.error("字段不能为空");
        } else if (!data.getRepairer().contains(GlobalUtil.user.getName())) {
            AlertDialog dialog = new AlertDialog.Builder(DetailsActivity.this).create();
            dialog.setMessage("维修人未含本人，确定提交反馈？");
            dialog.setTitle("注意");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.cancel();
                    feedback();
                }
            });
            dialog.show();
        } else {
            feedback();
        }
    }

    private void feedback() {
        XLoadingDialog.with(DetailsActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
        mainViewModel.feedback(data, new HttpUtil.HttpUtilCallBack<Data>() {
            @Override
            public void onResponse(Response response, Data result) {
                XLoadingDialog.with(DetailsActivity.this).dismiss();
                if (result != null) {
                    XToast.success("反馈成功");
                    Intent intent = new Intent();
                    intent.putExtra("feedback", true);
                    intent.putExtra("data", result.toString());
                    intent.putExtra("position", getIntent().getIntExtra("position", -1));
                    setResult(0, intent);
                    finish();
                } else {
                    XToast.error("反馈失败");
                }
            }

            @Override
            public void onFailure(IOException e) {
                XLoadingDialog.with(DetailsActivity.this).dismiss();
                XToast.error("反馈失败");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_details, menu);
        if (data.getState() == 1) {
            if (data.getRepairer() != null && data.getRepairer().contains(GlobalUtil.user.getName())) {
                menu.findItem(R.id.menu_details_make_over).setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_details_make_over:
                makeOver(data);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
