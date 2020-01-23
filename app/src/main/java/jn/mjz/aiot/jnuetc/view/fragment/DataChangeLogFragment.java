package jn.mjz.aiot.jnuetc.view.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.greendao.entity.DataChangeLog;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.DataChangeLogAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.WrapContentLinearLayoutManager;
import jn.mjz.aiot.jnuetc.viewmodel.DetailsViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DataChangeLogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataChangeLogFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String DATA_CHANGE_LOG_JSON = "dataChangeLogJson";

    private String dataChangeLogJson;
    private RecyclerView recyclerView;
    private DataChangeLogAdapter dataChangeLogAdapter;
    private DetailsViewModel detailsViewModel;
    private List<DataChangeLog> dataChangeLogs = new ArrayList<>();


    public DataChangeLogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DataChangeLogFragment.
     */
    public static DataChangeLogFragment newInstance() {
        DataChangeLogFragment fragment = new DataChangeLogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dataChangeLogJson = getArguments().getString(DATA_CHANGE_LOG_JSON);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(DATA_CHANGE_LOG_JSON, dataChangeLogJson);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView_data_change_log);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        recyclerView.setAdapter(dataChangeLogAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        detailsViewModel = ViewModelProviders.of(getActivity()).get(DetailsViewModel.class);
        detailsViewModel.getData().observe(this, data -> {
            if (data != null) {
                List<DataChangeLog> changeLogs = data.getDataChangeLogs();
                sortLogByTimeDesc(changeLogs);
                String changeLogsString = GsonUtil.getInstance().toJson(changeLogs);
                if (!changeLogsString.equals(dataChangeLogJson)) {
                    int oldSize = dataChangeLogs.size();
                    dataChangeLogJson = changeLogsString;
                    if (oldSize != 0) {
                        dataChangeLogs.clear();
                        dataChangeLogAdapter.notifyItemRangeRemoved(0, oldSize);
                    }
                    dataChangeLogs.addAll(changeLogs);
                    dataChangeLogAdapter.notifyItemRangeInserted(0, dataChangeLogs.size());
                }
            }
        });
        dataChangeLogAdapter = new DataChangeLogAdapter(getContext(), dataChangeLogs);
        return inflater.inflate(R.layout.fragment_data_change_log, container, false);
    }

    public static void sortLogByTimeDesc(List<DataChangeLog> dataChangeLogs) {
        Collections.sort(dataChangeLogs, (o1, o2) -> (int) (o2.getDate() - o1.getDate()));
    }

}
