package jn.mjz.aiot.jnuetc.View.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.robinhood.ticker.TickerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Time;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.DateUtil;

public class TimerFragment extends Fragment {

    private static final String TAG = "MyselfFragment";
    private Unbinder unbinder;
    private Time time;
    private String title;
    private Long startTime;

    @BindView(R.id.textView_timer_title)
    TextView textViewTitle;
    @BindView(R.id.linearLayout_timer_day)
    LinearLayout linearLayoutDay;
    @BindView(R.id.linearLayout_timer_hour)
    LinearLayout linearLayoutHour;
    @BindView(R.id.linearLayout_timer_minute)
    LinearLayout linearLayoutMinute;
    @BindView(R.id.linearLayout_timer_second)
    LinearLayout linearLayoutSecond;
    @BindView(R.id.tickerView_timer_day)
    TickerView tickerViewDay;
    @BindView(R.id.tickerView_timer_hour)
    TickerView tickerViewHour;
    @BindView(R.id.tickerView_timer_minute)
    TickerView tickerViewMinute;
    @BindView(R.id.tickerView_timer_second)
    TickerView tickerViewSecond;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public TimerFragment() {
    }

    public TimerFragment(@NonNull Long startTime) {
        this.startTime = startTime;

    }

    public TimerFragment(String title, @NonNull Long startTime) {
        this.title = title;
        this.startTime = startTime;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer_ticker_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);

        if (title != null) {
            textViewTitle.setText(title);
        }

        time = DateUtil.diffTime(startTime, System.currentTimeMillis());
        time.startTiming(time1 -> {
            tickerViewDay.setText(String.valueOf(time1.getDay()));
            tickerViewHour.setText(String.valueOf(time1.getHour()));
            tickerViewMinute.setText(String.valueOf(time1.getMinute()));
            tickerViewSecond.setText(String.valueOf(time1.getSecond()));
            if (time1.getDay() != 0) {
                linearLayoutDay.setVisibility(View.VISIBLE);
                linearLayoutHour.setVisibility(View.VISIBLE);
                linearLayoutMinute.setVisibility(View.VISIBLE);
            } else if (time1.getHour() != 0) {
                linearLayoutHour.setVisibility(View.VISIBLE);
                linearLayoutMinute.setVisibility(View.VISIBLE);
            } else if (time1.getMinute() != 0) {
                linearLayoutMinute.setVisibility(View.VISIBLE);
            }

        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        time.endTiming();
        unbinder.unbind();
    }

}
