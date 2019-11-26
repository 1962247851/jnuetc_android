package jn.mjz.aiot.jnuetc.view.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bm.library.Info;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.utils.XEmptyUtils;
import com.youth.xframe.utils.XKeyboardUtils;
import com.youth.xframe.utils.permission.XPermission;
import com.youth.xframe.utils.statusbar.XStatusBar;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.MyApplication;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.util.DateUtil;
import jn.mjz.aiot.jnuetc.util.GlobalUtil;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.util.SoftKeyBoardListener;
import jn.mjz.aiot.jnuetc.view.adapter.RecyclerView.PhotoAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.ViewPager.PhotoViewAdapter;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;
import okhttp3.Response;

/**
 * @author 19622
 */
public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DetailsActivity";

    // TODO: 2019/11/21 传进来的报修单没有反馈信息，判断的时候会判断
    private Data data;
    private MainViewModel mainViewModel;
    private PhotoViewAdapter photoViewAdapter;
    private PhotoViewAdapter.IPhotoViewPagerListener iPhotoViewPagerListener;
    private PhotoAdapter photoAdapter;
    private String dataStringBackup;
    private boolean modifyMode = false;

    @BindView(R.id.viewPager_details)
    ViewPager viewPager;
    @BindView(R.id.recyclerView_details_photo)
    RecyclerView recyclerViewPhoto;

    @BindView(R.id.button_details_tel)
    ImageButton buttonTel;
    @BindView(R.id.button_details_qq)
    ImageButton buttonQq;

    @BindView(R.id.toolbar_details)
    Toolbar toolbar;
    @BindView(R.id.button_details_feedback)
    Button buttonFeedback;
    @BindView(R.id.button_details_confirm)
    Button buttonConfirm;

    @BindView(R.id.fab_details_modify_done)
    FloatingActionButton fabDone;
    @BindView(R.id.textView_details_title_photo)
    TextView textViewTitlePhoto;
    @BindView(R.id.spinner_location)
    Spinner spinnerLocation;
    @BindView(R.id.spinner_details_college)
    Spinner spinnerCollege;
    @BindView(R.id.spinner_details_grade)
    Spinner spinnerGrade;
    @BindView(R.id.textView_details_date)
    TextView textViewDate;
    @BindView(R.id.tidt_details_name)
    TextInputEditText textViewName;
    @BindView(R.id.tidt_details_tel)
    TextInputEditText textViewTel;
    @BindView(R.id.tidt_details_qq)
    TextInputEditText textViewQq;
    @BindView(R.id.tidt_details_model)
    TextInputEditText textViewModel;
    @BindView(R.id.tidt_details_message)
    TextInputEditText textViewMessage;

    @BindView(R.id.textView_details_feedback)
    TextView textViewFeedback;

    @BindView(R.id.tidt_details_repairer)
    TextInputEditText textInputEditTextRepairer;
    @BindView(R.id.textView_details_repairDate)
    TextView textViewRepairDate;
    @BindView(R.id.textView_details_repairDate_title)
    TextView textViewRepairDateTitle;
    @BindView(R.id.tidt_details_repair_message)
    TextInputEditText textInputEditTextRepairMessage;

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
        mainViewModel = ViewModelProviders.of(DetailsActivity.this).get(MainViewModel.class);
        ButterKnife.bind(this);
        dataStringBackup = getIntent().getStringExtra("data");
        data = GsonUtil.getInstance().fromJson(dataStringBackup, Data.class);
        data = MyApplication.getDaoSession().getDataDao().queryBuilder().where(DataDao.Properties.Uuid.eq(data.getUuid())).unique();
        toolbar.setTitle(String.format(Locale.getDefault(), "%s - %d", data.getLocal(), data.getId()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        InitListener();

        ArrayAdapter<String> adapterMark = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, GlobalUtil.MARKS);
        ArrayAdapter<String> adapterService = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, GlobalUtil.SERVICES);
        ArrayAdapter<String> adapterLocation = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, GlobalUtil.LOCATIONS);
        ArrayAdapter<String> adapterCollege = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, GlobalUtil.COLLEGES);
        ArrayAdapter<String> adapterGrade = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, GlobalUtil.GRADES);
        spinnerMark.setAdapter(adapterMark);
        spinnerService.setAdapter(adapterService);
        spinnerLocation.setAdapter(adapterLocation);
        spinnerCollege.setAdapter(adapterCollege);
        spinnerGrade.setAdapter(adapterGrade);
        spinnerLocation.setEnabled(false);
        spinnerCollege.setEnabled(false);
        spinnerGrade.setEnabled(false);

        updateUI(data);

        String photo = data.getPhoto();
        if (photo != null && !photo.isEmpty()) {
            String[] photos = photo.split("。");
            String[] photoUrls = new String[photos.length];
            for (int i = 0; i < photos.length; i++) {
                photoUrls[i] = String.format("%s?url=/opt/dataDP&fileName=%s%s", GlobalUtil.URLS.FILE.DOWNLOAD, data.getUuid(), photos[i]);
            }

            photoAdapter = new PhotoAdapter(this, photoUrls, new PhotoAdapter.IPhotoRecyclerViewListener() {
                @Override
                public void OnPhotoClick(int position, Info info) {
                    XKeyboardUtils.closeKeyboard(DetailsActivity.this);
                    // TODO: 2019/9/23 展示ViewPager，要有动画
                    getSupportActionBar().hide();
                    if (modifyMode) {
                        fabDone.hide();
                    }
                    viewPager.setCurrentItem(position, false);
                    viewPager.setVisibility(View.VISIBLE);
//                    photoViewAdapter.setmRectF(info);
                }
            });

            recyclerViewPhoto.setLayoutManager(new GridLayoutManager(this, 3));
            recyclerViewPhoto.setAdapter(photoAdapter);

            iPhotoViewPagerListener = new PhotoViewAdapter.IPhotoViewPagerListener() {
                @Override
                public void OnDismiss() {
                    // TODO: 2019/11/23 增加消失动画
                    getSupportActionBar().show();
                    if (modifyMode) {
                        fabDone.show();
                    }
                    viewPager.setVisibility(View.GONE);
                }
            };

            photoViewAdapter = new PhotoViewAdapter(photoUrls, DetailsActivity.this, iPhotoViewPagerListener);

            viewPager.setAdapter(photoViewAdapter);
            viewPager.setOffscreenPageLimit(photoViewAdapter.getCount());

        } else {
            textViewTitlePhoto.setVisibility(View.GONE);
            recyclerViewPhoto.setVisibility(View.GONE);
        }


        switch (data.getState()) {
            //未处理，只可以接单
            case 0:
                buttonConfirm.setVisibility(View.VISIBLE);
                break;
            //处理中，只有自己参与的单子才可以可填写反馈，否则也只能查看
            case 1:
                if (data.getRepairer() != null && data.getRepairer().contains(MainViewModel.user.getName())) {
                    linearLayoutFeedback.setVisibility(View.VISIBLE);
                    textInputEditTextRepairer.setEnabled(true);
                    textInputEditTextRepairer.setText(data.getRepairer());
                }
                break;
            //已维修，除了管理员外，所有内容均不可修改
            case 2:
                //管理员或者与自己有关可修改报修单
                if (MainViewModel.user.haveModifyAccess() || MainViewModel.user.haveRelationWithData(data)) {
                    textInputEditTextRepairer.setEnabled(true);
                    textInputEditTextRepairer.setFocusable(false);
                    textInputEditTextRepairer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            XLoadingDialog.with(DetailsActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
                            mainViewModel.queryAllUser(new HttpUtil.HttpUtilCallBack<List<String>>() {
                                @Override
                                public void onResponse(Response response, List<String> result) {
                                    XLoadingDialog.with(DetailsActivity.this).dismiss();
                                    String[] names = new String[result.size()];
                                    result.toArray(names);
                                    boolean[] checkItems = new boolean[result.size()];
                                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                                    builder.setTitle("请选择一个或多个维修人")
                                            .setMultiChoiceItems(names, checkItems, new DialogInterface.OnMultiChoiceClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                                                }
                                            })
                                            .setPositiveButton("确定", null)
                                            .setNeutralButton("取消", null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
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
                                                //删除最后一个"，"
                                                names.deleteCharAt(names.length() - 1);
                                                String repairer = names.toString();
                                                if (!repairer.isEmpty()) {
                                                    data.setRepairer(repairer);
                                                    data.setOrderDate(data.getOrderDate());
                                                    data.setRepairDate(data.getRepairDate());
                                                    dialog.dismiss();
                                                    modify();
                                                } else {
                                                    XToast.info("请检查维修人");
                                                }
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(IOException e) {
                                    XLoadingDialog.with(DetailsActivity.this).cancel();
                                }
                            });
                        }
                    });
                }
                linearLayoutFeedback.setVisibility(View.VISIBLE);
                buttonFeedback.setVisibility(View.GONE);

                spinnerMark.setEnabled(false);
                spinnerService.setEnabled(false);
                textInputEditTextRepairMessage.setEnabled(false);

                textViewFeedback.setText("查看反馈");

                textViewRepairDateTitle.setVisibility(View.VISIBLE);
                textViewRepairDate.setVisibility(View.VISIBLE);
                textViewRepairDate.setText(DateUtil.getDateAndTime(data.getRepairDate(), " "));

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
            default:
        }

        XStatusBar.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));

    }

    private void InitListener() {

        textViewName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.setName(s.toString());
            }
        });
        textViewTel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.setTel(s.toString());
            }
        });
        textViewQq.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.setQq(s.toString());
            }
        });
        textViewModel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.setModel(s.toString());
            }
        });
        textViewMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.setMessage(s.toString());
            }
        });
        textInputEditTextRepairer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.setRepairer(s.toString());
            }
        });
        textInputEditTextRepairMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.setRepairMessage(s.toString());
            }
        });
        spinnerMark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.setMark(GlobalUtil.MARKS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.setService(GlobalUtil.SERVICES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.setLocal(GlobalUtil.LOCATIONS[position]);
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "%s - %d", data.getLocal(), data.getId()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerCollege.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.setCollege(GlobalUtil.COLLEGES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.setGrade(GlobalUtil.GRADES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        textViewDate.setOnClickListener(this);
        textViewRepairDate.setOnClickListener(this);
        fabDone.setOnClickListener(this);
        buttonFeedback.setOnClickListener(this);
        buttonConfirm.setOnClickListener(this);
        buttonTel.setOnClickListener(this);
        buttonQq.setOnClickListener(this);

        SoftKeyBoardListener.setListener(DetailsActivity.this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardHide(int height) {
                if (modifyMode) {
                    if (viewPager.getVisibility() == View.GONE) {
                        fabDone.show();
                    }
                }
            }

            @Override
            public void keyBoardShow(int height) {
                if (modifyMode) {
                    fabDone.hide();
                }
            }
        });
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
            case R.id.fab_details_modify_done:
                modify(data);
                break;
            case R.id.textView_details_date:
                if (modifyMode) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(data.getDate());

                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    builder.setTitle(textViewDate.getText())
                            .setMessage("选择修改项")
                            .setPositiveButton("时间", (dialog, which) -> {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(DetailsActivity.this, (view12, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    data.setDate(calendar.getTimeInMillis());
                                    textViewDate.setText(DateUtil.getDateAndTime(data.getDate(), " "));
                                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                                timePickerDialog.setCancelable(false);
                                timePickerDialog.show();
                            })
                            .setNegativeButton("日期", (dialog, which) -> {
                                DatePickerDialog datePickerDialog = new DatePickerDialog(DetailsActivity.this, (view1, year, month, dayOfMonth) -> {
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                    data.setDate(calendar.getTimeInMillis());
                                    textViewDate.setText(DateUtil.getDateAndTime(data.getDate(), " "));
                                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                                datePickerDialog.setCancelable(false);
                                datePickerDialog.show();
                            })
                            .show();
                }
                break;
            case R.id.textView_details_repairDate:
                if (modifyMode) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(data.getRepairDate());
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    builder.setTitle(textViewRepairDate.getText())
                            .setMessage("选择修改项")
                            .setPositiveButton("时间", (dialog, which) -> {
                                TimePickerDialog timePickerDialog = new TimePickerDialog(DetailsActivity.this, (view12, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    data.setRepairDate(calendar.getTimeInMillis());
                                    textViewRepairDate.setText(DateUtil.getDateAndTime(data.getRepairDate(), " "));
                                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                                timePickerDialog.setCancelable(false);
                                timePickerDialog.show();
                            })
                            .setNegativeButton("日期", (dialog, which) -> {
                                DatePickerDialog datePickerDialog = new DatePickerDialog(DetailsActivity.this, (view1, year, month, dayOfMonth) -> {
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                    data.setRepairDate(calendar.getTimeInMillis());
                                    textViewRepairDate.setText(DateUtil.getDateAndTime(data.getRepairDate(), " "));
                                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                                datePickerDialog.setCancelable(false);
                                datePickerDialog.show();
                            })
                            .show();
                }
                break;
            case R.id.button_details_tel:
                openTel(data.getTel());
                break;
            case R.id.button_details_qq:
                MainViewModel.copyToClipboard(DetailsActivity.this, data.getQq());
                if (XAppUtils.isInstallApp("com.tencent.mobileqq")) {
                    XAppUtils.startApp("com.tencent.mobileqq");
                    XToast.success(String.format("QQ：%s已复制到剪切板", data.getQq()));
                } else {
                    XToast.error("未安装手Q或安装的版本不支持");
                }
                break;
            default:
        }

    }

    private void modify(Data data) {
        if (data.isAllNotEmpty()) {
            if (data.toString().equals(dataStringBackup)) {
                XToast.info("未做任何更改");
                modifyMode = false;
                updateEnable();
            } else {
//            Log.e(TAG, "modify: data.getDate = \n" + data.getDate());
                XLoadingDialog.with(DetailsActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
                mainViewModel.modify(data, new HttpUtil.HttpUtilCallBack<Data>() {
                    @Override
                    public void onResponse(Response response, Data result) {
                        XToast.success("修改成功");
                        DetailsActivity.this.data = result;
                        dataStringBackup = result.toString();
                        updateUI(result);
                        modifyMode = false;
                        updateEnable();
                        Intent intent = new Intent();
                        intent.putExtra("modify", true);
                        intent.putExtra("data", result.toString());
                        intent.putExtra("position", getIntent().getIntExtra("position", -1));
                        setResult(1, intent);
                        XLoadingDialog.with(DetailsActivity.this).dismiss();
                    }

                    @Override
                    public void onFailure(IOException e) {
                        XLoadingDialog.with(DetailsActivity.this).dismiss();
                        XToast.error("修改失败");
                    }
                });
            }
        } else {
            XToast.error("字段不能为空");
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
                result.remove(MainViewModel.user.getName());
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
                        .setPositiveButton("确定", null)
                        .setNeutralButton("取消", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                            if (!repairer.isEmpty() && !repairer.contains(MainViewModel.user.getName())) {
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
                });
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
                    String[] items = {"接单成功后自动复制QQ号并且打开QQ"};
                    boolean[] booleans = {true};
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    builder.setTitle(data.getLocal() + " - " + data.getId())
                            .setCancelable(false)
                            .setMultiChoiceItems(items, booleans, (dialogInterface, i, b) -> {
                            })
                            .setNegativeButton("取消", (dialogInterface, i) -> XLoadingDialog.with(DetailsActivity.this).cancel())
                            .setPositiveButton("确定", (dialogInterface, i) -> {
                                XLoadingDialog.with(DetailsActivity.this).setMessage("请求处理中，请稍后").setCanceled(false).show();
                                result.setRepairer(MainViewModel.user.getName());
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
                                            MainViewModel.copyToClipboard(DetailsActivity.this, result.getQq());
                                            if (XAppUtils.isInstallApp("com.tencent.mobileqq")) {
                                                XAppUtils.startApp("com.tencent.mobileqq");
                                                XToast.success(String.format("QQ：%s已复制到剪切板", data.getQq()));
                                            } else {
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

        if (!data.isAllNotEmpty()) {
            XToast.error("字段不能为空");
        } else if (!data.getRepairer().contains(MainViewModel.user.getName())) {
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

    // TODO: 2019/11/10 修改接口，返回到上一级后更新数据
    private void modify() {
        XLoadingDialog.with(DetailsActivity.this).setCanceled(false).setMessage("请求处理中，请稍后").show();
        mainViewModel.feedback(data, new HttpUtil.HttpUtilCallBack<Data>() {

            @Override
            public void onResponse(Response response, Data result) {
                XLoadingDialog.with(DetailsActivity.this).dismiss();
                if (result != null) {
                    XToast.success("更新成功");
                    textInputEditTextRepairer.setText(data.getRepairer());
                } else {
                    XToast.error("更新失败");
                }
            }

            @Override
            public void onFailure(IOException e) {
                XLoadingDialog.with(DetailsActivity.this).dismiss();
                XToast.error("更新失败");
            }
        });
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
        switch (data.getState()) {
            case 0:
                if (MainViewModel.user.haveModifyAccess()) {
                    menu.findItem(R.id.menu_details_modify).setVisible(true);
                }
                break;
            case 1:
            case 2:
                if (MainViewModel.user.haveRelationWithData(data) || MainViewModel.user.haveModifyAccess()) {
                    menu.findItem(R.id.menu_details_modify).setVisible(true);
                }
                break;
            default:
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
                preFinish();
                return true;
            case R.id.menu_details_modify:
                if (modifyMode) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    builder.setTitle("退出编辑模式？")
                            .setMessage("更改将不会保存")
                            .setPositiveButton("取消", null)
                            .setNegativeButton("退出", (dialog, which) -> {
                                data = GsonUtil.getInstance().fromJson(dataStringBackup, Data.class);
                                updateUI(data);
                                modifyMode = false;
                                updateEnable();
                            });
                    builder.create().show();
                } else {
                    modifyMode = true;
                    updateEnable();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void updateEnable() {

        spinnerLocation.setEnabled(modifyMode);
        textViewName.setEnabled(modifyMode);
        textViewDate.setEnabled(modifyMode);
        textViewTel.setEnabled(modifyMode);
        textViewQq.setEnabled(modifyMode);
        spinnerCollege.setEnabled(modifyMode);
        spinnerGrade.setEnabled(modifyMode);
        textViewModel.setEnabled(modifyMode);
        textViewMessage.setEnabled(modifyMode);

        buttonQq.setVisibility(modifyMode ? View.GONE : View.VISIBLE);
        buttonTel.setVisibility(modifyMode ? View.GONE : View.VISIBLE);

        if (data.getState() == 0 && XEmptyUtils.isEmpty(data.getRepairer())) {
            buttonConfirm.setVisibility(modifyMode ? View.GONE : View.VISIBLE);
        }
        if (data.getState() == 1 && MainViewModel.user.haveRelationWithData(data)) {
            linearLayoutFeedback.setVisibility(modifyMode ? View.GONE : View.VISIBLE);
        }
        if (data.getState() == 2) {
            textInputEditTextRepairMessage.setEnabled(modifyMode);
            textViewRepairDate.setEnabled(modifyMode);
            spinnerMark.setEnabled(modifyMode);
            spinnerService.setEnabled(modifyMode);
            if (MainViewModel.user.haveRelationWithData(data)) {
                buttonFeedback.setEnabled(modifyMode);
            }

            if (modifyMode) {
                textViewRepairDate.setTextColor(XFrame.getColor(R.color.MainText));
            } else {
                textViewRepairDate.setTextColor(XFrame.getColor(R.color.SubText));
            }
        }
        if (modifyMode) {
            textViewDate.setTextColor(XFrame.getColor(R.color.MainText));
            fabDone.show();
        } else {
            textViewDate.setTextColor(XFrame.getColor(R.color.SubText));
            fabDone.hide();
        }
    }

    private void preFinish() {
//        if (imageViewPhoto2.getVisibility() == View.VISIBLE) {
//            imageViewPhoto2.animaTo(mRectF, new Runnable() {
//                @Override
//                public void run() {
//                    imageViewPhoto2.setVisibility(View.GONE);
//                    imageViewPhoto1.setVisibility(View.VISIBLE);
//                }
//            });
//        } else {
//            super.onBackPressed();
//        }
        if (viewPager.getVisibility() == View.VISIBLE) {
//            Animation animation = AnimationUtils.loadAnimation(DetailsActivity.this,R.anim.detail_photos);
//            animation.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    viewPager.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
//            viewPager.startAnimation(animation);
            viewPager.setVisibility(View.GONE);
            getSupportActionBar().show();
            if (modifyMode) {
                fabDone.show();
            }
        } else {
            if (modifyMode) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                builder.setTitle("退出编辑模式？")
                        .setMessage("更改将不会保存")
                        .setPositiveButton("取消", null)
                        .setNegativeButton("退出", (dialog, which) -> {
                            data = GsonUtil.getInstance().fromJson(dataStringBackup, Data.class);
                            updateUI(data);
                            modifyMode = false;
                            updateEnable();
                        });
                builder.create().show();
            } else {
//                Log.e(TAG, "preFinish: dataStringBackup=\n" + dataStringBackup);
//                Log.e(TAG, "preFinish: data=\n" + data);
                if (!dataStringBackup.equals(data.toString())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    builder.setTitle("舍弃更改？")
                            .setNegativeButton("舍弃", (dialog, which) -> finish())
                            .setPositiveButton("取消", null);
                    builder.create().show();
                } else {
                    finish();
                }
            }
        }
    }


    private void updateUI(Data data) {
        for (int i = 0; i < GlobalUtil.LOCATIONS.length; i++) {
            if (data.getLocal().equals(GlobalUtil.LOCATIONS[i])) {
                spinnerLocation.setSelection(i);
                getSupportActionBar().setTitle(String.format(Locale.getDefault(), "%s - %d", data.getLocal(), data.getId()));
                break;
            }
        }
        for (int i = 0; i < GlobalUtil.COLLEGES.length; i++) {
            if (data.getCollege().equals(GlobalUtil.COLLEGES[i])) {
                spinnerCollege.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < GlobalUtil.GRADES.length; i++) {
            if (data.getGrade().equals(GlobalUtil.GRADES[i])) {
                spinnerGrade.setSelection(i);
                break;
            }
        }

        textViewDate.setText(DateUtil.getDateAndTime(data.getDate(), " "));
        textViewName.setText(data.getName());
        textViewTel.setText(String.valueOf(data.getTel()));
        textViewQq.setText(String.valueOf(data.getQq()));
        textViewModel.setText(data.getModel());
        textViewMessage.setText(data.getMessage());
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            preFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
