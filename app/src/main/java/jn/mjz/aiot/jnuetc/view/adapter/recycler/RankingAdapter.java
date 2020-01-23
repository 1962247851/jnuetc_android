package jn.mjz.aiot.jnuetc.view.adapter.recycler;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.greendao.entity.RankingInfo;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.view.activity.HistoryActivity;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author qq1962247851
 * @date 2020/1/14 16:38
 */
public class RankingAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<RankingInfo> list;
    private static final int NO_DATA = 0;
    private static final int DATA = 1;
    private static final int FOOTER = 2;

    public RankingAdapter(Context context, List<RankingInfo> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        if (list.isEmpty()) {
            return NO_DATA;
        } else {
            return position == list.size() ? FOOTER : DATA;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NO_DATA) {
            return new NoDataViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_no_data, parent, false));
        } else if (viewType == DATA) {
            return new DataViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rv_ranking, parent, false));
        } else {
            return new FooterViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rv_ranking_footer, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            DataViewHolder viewHolder = (DataViewHolder) holder;
            RankingInfo rankingInfo = list.get(position);
            viewHolder.number.setText(String.valueOf(position + 1));
            if (rankingInfo.getUserName().equals(MainViewModel.user.getUserName())) {
                viewHolder.constraintLayout.setBackgroundResource(R.drawable.background_task_view_state_done_all);
            } else {
                viewHolder.constraintLayout.setBackground(null);
            }
            viewHolder.userName.setText(rankingInfo.getUserName());
            viewHolder.count.setText(rankingInfo.getCount() + "单");
            switch (position) {
                case 0:
                    viewHolder.number.setTextColor(Color.rgb(255, 215, 0));
                    viewHolder.userName.setTextColor(Color.rgb(255, 215, 0));
                    viewHolder.count.setTextColor(Color.rgb(255, 215, 0));
                    viewHolder.userName.setTextSize(40);
                    break;
                case 1:
                    viewHolder.number.setTextColor(Color.rgb(185, 185, 185));
                    viewHolder.userName.setTextColor(Color.rgb(185, 185, 185));
                    viewHolder.count.setTextColor(Color.rgb(185, 185, 185));
                    viewHolder.userName.setTextSize(35);
                    break;
                case 2:
                    viewHolder.number.setTextColor(Color.rgb(184, 115, 51));
                    viewHolder.userName.setTextColor(Color.rgb(184, 115, 51));
                    viewHolder.count.setTextColor(Color.rgb(184, 115, 51));
                    viewHolder.userName.setTextSize(30);
                    break;
                default:
                    viewHolder.number.setTextColor(Color.GRAY);
                    viewHolder.userName.setTextColor(Color.GRAY);
                    viewHolder.userName.setTextSize(20);
                    viewHolder.count.setTextColor(Color.GRAY);
            }
            viewHolder.materialCardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, HistoryActivity.class);
//                intent.putExtra("title",rankingInfo.getUserName());
                intent.putExtra("dataList", GsonUtil.getInstance().toJson(rankingInfo.getDataList()));
                intent.putExtra("title", rankingInfo.getUserName());
                context.startActivity(intent);
            });
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder viewHolder = (FooterViewHolder) holder;
            int cnt = 0;
            for (RankingInfo rankingInfo : list) {
                cnt += rankingInfo.getCount();
            }
            viewHolder.textViewTotal.setText(cnt + "单");
        }
    }

    @Override
    public int getItemCount() {
        return list.isEmpty() ? 1 : list.size() + 1;
    }

    class NoDataViewHolder extends RecyclerView.ViewHolder {

        NoDataViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.constraintLayout)
        ConstraintLayout constraintLayout;
        @BindView(R.id.mcv_item)
        MaterialCardView materialCardView;
        @BindView(R.id.tv_item_number)
        TextView number;
        @BindView(R.id.tv_item_name)
        TextView userName;
        @BindView(R.id.tv_item_count)
        TextView count;

        DataViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_item_total)
        TextView textViewTotal;

        FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
