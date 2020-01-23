package jn.mjz.aiot.jnuetc.view.adapter.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lcodecore.extextview.ExpandTextView;
import com.youth.xframe.utils.XFormatTimeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.greendao.entity.DataChangeLog;

/**
 * @author qq1962247851
 * @date 2020/1/20 12:19
 */
public class DataChangeLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int NO_DATA = 0;
    private static final int DATA = 1;

    private Context context;
    private List<DataChangeLog> dataChangeLogs;

    public DataChangeLogAdapter(Context context, List<DataChangeLog> dataChangeLogs) {
        this.context = context;
        this.dataChangeLogs = dataChangeLogs;
    }

    @Override
    public int getItemViewType(int position) {
        return dataChangeLogs.isEmpty() ? NO_DATA : DATA;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NO_DATA) {
            return new NoDataChangeLogHolder(
                    LayoutInflater.from(context).inflate(R.layout.adapter_no_data_change_log, parent, false)
            );
        } else {
            return new DataChangeLogHolder(
                    LayoutInflater.from(context).inflate(R.layout.item_rv_data_change_log, parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof DataChangeLogHolder) {
            DataChangeLog dataChangeLog = dataChangeLogs.get(position);
            DataChangeLogHolder holder = (DataChangeLogHolder) viewHolder;
            holder.textViewName.setText(dataChangeLog.getName());
            holder.textViewTime.setText(XFormatTimeUtils.getTimeSpanByNow1(dataChangeLog.getDate()));
            holder.expandTextView.setText(dataChangeLog.getChangeInfo());
        }
    }

    @Override
    public int getItemCount() {
        return dataChangeLogs.isEmpty() ? 1 : dataChangeLogs.size();
    }

    class DataChangeLogHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textView_item_change_log_content)
        ExpandTextView expandTextView;
        @BindView(R.id.textView_item_change_log_time)
        TextView textViewTime;
        @BindView(R.id.textView_item_change_log_name)
        TextView textViewName;

        DataChangeLogHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class NoDataChangeLogHolder extends RecyclerView.ViewHolder {

        NoDataChangeLogHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
