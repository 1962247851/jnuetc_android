package jn.mjz.aiot.jnuetc.View.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.youth.xframe.utils.permission.XPermission;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.Application.MyApplication;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.DateUtil;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.View.Fragment.SecondFragment;
import jn.mjz.aiot.jnuetc.ViewModel.MainViewModel;
import okhttp3.Response;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DetailsActivity";
    private Data data;
    private MainViewModel mainViewModel;


    @BindView(R.id.toolbar_details)
    Toolbar toolbar;
    @BindView(R.id.button_details_feedback)
    Button buttonFeedback;
    @BindView(R.id.button_details_confirm)
    Button buttonConfirm;
    @BindView(R.id.button_details_make_over)
    Button buttonMakeOver;

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
    @BindView(R.id.textView_details_model)
    TextView textViewModel;
    @BindView(R.id.textView_details_message)
    TextView textViewMessage;

    @BindView(R.id.textView_details_feedback)
    TextView textViewFeedback;

    //    @BindView(R.id.tidt_details_location)
//    TextInputEditText textInputEditTextLocation;
//    @BindView(R.id.tidt_details_id)
//    TextInputEditText textInputEditTextId;
//    @BindView(R.id.tidt_details_date)
//    TextInputEditText textInputEditTextDate;
//    @BindView(R.id.tidt_details_name)
//    TextInputEditText textInputEditTextName;
//    @BindView(R.id.tidt_details_tel)
//    TextInputEditText textInputEditTextTel;
//    @BindView(R.id.tidt_details_QQ)
//    TextInputEditText textInputEditTextQQ;
//    @BindView(R.id.tidt_details_model)
//    TextInputEditText textInputEditTextModel;
//    @BindView(R.id.tidt_details_message)
//    TextInputEditText textInputEditTextMessage;
    @BindView(R.id.tidt_details_repairer)
    TextInputEditText textInputEditTextRepairer;
    @BindView(R.id.tidt_details_repairDate)
    TextInputEditText textInputEditTextRepairRepairDate;
    @BindView(R.id.til_details_repairDate)
    TextInputLayout textInputLayoutRepairDate;
    @BindView(R.id.tidt_details_repair_message)
    TextInputEditText textInputEditTextRepairMessage;
//    @BindView(R.id.til_details_QQ)
//    TextInputLayout textInputLayoutQQ;
//    @BindView(R.id.til_details_tel)
//    TextInputLayout textInputLayoutTel;

    @BindView(R.id.spinner_mark)
    Spinner spinnerMark;
    @BindView(R.id.spinner_service)
    Spinner spinnerService;

    @BindView(R.id.linearLayout_details_feedback)
    LinearLayout linearLayoutFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        data = GsonUtil.getInstance().fromJson(getIntent().getStringExtra("data"), Data.class);

        buttonFeedback.setOnClickListener(this);
        buttonConfirm.setOnClickListener(this);
        buttonMakeOver.setOnClickListener(this);
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
        textViewModel.setText(data.getModel());
        textViewMessage.setText(data.getMessage());

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
                buttonMakeOver.setVisibility(View.GONE);

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_details_feedback:
                feedback(data);
                break;
            case R.id.button_details_confirm:
                order(data);
                break;
            case R.id.button_details_make_over:
                makeOver(data);
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
                XPermission.requestPermissions(DetailsActivity.this, 0, new String[]{Manifest.permission.CALL_PHONE}, new XPermission.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + data.getTel()));
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionDenied() {

                    }
                });
                break;
        }

    }

    private void makeOver(Data data) {
        String repairer = textInputEditTextRepairer.getText().toString();

        if (!repairer.isEmpty() && !repairer.contains(GlobalUtil.user.getName())) {

            data.setRepairer(repairer);
            data.setRepairDate(System.currentTimeMillis());

            AlertDialog dialog = new AlertDialog.Builder(DetailsActivity.this).create();
            dialog.setTitle("注意");
            dialog.setMessage("请确保被转让人的姓名无误");
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确认无误", (dialogInterface, i) -> {
                dialog.cancel();
                makeOver();
            });
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "我再瞅瞅", (dialogInterface, i) -> {
            });
            dialog.show();

        } else {
            XToast.info("请检查被转让人");
        }
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
        XLoadingDialog.with(DetailsActivity.this).setMessage("请求处理中，请稍后").setCanceled(false).show();
        mainViewModel.queryById(String.valueOf(data.getId()), new HttpUtil.HttpUtilCallBack<Data>() {

            @Override
            public void onResponse(Response response, Data result) {
                if (result.getState() == 0) {
                    String[] items = {"接单成功后自动打开QQ会话"};
                    boolean[] booleans = {true};
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    // TODO: 2019/7/10 设置customTitle
                    builder.setTitle(data.getLocal() + " - " + data.getId())
                            .setCancelable(false)
                            .setMultiChoiceItems(items, booleans, (dialogInterface, i, b) -> {
                            })
                            .setNegativeButton("取消", (dialogInterface, i) -> XLoadingDialog.with(DetailsActivity.this).cancel())
                            .setPositiveButton("确定", (dialogInterface, i) -> {
                                XLoadingDialog.with(DetailsActivity.this).dismiss();
                                // TODO: 2019/7/10
                                result.setRepairer(GlobalUtil.user.getName());
                                result.setState((short) 1);
                                result.setRepairDate(System.currentTimeMillis());
                                mainViewModel.feedback(result, new HttpUtil.HttpUtilCallBack<Data>() {
                                    @Override
                                    public void onResponse(Response response1, Data o) {
                                        XToast.success("接单成功");

                                        SecondFragment.notifyDataList2Inserted(result);
                                        MyApplication.getDaoSession().getDataDao().update(result);

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
        data.setRepairDate(System.currentTimeMillis());
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
                XToast.success("反馈成功");

                SecondFragment.notifyDataList3Inserted(data);

                Intent intent = new Intent();
                intent.putExtra("feedback", true);
                intent.putExtra("data", result.toString());
                intent.putExtra("position", getIntent().getIntExtra("position", -1));
                setResult(0, intent);
                finish();
            }

            @Override
            public void onFailure(IOException e) {
                XLoadingDialog.with(DetailsActivity.this).dismiss();
                XToast.error("反馈失败");
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
