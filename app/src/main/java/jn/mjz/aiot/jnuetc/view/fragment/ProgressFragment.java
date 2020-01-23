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

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.DataTimeLineAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.WrapContentLinearLayoutManager;
import jn.mjz.aiot.jnuetc.viewmodel.DetailsViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProgressFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author 19622
 */
public class ProgressFragment extends Fragment {

    private DetailsViewModel detailsViewModel;

    private RecyclerView recyclerView;
    private DataTimeLineAdapter dataTimeLineAdapter;

    public ProgressFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProgressFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgressFragment newInstance() {
        return new ProgressFragment();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        detailsViewModel = ViewModelProviders.of(getActivity()).get(DetailsViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerView_progress);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        recyclerView.setAdapter(dataTimeLineAdapter);

        detailsViewModel.getData().observe(this, data -> {
            if (data != null) {
                dataTimeLineAdapter.setDataId(data.getId());
                dataTimeLineAdapter.notifyItemRangeChanged(0, 3);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dataTimeLineAdapter = new DataTimeLineAdapter(getContext());
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

}
