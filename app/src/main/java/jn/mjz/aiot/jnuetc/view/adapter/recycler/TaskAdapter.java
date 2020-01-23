package jn.mjz.aiot.jnuetc.view.adapter.recycler;

import android.content.Context;
import android.graphics.Color;
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
import com.youth.xframe.XFrame;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.util.DateUtil;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;


/**
 * @author 19622
 */
public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final Integer NO_DATA = 0;
    private static final Integer DATA = 1;

    private static final String TAG = "TaskAdapter";
    private List<Data> dataList;
    private Context context;
    private ITaskListener iTaskListener;
    private boolean isSelectMode = false;
    private boolean enableSelect = false;
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NO_DATA) {
            return new NoDataHolder(LayoutInflater.from(context).inflate(R.layout.adapter_no_data, parent, false));
        } else {
            return new TaskViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rv_task, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (viewHolder instanceof TaskViewHolder) {
            TaskViewHolder holder = (TaskViewHolder) viewHolder;
            Data data = dataList.get(position);
            holder.materialCardView.setTag(position);
            if (!payloads.isEmpty() && "updateState".equals(payloads.get(0))) {
//            Log.e(TAG, "onBindViewHolder: selectMode");
                updateState(data, holder);
            } else if (!payloads.isEmpty() && "updateSelect".equals(payloads.get(0))) {
//            Log.e(TAG, "onBindViewHolder: updateSelect");
                updateSelect(position, holder);
            } else if (!payloads.isEmpty() && "selectMode".equals(payloads.get(0))) {
//            Log.e(TAG, "onBindViewHolder: selectMode");
                selectMode(position, holder);
            } else if (!payloads.isEmpty() && "quitSelectMode".equals(payloads.get(0))) {
//            Log.e(TAG, "onBindViewHolder: quitSelectMode");
                quitSelectMode(position, holder);
            } else {
                String locationAndId, dateAndName;
                locationAndId = String.format("%s - %s", data.getLocal(), String.valueOf(data.getId()));
                dateAndName = String.format("%s %s", DateUtil.getDateAndTime(data.getDate(), " "), data.getName());
                holder.textViewLocationAndId.setText(locationAndId);
                holder.textViewDateAndName.setText(dateAndName);
                holder.textViewModel.setText(String.format("型号：%s", data.getModel()));
                holder.expandTextViewMessage.setText(String.format("故障详情%s：%s", data.getPhoto() == null || data.getPhoto().isEmpty() ? "" : "（有图）", data.getMessage()));
                updateState(data, holder);
                if (isSelectMode) {
                    selectMode(position, holder);
                    updateSelect(position, holder);
                } else {
                    quitSelectMode(position, holder);
                }
                holder.materialCardView.setOnClickListener(view -> {
                    if (!isSelectMode) {
                        iTaskListener.onItemClick(position, data);
                    } else {
                        //防止错位
                        int pos = (int) holder.materialCardView.getTag();
//                    Log.e("SecondFragment", "onBindViewHolder: tag = " + pos);
//                    Log.e("SecondFragment", "onBindViewHolder: position = " + position);
                        boolean b = booleanArray.get(pos, false);
                        booleanArray.put(pos, !b);
                        updateSelect(pos, holder);
                        iTaskListener.onSelect(b ? --selectCnt : ++selectCnt);
                    }
                });
                holder.textViewConfirm.setOnClickListener(view -> iTaskListener.onConfirmClick(position, data));
                if (enableSelect) {
                    holder.materialCardView.setOnLongClickListener(view -> {
                        if (enableSelect) {
                            isSelectMode = !isSelectMode;
                            if (isSelectMode) {
                                notifyItemRangeChanged(0, dataList.size(), "updateSelect");
                                notifyItemRangeChanged(0, dataList.size(), "selectMode");
                                iTaskListener.onStartSelect(selectCnt);
//                    Log.e("SecondFragment", "onBindViewHolder: " + booleanArray);
                            } else {
                                notifyItemRangeChanged(0, dataList.size(), "quitSelectMode");
                                iTaskListener.onCancelSelect();
                            }
                        }
                        return true;
                    });
                }
            }
        }
    }

    private void quitSelectMode(int position, TaskViewHolder holder) {
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
            default:
        }
    }

    private void selectMode(int position, TaskViewHolder holder) {
        if (!booleanArray.get(position, false)) {
            holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_unselect);
        }
    }

    private void updateSelect(int position, TaskViewHolder holder) {
        if (!booleanArray.get(position, false)) {
            holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_unselect);
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
                default:
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

    class NoDataHolder extends RecyclerView.ViewHolder {

        NoDataHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private void updateState(Data data, TaskViewHolder holder) {
        String repairer = data.getRepairer();
        switch (data.getState()) {
            case 0:
                holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_new_left);
                holder.textViewConfirm.setEnabled(true);
                holder.textViewState.setText("未处理");
                holder.textViewConfirm.setText("接单");
                holder.textViewConfirm.setTextColor(XFrame.getColor(R.color.colorPrimary));
                break;
            case 1:
                holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_processing_left);
                if (!repairer.equals(MainViewModel.user.getUserName())) {
                    holder.textViewState.setText(String.format("%s 处理中", repairer));
                } else {
                    holder.textViewState.setText("处理中");
                }
                holder.textViewConfirm.setTextColor(Color.GRAY);
                holder.textViewConfirm.setText(String.format("接单时间：%s", DateUtil.getDateAndTime(data.getOrderDate(), " ")));
                break;
            case 2:
                holder.relativeLayoutState.setBackgroundResource(R.drawable.background_task_view_state_done_left);
                if (repairer.equals(MainViewModel.user.getUserName())) {
                    holder.textViewState.setText("已维修");
                } else {
                    holder.textViewState.setText(String.format("%s 已维修", repairer));
                }
                holder.textViewConfirm.setTextColor(Color.GRAY);
                holder.textViewConfirm.setText(String.format("接单时间：%s\n反馈时间：%s", DateUtil.getDateAndTime(data.getOrderDate(), " "), DateUtil.getDateAndTime(data.getRepairDate(), " ")));
                break;
            default:
        }
    }

    public interface ITaskListener {
        /**
         * 点击项目
         *
         * @param position 位置
         * @param data     Data
         */
        void onItemClick(int position, Data data);

        /**
         * 开始多选
         *
         * @param count 选择的数目
         */
        void onStartSelect(int count);

        /**
         * 正在选择
         *
         * @param count 选中的数目
         */
        void onSelect(int count);

        /**
         * 选择完成
         *
         * @param sparseBooleanArray 选中数组
         */
        void onConfirmSelect(SparseBooleanArray sparseBooleanArray);

        /**
         * 取消多选
         */
        void onCancelSelect();

        /**
         * 接单
         *
         * @param position 位置
         * @param data     Data
         */
        void onConfirmClick(int position, Data data);
    }

    @Override
    public int getItemViewType(int position) {
        if (dataList.isEmpty()) {
            return NO_DATA;
        } else {
            return DATA;
        }
    }

    @Override
    public int getItemCount() {
        if (dataList.isEmpty()) {
            return 1;
        }
        return dataList.size();
    }

    public void finishSelect() {
        iTaskListener.onConfirmSelect(booleanArray);
    }

    public void cancelSelect() {
        isSelectMode = false;
        notifyItemRangeChanged(0, dataList.size(), "quitSelectMode");
        iTaskListener.onCancelSelect();
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
        if (isSelectMode) {
            //如果在选择模式下删除，则刷新数据
            iTaskListener.onStartSelect(selectCnt);
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

    public void setEnableSelect(boolean enableSelect) {
        this.enableSelect = enableSelect;
    }

}
