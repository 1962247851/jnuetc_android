package jn.mjz.aiot.jnuetc.view.fragment;


import android.content.DialogInterface;
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

import com.youth.xframe.widget.XLoadingDialog;
import com.youth.xframe.widget.XToast;

import java.util.List;

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.databinding.FragmentFeedbackBinding;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.viewmodel.DetailsViewModel;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedbackFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author 19622
 */
public class FeedbackFragment extends Fragment {
    /**
     * the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
     */
    private static final String DATA_JSON = "dataJsonString";

    private DetailsViewModel detailsViewModel;

    private String dataJsonString;


    public FeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param dataJsonString dataJsonString
     * @return A new instance of fragment FeedbackFragment.
     */
    public static FeedbackFragment newInstance(String dataJsonString) {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        args.putString(DATA_JSON, dataJsonString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataJsonString = getArguments().getString(DATA_JSON);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DATA_JSON, dataJsonString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentFeedbackBinding fragmentFeedbackBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_feedback, container, false);
        detailsViewModel = ViewModelProviders.of(getActivity()).get(DetailsViewModel.class);
        detailsViewModel.getModifyMode().observe(this, fragmentFeedbackBinding::setModifyMode);
        detailsViewModel.getData().observe(this, data -> {
            if (data != null) {
                fragmentFeedbackBinding.setData(data);
            }
        });
        fragmentFeedbackBinding.setOnRepairerClick(v -> changeRepairer());
        fragmentFeedbackBinding.setFeedbackMode(false);
        fragmentFeedbackBinding.setRepairMessageTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                detailsViewModel.getData().getValue().setRepairMessage(s.toString());
                detailsViewModel.getData().setValue(
                        detailsViewModel.getData().getValue()
                );
            }
        });
        fragmentFeedbackBinding.setOnServiceSelectListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Data data = fragmentFeedbackBinding.getData();
                if (data != null) {
                    data.setService(
                            Data.getServices().get(position)
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
        fragmentFeedbackBinding.setOnMarkSelectListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Data data = fragmentFeedbackBinding.getData();
                if (data != null) {
                    data.setMark(
                            Data.getMarks().get(position)
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
        return fragmentFeedbackBinding.getRoot();
    }

    private void changeRepairer() {
        XLoadingDialog.with(getContext()).setCanceled(false).show();
        MainViewModel.queryAllUser(new HttpUtil.HttpUtilCallBack<List<String>>() {
            @Override
            public void onResponse(List<String> result) {
                XLoadingDialog.with(getContext()).cancel();
                String[] names = new String[result.size()];
                result.toArray(names);
                boolean[] checkItems = new boolean[result.size()];
                AlertDialog.Builder repairerBuilder = new AlertDialog.Builder(getContext());
                repairerBuilder.setTitle("请选择一个或多个维修人")
                        .setMultiChoiceItems(names, checkItems, (dialogInterface, i, b) -> {
                        })
                        .setPositiveButton("确定", null)
                        .setNeutralButton("取消", null);
                AlertDialog dialog = repairerBuilder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                    StringBuilder names1 = new StringBuilder();
                    for (int j = 0; j < checkItems.length; j++) {
                        if (checkItems[j]) {
                            names1.append(result.get(j));
                            names1.append("，");
                        }
                    }
                    if (names1.toString().isEmpty()) {
                        XToast.info("未选择");
                    } else {
                        names1.deleteCharAt(names1.length() - 1);
                        String repairer = names1.toString();
                        if (!repairer.isEmpty()) {
                            detailsViewModel.getData().getValue().setRepairer(repairer);
                            dialog.dismiss();
                        } else {
                            XToast.info("请检查维修人");
                        }
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                XLoadingDialog.with(getContext()).cancel();
                XToast.error("获取用户列表失败\n" + error);
            }
        });
    }

}
