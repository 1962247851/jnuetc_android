package jn.mjz.aiot.jnuetc.View.Adapter.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.xframe.XFrame;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;

public class CheckableAdapter extends RecyclerView.Adapter<CheckableAdapter.CheckableTVHolder> {

    private String[] titles;
    private Map<String, Boolean> selections;
    private IOnCheckedChangeListener i;

    public CheckableAdapter(String[] titles, Map<String, Boolean> selections, IOnCheckedChangeListener i) {
        this.titles = titles;
        this.selections = selections;
        this.i = i;
    }

    @NonNull
    @Override
    public CheckableTVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CheckableTVHolder(LayoutInflater.from(XFrame.getContext()).inflate(R.layout.item_rv_checkable_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CheckableTVHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            holder.textView.setText(titles[position]);
            holder.textView.setChecked(selections.get(titles[position]));
            holder.textView.setOnClickListener(view -> {
                boolean checked = !selections.get(titles[position]);
                selections.put(titles[position], checked);
                holder.textView.setChecked(checked);
                i.OnCheckChanged();
//                Log.d("onBindViewHolder: ", String.valueOf(position) + checked);
            });
        } else if ((int) payloads.get(0) == 0) {
            //更新选择
            holder.textView.setChecked(selections.get(titles[position]));
        }
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    class CheckableTVHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.checkedTextView_item)
        CheckedTextView textView;

        CheckableTVHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull CheckableTVHolder holder, int position) {

    }

    public interface IOnCheckedChangeListener {
        void OnCheckChanged();
    }
}
