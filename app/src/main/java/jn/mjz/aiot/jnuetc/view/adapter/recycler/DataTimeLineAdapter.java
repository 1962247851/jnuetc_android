package jn.mjz.aiot.jnuetc.view.adapter.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.xframe.XFrame;
import com.youth.xframe.utils.XDateUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.util.HttpUtil;

/**
 * @author qq1962247851
 * @date 2020/1/20 9:58
 */
public class DataTimeLineAdapter extends RecyclerView.Adapter<DataTimeLineAdapter.DataTimeLineHolder> {

    private static final int FIRST = 0;
    private static final int BODY = 1;
    private static final int LAST = 2;

    private Context context;
    private long dataId;

    public DataTimeLineAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public DataTimeLineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataTimeLineHolder(
                LayoutInflater.from(context).inflate(R.layout.item_rv_data_time_line, parent, false)
        );
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public void setDataId(long dataId) {
        this.dataId = dataId;
    }

    @Override
    public void onBindViewHolder(@NonNull DataTimeLineHolder holder, int position) {
        if (dataId != -1) {
            Data data = App.getDaoSession().getDataDao().queryBuilder().where(DataDao.Properties.Id.eq(dataId)).build().unique();
            if (data == null) {
                Data.update(dataId, new HttpUtil.HttpUtilCallBack<Data>() {
                    @Override
                    public void onResponse(Data result) {
                        updateUi(result, position, holder);
                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });
            } else {
                updateUi(data, position, holder);
            }
        }
    }

    private void updateUi(Data data, int position, @NonNull DataTimeLineHolder holder) {
        if (getItemViewType(position) == LAST) {
            holder.lineTop.setVisibility(View.INVISIBLE);
        } else {
            holder.lineTop.setVisibility(View.VISIBLE);
        }
        switch (data.getState()) {
            case 0:
                switch (position) {
                    case 0:
                        holder.lineBottom.setVisibility(View.VISIBLE);
                        holder.lineTop.setVisibility(View.INVISIBLE);
                        holder.textViewState.setText("待处理");
                        holder.imageView.setBackground(XFrame.getDrawable(R.drawable.ic_done_accent));
                        holder.lineBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.textViewContent.setText("报修单创建，等待接单");
                        holder.textViewTime.setText(XDateUtils.millis2String(data.getDate()));
                        break;
                    case 1:
                        holder.lineBottom.setVisibility(View.VISIBLE);
                        holder.lineTop.setVisibility(View.VISIBLE);
                        holder.textViewState.setText("处理中");
                        holder.imageView.setBackground(XFrame.getDrawable(R.drawable.ic_more_gray));
                        holder.lineTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.lineBottom.setBackgroundColor(XFrame.getColor(R.color.colorGray));
                        holder.textViewContent.setText("");
                        holder.textViewTime.setText("");
                        break;
                    default:
                        holder.lineBottom.setVisibility(View.INVISIBLE);
                        holder.lineTop.setVisibility(View.VISIBLE);
                        holder.textViewState.setText("已维修");
                        holder.imageView.setBackground(XFrame.getDrawable(R.drawable.ic_more_gray));
                        holder.lineTop.setBackgroundColor(XFrame.getColor(R.color.colorGray));
                        holder.textViewContent.setText("");
                        holder.textViewTime.setText("");
                }
                break;
            case 1:
                switch (position) {
                    case 0:
                        holder.lineBottom.setVisibility(View.VISIBLE);
                        holder.lineTop.setVisibility(View.INVISIBLE);
                        holder.textViewState.setText("待处理");
                        holder.imageView.setBackground(XFrame.getDrawable(R.drawable.ic_done_accent));
                        holder.lineBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.textViewContent.setText("报修单创建，等待接单");
                        holder.textViewTime.setText(XDateUtils.millis2String(data.getDate()));
                        break;
                    case 1:
                        holder.lineBottom.setVisibility(View.VISIBLE);
                        holder.lineTop.setVisibility(View.VISIBLE);
                        holder.textViewState.setText("处理中");
                        holder.imageView.setBackground(XFrame.getDrawable(R.drawable.ic_done_accent));
                        holder.lineTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.lineBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.textViewContent.setText(data.getRepairer() + "正在处理");
                        holder.textViewTime.setText(XDateUtils.millis2String(data.getOrderDate()));
                        break;
                    default:
                        holder.lineBottom.setVisibility(View.INVISIBLE);
                        holder.lineTop.setVisibility(View.VISIBLE);
                        holder.textViewState.setText("已维修");
                        holder.imageView.setBackground(XFrame.getDrawable(R.drawable.ic_more_gray));
                        holder.lineTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.textViewContent.setText("");
                        holder.textViewTime.setText("");
                }
                break;
            default:
                switch (position) {
                    case 0:
                        holder.lineBottom.setVisibility(View.VISIBLE);
                        holder.lineTop.setVisibility(View.INVISIBLE);
                        holder.textViewState.setText("待处理");
                        holder.imageView.setBackground(XFrame.getDrawable(R.drawable.ic_done_accent));
                        holder.lineBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.textViewContent.setText("报修单创建，等待接单");
                        holder.textViewTime.setText(XDateUtils.millis2String(data.getDate()));
                        break;
                    case 1:
                        holder.lineBottom.setVisibility(View.VISIBLE);
                        holder.lineTop.setVisibility(View.VISIBLE);
                        holder.textViewState.setText("处理中");
                        holder.imageView.setBackground(XFrame.getDrawable(R.drawable.ic_done_accent));
                        holder.lineTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.lineBottom.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.textViewContent.setText(data.getRepairer() + "正在处理");
                        holder.textViewTime.setText(XDateUtils.millis2String(data.getOrderDate()));
                        break;
                    default:
                        holder.lineBottom.setVisibility(View.INVISIBLE);
                        holder.lineTop.setVisibility(View.VISIBLE);
                        holder.textViewState.setText("已维修");
                        holder.imageView.setBackground(XFrame.getDrawable(R.drawable.ic_done_accent));
                        holder.lineTop.setBackgroundColor(XFrame.getColor(R.color.colorAccent));
                        holder.textViewContent.setText(data.getRepairer() + "已完成维修");
                        holder.textViewTime.setText(XDateUtils.millis2String(data.getRepairDate()));
                }
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    class DataTimeLineHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.viewTop)
        View lineTop;
        @BindView(R.id.viewBottom)
        View lineBottom;
        @BindView(R.id.imageView_item_state)
        ImageView imageView;
        @BindView(R.id.textView_item_state)
        TextView textViewState;
        @BindView(R.id.textView_item_content)
        TextView textViewContent;
        @BindView(R.id.textView_item_time)
        TextView textViewTime;

        DataTimeLineHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
