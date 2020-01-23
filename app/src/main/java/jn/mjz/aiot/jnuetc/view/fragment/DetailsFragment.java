package jn.mjz.aiot.jnuetc.view.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.youth.xframe.utils.permission.XPermission;
import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.databinding.FragmentDetailsBinding;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.viewmodel.DetailsViewModel;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author 19622
 */
public class DetailsFragment extends Fragment {
    private static final String DATA_JSON = "dataJson";

    private String dataJson;
    private DetailsViewModel detailsViewModel;
    private OnFragmentInteractionListener mListener;

    public DetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailsFragment.
     */
    public static DetailsFragment newInstance(String dataJson) {
        DetailsFragment detailsFragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(DATA_JSON, dataJson);
        detailsFragment.setArguments(args);
        return detailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataJson = getArguments().getString(DATA_JSON);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on(Object o) {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DATA_JSON, dataJson);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDetailsBinding fragmentDetailsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_details, container, false);
        detailsViewModel = ViewModelProviders.of(getActivity()).get(DetailsViewModel.class);
        detailsViewModel.getModifyMode().observe(this, fragmentDetailsBinding::setModifyMode);
        detailsViewModel.getData().observe(this, data -> {
            if (data != null) {
                fragmentDetailsBinding.setData(data);
            }
        });
        fragmentDetailsBinding.setOnLocalSelectListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Data data = fragmentDetailsBinding.getData();
                if (data != null) {
                    data.setLocal(
                            Data.getLocals().get(position)
                    );
                    detailsViewModel.getData().setValue(
                            detailsViewModel.getData().getValue()
                    );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        fragmentDetailsBinding.setOnCollegeSelectListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Data data = fragmentDetailsBinding.getData();
                if (data != null) {
                    data.setCollege(
                            Data.getColleges().get(position)
                    );
                    detailsViewModel.getData().setValue(
                            detailsViewModel.getData().getValue()
                    );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        fragmentDetailsBinding.setOnGradeSelectListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Data data = fragmentDetailsBinding.getData();
                if (data != null) {
                    data.setGrade(
                            Data.getGrades().get(position)
                    );
                    detailsViewModel.getData().setValue(
                            detailsViewModel.getData().getValue()
                    );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        fragmentDetailsBinding.setOnItemClickListener(position -> {
            if (mListener != null) {
                mListener.onItemClick(position);
            }
        });
        fragmentDetailsBinding.setModelTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                detailsViewModel.getData().getValue().setModel(s.toString());
                detailsViewModel.getData().setValue(
                        detailsViewModel.getData().getValue()
                );
            }
        });
        fragmentDetailsBinding.setMessageTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                detailsViewModel.getData().getValue().setMessage(s.toString());
                detailsViewModel.getData().setValue(
                        detailsViewModel.getData().getValue()
                );
            }
        });
        fragmentDetailsBinding.setNameTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                detailsViewModel.getData().getValue().setName(s.toString());
                detailsViewModel.getData().setValue(
                        detailsViewModel.getData().getValue()
                );
            }
        });
        fragmentDetailsBinding.setTelTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                detailsViewModel.getData().getValue().setTel(s.toString());
                detailsViewModel.getData().setValue(
                        detailsViewModel.getData().getValue()
                );
            }
        });
        fragmentDetailsBinding.setQqTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                detailsViewModel.getData().getValue().setQq(s.toString());
                detailsViewModel.getData().setValue(
                        detailsViewModel.getData().getValue()
                );
            }
        });
        fragmentDetailsBinding.setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.button_details_tel:
                    XPermission.requestPermissions(getActivity(), 0, new String[]{Manifest.permission.CALL_PHONE}, new XPermission.OnPermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse(String.format(Locale.getDefault(), "tel:%s", detailsViewModel.getData().getValue().getTel())));
                            startActivity(intent);
                        }

                        @Override
                        public void onPermissionDenied() {
                            XPermission.showTipsDialog(getActivity());
                        }
                    });
                    break;
                case R.id.button_details_qq:
                    Data.openQq(detailsViewModel.getData().getValue().getQq());
                    break;
                case R.id.button_details_order:
                    XLoadingDialog.with(getActivity()).setCanceled(false).show();
                    MainViewModel.queryById(String.valueOf(detailsViewModel.getData().getValue().getId()), new HttpUtil.HttpUtilCallBack<Data>() {
                        @Override
                        public void onResponse(Data result) {
                            XLoadingDialog.with(getActivity()).cancel();
                            if (result.getState() == 0) {
                                String dataBackUpString = result.toString();
                                String[] items = {"接单成功后自动复制QQ号并且打开QQ"};
                                boolean[] booleans = {true};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(detailsViewModel.getData().getValue().getLocal() + " - " + detailsViewModel.getData().getValue().getId())
                                        .setCancelable(false)
                                        .setMultiChoiceItems(items, booleans, (dialogInterface, i, b) -> {
                                        })
                                        .setNegativeButton("取消", (dialogInterface, i) -> XLoadingDialog.with(getActivity()).cancel())
                                        .setPositiveButton("接单", (dialogInterface, i) -> {
                                            XLoadingDialog.with(getActivity()).setCanceled(false).show();
                                            result.setOrderDate(System.currentTimeMillis());
                                            result.setRepairer(MainViewModel.user.getUserName());
                                            result.setState((short) 1);
                                            result.modify(dataBackUpString, new HttpUtil.HttpUtilCallBack<Data>() {
                                                @Override
                                                public void onResponse(Data o) {
                                                    XToast.success("接单成功");
                                                    App.getDaoSession().getDataDao().update(o);
                                                    if (booleans[0]) {
                                                        Data.openQq(o.getQq());
                                                    }
                                                    Intent intent = new Intent();
                                                    intent.putExtra("order", true);
                                                    intent.putExtra("data", o.toString());
                                                    intent.putExtra("position", getActivity().getIntent().getIntExtra("position", -1));
                                                    getActivity().setResult(0, intent);
                                                    XLoadingDialog.with(getActivity()).cancel();
                                                    getActivity().finish();
                                                }

                                                @Override
                                                public void onFailure(String error) {
                                                    XLoadingDialog.with(getActivity()).cancel();
                                                    XToast.error("接单失败");
                                                }
                                            });
                                        })
                                        .create()
                                        .show();

                            } else {
                                XToast.warning("唉呀，有人抢先了...\n该报修单已被 " + result.getRepairer() + " 处理");
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            XToast.error("接单失败\n" + error);
                            XLoadingDialog.with(getActivity()).cancel();
                        }
                    });

                    break;
                default:
            }
        });
        return fragmentDetailsBinding.getRoot();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        EventBus.getDefault().unregister(this);
    }

    public interface OnFragmentInteractionListener {

        /**
         * 点击轮播图
         *
         * @param position 哪一个
         */
        void onItemClick(int position);
    }
}
