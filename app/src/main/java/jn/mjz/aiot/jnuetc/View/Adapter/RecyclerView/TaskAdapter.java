package jn.mjz.aiot.jnuetc.View.Adapter.RecyclerView;

import android.content.Context;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.lcodecore.extextview.ExpandTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.DateUtil;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {


    private boolean enableSelect = true;
    private static final String TAG = "TaskAdapter";
    private List<Data> dataList;
    private Context context;
    private ITaskListener iTaskListener;
    private boolean isSelectMode = false;
    private SparseBooleanArray booleanArray = new SparseBooleanArray();
    private int selectCnt = 0;

    public TaskAdapter(List<Data> dataList, Context context, ITaskListener iTaskListener) {
        this.dataList = dataList;
        this.context = context;
        this.iTaskListener = iTaskListener;
    }


    public TaskAdapter(boolean enableSelect, List<Data> dataList, Context context, ITaskListener iTaskListener) {
        this.enableSelect = enableSelect;
        this.dataList = dataList;
        this.context = context;
        this.iTaskListener = iTaskListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rv_task, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull List<Object> payloads) {
        Data data = dataList.get(position);
        holder.materialCardView.setTag(position);
        if (!payloads.isEmpty() && payloads.get(0).equals("updateState")) {
//            Log.e(TAG, "onBindViewHolder: selectMode");
            updateState(data, holder);
        } else if (!payloads.isEmpty() && payloads.get(0).equals("updateSelect")) {
//            Log.e(TAG, "onBindViewHolder: updateSelect");
            updateSelect(position, holder);
        } else if (!payloads.isEmpty() && payloads.get(0).equals("selectMode")) {
//            Log.e(TAG, "onBindViewHolder: selectMode");
            selectMode(position, holder);
        } else if (!payloads.isEmpty() && payloads.get(0).equals("quitSelectMode")) {
//            Log.e(TAG, "onBindViewHolder: quitSelectMode");
            quitSelectMode(position, holder);
        } else {
            holder.textViewLocationAndId.setText(String.format("%s - %s", data.getLocal(), String.valueOf(data.getId())));
            holder.textViewDateAndName.setText(String.format("%s %s", DateUtil.getDateAndTime(data.getDate(), " "), data.getName()));
            holder.textViewModel.setText(String.format("型号：%s", data.getModel()));
            holder.expandTextViewMessage.setText(String.format("故障详情：%s", data.getMessage()));
            updateState(data, holder);
            if (isSelectMode) {
                selectMode(position, holder);
                updateSelect(position, holder);
            } else {
                quitSelectMode(position, holder);
            }
            holder.materialCardView.setOnClickListener(view -> {
                if (!isSelectMode) {
                    iTaskListener.OnItemClick(position, data);
                } else {
                    //防止错位
                    int pos = (int) holder.materialCardView.getTag();
//                    Log.e("SecondFragment", "onBindViewHolder: tag = " + pos);
//                    Log.e("SecondFragment", "onBindViewHolder: position = " + position);
                    boolean b = booleanArray.get(pos, false);
                    booleanArray.put(pos, !b);
                    updateSelect(pos, holder);
                    iTaskListener.OnSelect(b ? --selectCnt : ++selectCnt);
                }
            });
            holder.textViewConfirm.setOnClickListener(view -> iTaskListener.OnConfirmClick(position, data));

            holder.materialCardView.setOnLongClickListener(view -> {
                if (enableSelect) {
                    isSelectMode = !isSelectMode;
                    if (isSelectMode) {
                        notifyItemRangeChanged(0, dataList.size(), "updateSelect");
                        notifyItemRangeChanged(0, dataList.size(), "selectMode");
                        iTaskListener.OnStartSelect(selectCnt);
//                    Log.e("SecondFragment", "onBindViewHolder: " + booleanArray);
                    } else {
                        notifyItemRangeChanged(0, dataList.size(), "quitSelectMode");
                        iTaskListener.OnCancelSelect();
                    }
                }
                return true;
            });
        }

    }

    private void quitSelectMode(int position, TaskViewHolder holder) {
//        Log.e(TAG, "quitSelectMode: " + holder.materialCardView.getTag());
        switch (dataList.get(position).getState()) {
            case 0:
                holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_new_left);
                break;
            case 1:
                holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_processing_left);
                break;
            case 2:
                holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_done_left);
                break;
        }
    }

    private void selectMode(int position, TaskViewHolder holder) {
//        Log.e(TAG, "selectMode: " + holder.materialCardView.getTag());
        if (!booleanArray.get(position, false)) {
            holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_unselect);
//            switch (dataList.get(position).getState()) {
//                case 0:
//                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_new_unselect);
//                    break;
//                case 1:
//                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_processing_unselect);
//                    break;
//                case 2:
//                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_done_unselect);
//                    break;
//            }
        }
    }

    private void updateSelect(int position, TaskViewHolder holder) {
//        Log.e(TAG, "updateSelect: " + holder.materialCardView.getTag());
        if (!booleanArray.get(position, false)) {
            holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_unselect);
//            switch (dataList.get(position).getState()) {
//                case 0:
//                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_new_unselect);
//                    break;
//                case 1:
//                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_processing_unselect);
//                    break;
//                case 2:
//                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_done_unselect);
//                    break;
//            }
        } else {
            switch (dataList.get(position).getState()) {
                case 0:
                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_new_all);
                    break;
                case 1:
                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_processing_all);
                    break;
                case 2:
                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_done_all);
                    break;
            }
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.mcv_item)
        MaterialCardView materialCardView;

        @BindView(R.id.tv_item_location_and_id)
        TextView textViewLocationAndId;

        @BindView(R.id.tv_item_date_and_name)
        TextView textViewDateAndName;

        @BindView(R.id.tv_item_model)
        TextView textViewModel;

        @BindView(R.id.etv_item_message)
        ExpandTextView expandTextViewMessage;

        @BindView(R.id.tv_item_state)
        TextView textViewState;

        @BindView(R.id.relativeLayout_item_state)
        RelativeLayout relativeLayoutState;

        @BindView(R.id.tv_item_confirm)
        TextView textViewConfirm;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private void updateState(Data data, TaskViewHolder holder) {
        switch (data.getState()) {
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_new_left);
                    holder.textViewConfirm.setTextColor(context.getColor(R.color.colorPrimary));
                } else {
                    holder.textViewConfirm.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                }
                holder.textViewState.setText("未处理");
                holder.textViewConfirm.setEnabled(true);
                break;
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_processing_left);
                    holder.textViewConfirm.setTextColor(context.getColor(R.color.SubText));
                } else {
                    holder.textViewConfirm.setTextColor(context.getResources().getColor(R.color.SubText));
                }
                holder.textViewState.setText(String.format("%s 处理中", data.getRepairer()));
                holder.textViewConfirm.setEnabled(false);
                break;
            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_done_left);
                    holder.textViewConfirm.setTextColor(context.getColor(R.color.SubText));
                } else {
                    holder.textViewConfirm.setTextColor(context.getResources().getColor(R.color.SubText));
                }
                holder.textViewState.setText(String.format("%s 已维修\n反馈时间：%s", data.getRepairer(), DateUtil.getDateAndTime(data.getRepairDate(), " - ")));
                holder.textViewConfirm.setEnabled(false);
                break;
        }
    }

    public interface ITaskListener {
        void OnItemClick(int position, Data data);

        void OnStartSelect(int count);

        void OnSelect(int count);

        void OnConfirmSelect(SparseBooleanArray sparseBooleanArray);

        void OnCancelSelect();

        void OnConfirmClick(int position, Data data);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void finishSelect() {
        iTaskListener.OnConfirmSelect(booleanArray);
    }

    public void cancelSelect() {
        isSelectMode = false;
        notifyItemRangeChanged(0, dataList.size(), "quitSelectMode");
        iTaskListener.OnCancelSelect();
    }

    public void clearSelect() {
        booleanArray.clear();
        selectCnt = 0;
    }

    public void deleteSelect(int position) {
        if (booleanArray.get(position, false)) {
            booleanArray.delete(position);
            selectCnt--;
        }
        for (int i = 0; i < booleanArray.size(); i++) {
            int key = booleanArray.keyAt(i);
            if (key > position) {
                if (booleanArray.get(key, false)) {
                    booleanArray.put(key, false);
                    booleanArray.put(key - 1, true);
                }
            }
        }
        if (isSelectMode) {//如果在选择模式下删除，则刷新数据
            iTaskListener.OnStartSelect(selectCnt);
        }
    }

    public void insertSelect() {
        for (int i = booleanArray.size() - 1; i >= 0; i--) {
            if (booleanArray.get(i, false)) {
                int key = booleanArray.keyAt(i);
                booleanArray.put(key + 1, true);
                booleanArray.put(key, false);
            }
        }
    }

    public boolean isSelectMode() {
        return isSelectMode;
    }

    public boolean selectNone() {
        for (int i = 0; i < booleanArray.size(); i++) {
            int key = booleanArray.keyAt(i);
            if (booleanArray.get(key)) {
                return false;
            }
        }
        return true;
    }

}
