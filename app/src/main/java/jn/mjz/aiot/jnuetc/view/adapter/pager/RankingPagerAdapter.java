package jn.mjz.aiot.jnuetc.view.adapter.pager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.card.MaterialCardView;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.XDateUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.OrderTimeRankingInfo;
import jn.mjz.aiot.jnuetc.greendao.entity.RankingInfo;
import jn.mjz.aiot.jnuetc.util.DateUtil;
import jn.mjz.aiot.jnuetc.util.GlobalUtil;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.SharedPreferencesUtil;
import jn.mjz.aiot.jnuetc.view.activity.HistoryActivity;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.RankingAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.WrapContentLinearLayoutManager;
import jn.mjz.aiot.jnuetc.view.custom.ExpandLayout;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
public class RankingPagerAdapter extends PagerAdapter {

    private static final List<Integer> distinct = MainViewModel.getUser().haveWholeSchoolAccess() ?
            Arrays.asList(0, 1) : Collections.singletonList(MainViewModel.getUser().getWhichGroup());

    private static final List<Integer> DEFAULT_CHART_COLORS = new ArrayList<>(
            Arrays.asList(
                    XFrame.getColor(R.color.ChartColor1), XFrame.getColor(R.color.ChartColor2), XFrame.getColor(R.color.ChartColor3), XFrame.getColor(R.color.ChartColor4),
                    XFrame.getColor(R.color.ChartColor5), XFrame.getColor(R.color.ChartColor6), XFrame.getColor(R.color.ChartColor7), XFrame.getColor(R.color.ChartColor8),
                    XFrame.getColor(R.color.ChartColor9), XFrame.getColor(R.color.ChartColor10), XFrame.getColor(R.color.ChartColor11), XFrame.getColor(R.color.ChartColor12),
                    XFrame.getColor(R.color.ChartColor13), XFrame.getColor(R.color.ChartColor14), XFrame.getColor(R.color.ChartColor15), XFrame.getColor(R.color.ChartColor16)
            )
    );
    private static final String TAG = "RankingPagerAdapter";
    private String[] titles = {"周排行榜", "总排行榜"};
    private List<RankingInfo> rankingInfos1;
    private List<RankingInfo> rankingInfos2;
    private List<Integer> states = new ArrayList<>(
            Arrays.asList(
                    1, 2
            )
    );
    private List<Integer> stateDone = new ArrayList<>(
            Collections.singletonList(
                    2
            )
    );
    private List<Integer> statesAll = new ArrayList<>(
            Arrays.asList(
                    0, 1, 2
            )
    );

    private RankingAdapter rankingAdapter1;
    private RankingAdapter rankingAdapter2;
    private RecyclerView recyclerView1;
    private RecyclerView recyclerView2;
    private LinkedHashMap<String, View> views1;
    private LinkedHashMap<String, View> views2;

    private Context context;

    public RankingPagerAdapter(List<RankingInfo> rankingInfos1, List<RankingInfo> rankingInfos2, Context context) {
        this.rankingInfos1 = rankingInfos1;
        this.rankingInfos2 = rankingInfos2;
        this.context = context;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return makeOnePage(container, position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    }

    /**
     * 创建一个饼状图
     *
     * @param entries     entries
     * @param colors      颜色
     * @param label       图例的标签（默认关闭）pieChart.getLegend().setEnabled(false);
     * @param description 图的描述
     * @return PieChart
     */
    private PieChart createPieChart(List<PieEntry> entries, List<Integer> colors, String label, String description) {
        boolean haveSmallValue = false;
        for (PieEntry pieEntry : entries) {
            if (pieEntry.getValue() - 0.05 < 1e-9) {
                haveSmallValue = true;
                break;
            }
        }
        PieChart pieChart = new PieChart(context);
        ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);
        pieChart.setLayoutParams(layoutParams1);
        pieChart.setUsePercentValues(true);
        pieChart.setNoDataTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
        pieChart.setEntryLabelColor(XFrame.getColor(R.color.leftSelectableTextColor));
        pieChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);
        pieChart.setNoDataText("暂无数据可以统计喔~~~");
        pieChart.getLegend().setEnabled(false);

        if (!entries.isEmpty()) {
            PieDataSet set = new PieDataSet(entries, label);

            //根据主题设置颜色
            set.setValueTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
            set.setValueLineColor(XFrame.getColor(R.color.leftSelectableTextColor));

            set.setColors(colors);

            if (haveSmallValue) {
                //数据在饼图周围显示
                set.setValueLinePart1OffsetPercentage(80.f);
                set.setValueLinePart1Length(1.2f);
                set.setValueLinePart2Length(0.5f);
                set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                pieChart.setExtraOffsets(10f, 5f, 10f, 5f);
                pieChart.setDrawEntryLabels(false);
                Legend legend = pieChart.getLegend();
                legend.setTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
                legend.setEnabled(true);
                legend.setOrientation(Legend.LegendOrientation.VERTICAL);
                //顶部
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                //左对齐
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            }


            PieData data = new PieData(set);
            data.setDrawValues(true);
            data.setValueFormatter(new PercentFormatter(pieChart));
            data.setValueTextSize(12f);

            pieChart.setData(data);
            pieChart.invalidate();

            Description dscp = new Description();
            dscp.setText(description);

            pieChart.setDescription(dscp);
            pieChart.setHoleRadius(0f);
            pieChart.setTransparentCircleRadius(0f);
        } else {
            pieChart.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        return pieChart;
    }

    /**
     * 创建一个柱状图
     *
     * @param entries     entries
     * @param colors      颜色
     * @param label       图例的标签
     * @param xValues     横坐标
     * @param description 图的描述
     * @return BarChart
     */
    private BarChart createBarChart(List<BarEntry> entries, List<Integer> colors, String label, List<String> xValues, String description) {
        BarChart barChart = new BarChart(context);
        ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);
        barChart.setLayoutParams(layoutParams1);
        barChart.setNoDataTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
        barChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);
        barChart.setNoDataText("暂无数据可以统计喔~~~");
        //是否绘制网格背景
        barChart.setDrawGridBackground(false);
        //将Y数据显示在点的上方
        barChart.setDrawValueAboveBar(true);
        //挤压缩放
        barChart.setPinchZoom(true);
        //双击缩放
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(2500);
        barChart.setFitBars(true);
        //图例设置
        barChart.getLegend().setEnabled(false);

        if (!entries.isEmpty()) {

            //对Y轴进行设置
            YAxis yAxis = barChart.getAxisLeft();
            yAxis.setTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
            //设置最小间隔，防止当放大时，出现重复标签
            yAxis.setGranularity(1f);
            yAxis.setDrawAxisLine(true);
            yAxis.setDrawGridLines(true);
            // this replaces setStartAtZero(true)
            yAxis.setAxisMinimum(0f);
            yAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return (int) value + "单";
                }
            });
//        yl.setInverted(true);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
            //设置最小间隔，防止当放大时，出现重复标签
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            //显示个数
            xAxis.setLabelCount(xValues.size());
            //设置x轴的数据
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return xValues.get((int) value);
                }
            });

            BarDataSet set = new BarDataSet(entries, label);
            set.setValueTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
            set.setColors(colors);

            BarData data = new BarData(set);
            data.setDrawValues(true);
            data.setValueTextSize(12f);
            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return (int) value + "单";
                }
            });

            barChart.setData(data);
            //图表数据显示动画
            barChart.animateXY(800, 800);
            //设置屏幕显示条数
            barChart.setVisibleXRangeMaximum(8);
            barChart.invalidate();

            Description dscp = new Description();
            dscp.setText(description);
            barChart.setDescription(dscp);
        } else {
            barChart.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        return barChart;
    }

    /**
     * 创建一个折线图，单条折线
     *
     * @param entries     entries
     * @param colors      颜色
     * @param label       图例的标签
     * @param xValues     横坐标
     * @param description 图的描述
     * @return LineChart
     */
    private LineChart createLineChart(List<Entry> entries, List<Integer> colors, String label, List<String> xValues, String description) {
        LineChart lineChart = new LineChart(context);
        ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);
        lineChart.setLayoutParams(layoutParams1);
        lineChart.setNoDataTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
        lineChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);
        lineChart.setNoDataText("暂无数据可以统计喔~~~");
        //是否绘制网格背景
        lineChart.setDrawGridBackground(false);
        //挤压缩放
        lineChart.setPinchZoom(true);
        //双击缩放
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.animateY(2500);
        //图例设置
        lineChart.getLegend().setEnabled(false);

        if (!entries.isEmpty()) {
            //对Y轴进行设置
            YAxis yAxis = lineChart.getAxisLeft();
            yAxis.setTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
            //设置最小间隔，防止当放大时，出现重复标签
            yAxis.setGranularity(1f);
            yAxis.setDrawAxisLine(true);
            yAxis.setDrawGridLines(true);
            // this replaces setStartAtZero(true)
            yAxis.setAxisMinimum(0f);
            yAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return (int) value + "单";
                }
            });
//        yl.setInverted(true);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
            //设置最小间隔，防止当放大时，出现重复标签
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(-1f);
            xAxis.setAxisMaximum(xValues.size());
            //显示个数
            xAxis.setLabelCount(xValues.size() + 2);
            //设置x轴的数据
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < xValues.size()) {
                        return xValues.get(index);
                    } else {
                        return "";
                    }
                }
            });

            LineDataSet set = new LineDataSet(entries, label);
            set.setValueTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
            set.setColors(colors);

            LineData data = new LineData(set);
            data.setDrawValues(true);
            data.setValueTextSize(12f);
            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return (int) value + "单";
                }
            });

            lineChart.setData(data);
            //图表数据显示动画
            lineChart.animateXY(800, 800);
            //设置屏幕显示条数
            lineChart.setVisibleXRangeMaximum(5);
            lineChart.invalidate();

            Description dscp = new Description();
            dscp.setText(description);
            lineChart.setDescription(dscp);
        } else {
            lineChart.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        return lineChart;
    }

    /**
     * 创建一个折线图，多条折线
     *
     * @param entriesList entriesList
     * @param labels      图例的标签
     * @param xValues     横坐标
     * @param description 图的描述
     * @return LineChart
     */
    private LineChart createMultiLineChart(List<List<Entry>> entriesList, List<String> labels, List<String> xValues, String description) {
        List<ILineDataSet> lineDataSets = new ArrayList<>();
        LineChart lineChart = new LineChart(context);
        ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);
        lineChart.setLayoutParams(layoutParams1);
        lineChart.setNoDataTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
        lineChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);
        lineChart.setNoDataText("暂无数据可以统计喔~~~");
        //是否绘制网格背景
        lineChart.setDrawGridBackground(false);
        //挤压缩放
        lineChart.setPinchZoom(true);
        //双击缩放
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.animateY(2500);
        //图例设置
        Legend legend = lineChart.getLegend();
        legend.setTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        //顶部
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        //左对齐
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);

        if (!entriesList.isEmpty()) {
            //对Y轴进行设置
            YAxis yAxis = lineChart.getAxisLeft();
            yAxis.setTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
            //设置最小间隔，防止当放大时，出现重复标签
            yAxis.setGranularity(1f);
            yAxis.setDrawAxisLine(true);
            yAxis.setDrawGridLines(true);
            // this replaces setStartAtZero(true)
            yAxis.setAxisMinimum(0f);
            yAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return (int) value + "单";
                }
            });
//        yl.setInverted(true);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
            //设置最小间隔，防止当放大时，出现重复标签
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(-1f);
            xAxis.setAxisMaximum(xValues.size());
            //显示个数
            xAxis.setLabelCount(xValues.size() + 2);
            //设置x轴的数据
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < xValues.size()) {
                        return xValues.get(index);
                    } else {
                        return "";
                    }
                }
            });

            for (int i = 0; i < entriesList.size(); i++) {
                List<Entry> entries = entriesList.get(i);
                LineDataSet set = new LineDataSet(entries, labels.get(i));
                set.setValueTextColor(XFrame.getColor(R.color.leftSelectableTextColor));
                set.setColor(RankingPagerAdapter.DEFAULT_CHART_COLORS.get(i));
                set.setCircleColor(RankingPagerAdapter.DEFAULT_CHART_COLORS.get(i));
                set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                set.setDrawFilled(true);
                set.setFillColor(RankingPagerAdapter.DEFAULT_CHART_COLORS.get(i));
                lineDataSets.add(set);
            }

            LineData data = new LineData(lineDataSets);
            data.setDrawValues(true);
            data.setValueTextSize(12f);
            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return (int) value + "单";
                }
            });

            lineChart.setData(data);
            //图表数据显示动画
            lineChart.animateXY(800, 800);
            //设置屏幕显示条数
            lineChart.setVisibleXRangeMaximum(5);
            lineChart.invalidate();

            Description dscp = new Description();
            dscp.setText(description);
            lineChart.setDescription(dscp);
        } else {
            lineChart.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        return lineChart;
    }

    private View makeOnePage(@NonNull ViewGroup container, int position) {
        LinkedHashMap<String, View> views;
        if (container.getChildAt(position) == null) {
            boolean analyzeAll = position == 1;
            RecyclerView recyclerView;
            if (analyzeAll) {
                views = views2 = new LinkedHashMap<>(11);
                recyclerView2 = new RecyclerView(context);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                recyclerView2.setLayoutParams(layoutParams);
                recyclerView2.setLayoutManager(new WrapContentLinearLayoutManager(context));
                rankingAdapter2 = new RankingAdapter(context, rankingInfos2);
                recyclerView2.setAdapter(rankingAdapter2);
                recyclerView = recyclerView2;
            } else {
                views = views1 = new LinkedHashMap<>(11);
                recyclerView1 = new RecyclerView(context);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                recyclerView1.setLayoutParams(layoutParams);
                recyclerView1.setLayoutManager(new WrapContentLinearLayoutManager(context));
                rankingAdapter1 = new RankingAdapter(context, rankingInfos1);
                recyclerView1.setAdapter(rankingAdapter1);
                recyclerView = recyclerView1;
            }

            PieChart pieChart1 = analyzeOrderTime(states, analyzeAll, true);
            PieChart pieChart11 = analyzeOrderTime(states, analyzeAll, false);

            views.put("维修单数排行（只统计已反馈）\n", recyclerView);
            views.put("接单时间段统计", pieChart1);
            views.put("接单时间段统计（关于我）", pieChart11);

            String rankingOrderDateShowType = SharedPreferencesUtil.getSettingPreferences().getString("ranking_order_date_show_type", "2");
            switch (rankingOrderDateShowType) {
                case "1":
                    //两张图展示
                    LineChart lineChart1 = analyzeOrderDate(states, analyzeAll, true);
                    LineChart lineChart11 = analyzeOrderDate(states, analyzeAll, false);
                    views.put("接单日期统计", lineChart1);
                    views.put("接单日期统计（关于我）", lineChart11);
                    break;
                case "2":
                    //一张图
                    LineChart lineChart2 = analyzeOrderDate(states, analyzeAll);
                    views.put("接单日期统计（双重视图）", lineChart2);
                    break;
                default:
                    //三张图
                    LineChart lineChart13 = analyzeOrderDate(states, analyzeAll, true);
                    LineChart lineChart113 = analyzeOrderDate(states, analyzeAll, false);
                    LineChart lineChart23 = analyzeOrderDate(states, analyzeAll);
                    views.put("接单日期统计", lineChart13);
                    views.put("接单日期统计（关于我）", lineChart113);
                    views.put("接单日期统计（双重视图）", lineChart23);
            }
            BarChart barChart1 = analyzeLocal(statesAll, analyzeAll);
            BarChart barChart2 = analyzeCollege(statesAll, analyzeAll);
            PieChart pieChart2 = analyzeGrade(statesAll, analyzeAll);
            PieChart pieChart3 = analyzeMark(stateDone, analyzeAll);
            PieChart pieChart4 = analyzeService(stateDone, analyzeAll, true);
            views.put("报修园区统计", barChart1);
            views.put("报修学院统计", barChart2);
            views.put("报修年级统计", pieChart2);
            views.put("报修人电脑水平统计", pieChart3);
            views.put("服务内容统计", pieChart4);
            NestedScrollView scrollView = new NestedScrollView(context);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            scrollView.setLayoutParams(layoutParams);
            LinearLayout linearLayout = new LinearLayout(context);
            ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(layoutParams1);
            for (String next : views.keySet()) {
                LinearLayout linearLayout2 = new LinearLayout(context);
                ViewGroup.LayoutParams layoutParams4 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                linearLayout2.setOrientation(LinearLayout.VERTICAL);
                linearLayout2.setLayoutParams(layoutParams4);
                linearLayout2.setPadding(20, 10, 20, 10);
                MaterialCardView materialCardView = new MaterialCardView(context);
                MaterialCardView.LayoutParams layoutParams2 = new MaterialCardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams2.setMargins(20, 15, 20, 15);
                materialCardView.setLayoutParams(layoutParams2);
                //构造标题
                TextView textView = new TextView(context);
                textView.setText(next);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setPadding(30, 15, 30, 15);
                textView.setTextSize(20);

//            TypedValue typedValue = new TypedValue();
//            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
//            int[] attribute = new int[]{android.R.attr.selectableItemBackground};
//            TypedArray typedArray = context.getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
//            Drawable drawable = typedArray.getDrawable(0);
//            typedArray.recycle();
//            textView.setBackground(drawable);

                View view = views.get(next);
                //构造折叠视图
                ExpandLayout expandLayout = new ExpandLayout(context);
                ViewGroup.LayoutParams layoutParams3 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                expandLayout.setLayoutParams(layoutParams3);
                linearLayout2.addView(textView);
                if (view instanceof RecyclerView) {
                    NestedScrollView nestedScrollView = new NestedScrollView(context);
                    ViewGroup.LayoutParams layoutParams5 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    nestedScrollView.setLayoutParams(layoutParams5);
                    nestedScrollView.addView(view);

                    linearLayout2.addView(nestedScrollView);
                    linearLayout.addView(linearLayout2);
                } else {
                    expandLayout.addView(view);
                    expandLayout.reSetViewDimensions();
                    boolean rankingExpand = SharedPreferencesUtil.getSettingPreferences().getBoolean("ranking_expand", true);
                    expandLayout.initExpand(rankingExpand);
                    Drawable drawable1 = XFrame.getDrawable(expandLayout.isExpand() ? R.drawable.ic_arrow_drop_up_gray : R.drawable.ic_arrow_drop_down_gray);
                    drawable1.setBounds(0, 0, 80, 80);
                    textView.setCompoundDrawables(null, null, drawable1, null);
                    linearLayout2.addView(expandLayout);
                    materialCardView.addView(linearLayout2);
                    linearLayout.addView(materialCardView);
                    textView.setOnClickListener(v -> {
                        expandLayout.toggleExpand();
                        Drawable drawable2 = XFrame.getDrawable(expandLayout.isExpand() ? R.drawable.ic_arrow_drop_up_gray : R.drawable.ic_arrow_drop_down_gray);
                        drawable2.setBounds(0, 0, 80, 80);
                        textView.setCompoundDrawables(null, null, drawable2, null);
                    });
                }
            }
            scrollView.addView(linearLayout);
            container.addView(scrollView);
            return scrollView;
        } else {
            return container.getChildAt(position);
        }
    }

    /**
     * 统计园区，返回柱状图
     *
     * @param states     待统计的报修单状态
     * @param analyzeAll 统计所有报修单
     * @return BarChart
     */
    private BarChart analyzeLocal(List<Integer> states, boolean analyzeAll) {
        //横坐标
        List<String> xValues = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>(GlobalUtil.LOCATIONS.length);
        Map<String, List<Data>> localDataMap = new HashMap<>(GlobalUtil.LOCATIONS.length);
        for (String local : GlobalUtil.LOCATIONS) {
            map.put(local, 0);
            localDataMap.put(local, new ArrayList<>());
        }
        List<Data> dataList = getDataListByTime(states, analyzeAll);
        for (Data data : dataList) {
            String local = data.getLocal();
            int integer = map.get(local);
            integer++;
            map.remove(local);
            map.put(local, integer);
            List<Data> dataList1 = localDataMap.get(local);
            dataList1.add(data);
            localDataMap.remove(local);
            localDataMap.put(local, dataList1);
        }
        int index = 0;
        for (String key : map.keySet()) {
            int integer = map.get(key);
            if (integer != 0) {
                xValues.add(key);
                entries.add(new BarEntry(index, integer));
                index++;
            }
        }
        BarChart barChart = createBarChart(entries, DEFAULT_CHART_COLORS, "", xValues, "");
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof BarEntry) {
                    String local = xValues.get((int) e.getX());
                    List<Data> dataList1 = localDataMap.get(local);
                    Intent intent = new Intent(context, HistoryActivity.class);
                    intent.putExtra("dataList", GsonUtil.getInstance().toJson(dataList1));
                    intent.putExtra("title", local);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });
        return barChart;
    }

    /**
     * 统计学院，返回柱状图
     *
     * @param states     待统计的报修单状态
     * @param analyzeAll 统计所有报修单
     * @return BarChart
     */
    private BarChart analyzeCollege(List<Integer> states, boolean analyzeAll) {
        //横坐标
        List<String> xValues = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>(GlobalUtil.COLLEGES.length);
        Map<String, List<Data>> collegeDataMap = new HashMap<>(GlobalUtil.COLLEGES.length);
        for (String college : GlobalUtil.COLLEGES) {
            map.put(college, 0);
            collegeDataMap.put(college, new ArrayList<>());
        }
        List<Data> dataList = getDataListByTime(states, analyzeAll);
        for (Data data : dataList) {
            String college = data.getCollege();
            int integer = map.get(college);
            integer++;
            map.remove(college);
            map.put(college, integer);
            List<Data> dataList1 = collegeDataMap.get(college);
            dataList1.add(data);
            collegeDataMap.remove(college);
            collegeDataMap.put(college, dataList1);
        }
        int index = 0;
        for (String key : map.keySet()) {
            int integer = map.get(key);
            if (integer != 0) {
                xValues.add(key);
                entries.add(new BarEntry(index, integer));
                index++;
            }
        }
        BarChart barChart = createBarChart(entries, DEFAULT_CHART_COLORS, "", xValues, "");
        barChart.setVisibleXRangeMaximum(5);
        barChart.getXAxis().setLabelRotationAngle(-15f);
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof BarEntry) {
                    String college = xValues.get((int) e.getX());
                    List<Data> dataList1 = collegeDataMap.get(college);
                    Intent intent = new Intent(context, HistoryActivity.class);
                    intent.putExtra("dataList", GsonUtil.getInstance().toJson(dataList1));
                    intent.putExtra("title", college);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });
        return barChart;
    }

    /**
     * 统计接单日期，返回双条折线图
     *
     * @param states     待统计的报修单状态
     * @param analyzeAll 统计所有报修单
     * @return LineChart
     */
    private LineChart analyzeOrderDate(List<Integer> states, boolean analyzeAll) {
        List<String> labels = new ArrayList<>(
                Arrays.asList(
                        "所有人", "我"
                )
        );
        //横坐标
        List<String> xValues = new ArrayList<>();
        Set<String> dateSet = new HashSet<>();
        List<List<Entry>> entriesList = new ArrayList<>();
        Map<String, List<Data>> dateDataMap = new HashMap<>(7);
        Map<String, List<Data>> dateDataMapMyself = new HashMap<>(7);
        List<Data> dataList = getDataListByOrderTimeAndRepairer(states, analyzeAll, true);
        List<Data> dataListMyself = getDataListByOrderTimeAndRepairer(states, analyzeAll, false);
        for (Data data : dataList) {
            String dateString = XDateUtils.millis2String(data.getOrderDate(), "yyyy/MM/dd");
            if (dateSet.add(dateString)) {
                xValues.add(dateString);
            }
            List<Data> dataList1 = dateDataMap.get(dateString);
            if (dataList1 == null) {
                dataList1 = new ArrayList<>();
                dataList1.add(data);
                dateDataMap.put(dateString, dataList1);
            } else {
                dataList1.add(data);
                dateDataMap.remove(dateString);
                dateDataMap.put(dateString, dataList1);
            }
        }
        for (Data data : dataListMyself) {
            String dateString = XDateUtils.millis2String(data.getOrderDate(), "yyyy/MM/dd");
            List<Data> dataList1 = dateDataMapMyself.get(dateString);
            if (dataList1 == null) {
                dataList1 = new ArrayList<>();
                dataList1.add(data);
                dateDataMapMyself.put(dateString, dataList1);
            } else {
                dataList1.add(data);
                dateDataMapMyself.remove(dateString);
                dateDataMapMyself.put(dateString, dataList1);
            }
        }

        //从小到大排序
        Collections.sort(xValues, String::compareTo);

        List<Entry> entries = new ArrayList<>();
        List<Entry> entriesMyself = new ArrayList<>();
        int index = 0;
        for (String key : xValues) {
            List<Data> dataList1 = dateDataMapMyself.get(key);
            if (dataList1 != null) {
                int integerMyself = dataList1.size();
                if (integerMyself != 0) {
                    entriesMyself.add(new Entry(index, integerMyself));
                }
            }
            int integer = dateDataMap.get(key).size();
            if (integer != 0) {
                entries.add(new Entry(index, integer));
                index++;
            }
        }

        if (!entries.isEmpty()) {
            entriesList.add(entries);
            entriesList.add(entriesMyself);
        }

        LineChart lineChart = createMultiLineChart(entriesList, labels, xValues, "");
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setLabelRotationAngle(-15f);
        xAxis.setAxisMinimum(-1f);
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String dateString = xValues.get((int) e.getX());
                int y = (int) e.getY();
                Intent intent = new Intent(context, HistoryActivity.class);
                intent.putExtra("title", dateString);
                List<Data> dataList1 = dateDataMap.get(dateString);
                List<Data> dataList1Myself = dateDataMapMyself.get(dateString);
                if (dataList1.size() != y) {
                    intent.putExtra("dataList", GsonUtil.getInstance().toJson(dataList1Myself));
                } else {
                    intent.putExtra("dataList", GsonUtil.getInstance().toJson(dataList1));
                }
                context.startActivity(intent);
            }

            @Override
            public void onNothingSelected() {
            }
        });
        return lineChart;
    }

    /**
     * 统计接单日期，返回折线图
     *
     * @param states         待统计的报修单状态
     * @param analyzeAll     统计所有报修单
     * @param analyzeAllUser 统计所有人
     * @return LineChart
     */
    private LineChart analyzeOrderDate(List<Integer> states, boolean analyzeAll, boolean analyzeAllUser) {
        //横坐标
        List<String> xValues = new ArrayList<>();
        Set<String> dateSet = new HashSet<>();
        List<Entry> entries = new ArrayList<>();
        Map<String, List<Data>> dateDataMap = new HashMap<>(7);
        List<Data> dataList = getDataListByOrderTimeAndRepairer(states, analyzeAll, analyzeAllUser);
        for (Data data : dataList) {
            String dateString = XDateUtils.millis2String(data.getOrderDate(), "yyyy/MM/dd");
            if (dateSet.add(dateString)) {
                xValues.add(dateString);
            }
            List<Data> dataList1 = dateDataMap.get(dateString);
            if (dataList1 == null) {
                dataList1 = new ArrayList<>();
                dataList1.add(data);
                dateDataMap.put(dateString, dataList1);
            } else {
                dataList1.add(data);
                dateDataMap.remove(dateString);
                dateDataMap.put(dateString, dataList1);
            }
        }

        Collections.sort(xValues, String::compareTo);

        int index = 0;
        for (String key : xValues) {
            int integer = dateDataMap.get(key).size();
            if (integer != 0) {
                entries.add(new Entry(index, integer));
                index++;
            }
        }
        LineChart lineChart = createLineChart(entries, DEFAULT_CHART_COLORS, "", xValues, "");
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setLabelRotationAngle(-15f);
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String dateString = xValues.get((int) e.getX());
                List<Data> dataList1 = dateDataMap.get(dateString);
                Intent intent = new Intent(context, HistoryActivity.class);
                intent.putExtra("dataList", GsonUtil.getInstance().toJson(dataList1));
                intent.putExtra("title", dateString);
                context.startActivity(intent);
            }

            @Override
            public void onNothingSelected() {
            }
        });
        return lineChart;
    }

    /**
     * 统计接单时间，返回饼状图
     *
     * @param states         要统计的状态
     * @param analyzeAll     统计所有时间段或者当前一周
     * @param analyzeAllUser 统计所有人
     * @return PieChart
     */
    private PieChart analyzeOrderTime(List<Integer> states, boolean analyzeAll, boolean analyzeAllUser) {
        OrderTimeRankingInfo orderTimeRankingInfo = new OrderTimeRankingInfo();
        List<Data> dataList = getDataListByOrderTimeAndRepairer(states, analyzeAll, analyzeAllUser);
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (Data data : dataList) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(data.getOrderDate());
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            if (hourOfDay >= 6 && hourOfDay < 12) {
                orderTimeRankingInfo.getMorningDataList().add(data);
            } else if (hourOfDay >= 12 && hourOfDay < 15) {
                orderTimeRankingInfo.getNoonDataList().add(data);
            } else if (hourOfDay >= 15 && hourOfDay < 18) {
                orderTimeRankingInfo.getAfterNoonDataList().add(data);
            } else {
                orderTimeRankingInfo.getEveningDataList().add(data);
            }
        }
        int morning = orderTimeRankingInfo.getMorningDataList().size();
        int noon = orderTimeRankingInfo.getNoonDataList().size();
        int afternoon = orderTimeRankingInfo.getAfterNoonDataList().size();
        int evening = orderTimeRankingInfo.getEveningDataList().size();
        int total = morning + noon + afternoon + evening;
        if (morning != 0) {
            entries.add(new PieEntry((float) morning / total, OrderTimeRankingInfo.MORNING_LABEL));
            colors.add(OrderTimeRankingInfo.COLOR_MORNING);
        }
        if (noon != 0) {
            entries.add(new PieEntry((float) noon / total, OrderTimeRankingInfo.NOON_LABEL));
            colors.add(OrderTimeRankingInfo.COLOR_NOON);
        }
        if (afternoon != 0) {
            entries.add(new PieEntry((float) afternoon / total, OrderTimeRankingInfo.AFTERNOON_LABEL));
            colors.add(OrderTimeRankingInfo.COLOR_AFTERNOON);
        }
        if (evening != 0) {
            entries.add(new PieEntry((float) evening / total, OrderTimeRankingInfo.EVENING_LABEL));
            colors.add(OrderTimeRankingInfo.COLOR_EVENING);
        }
        PieChart pieChart = createPieChart(entries, colors, "时间段", "");
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    Intent intent = new Intent(context, HistoryActivity.class);
                    List<Data> dataList;
                    String label;
                    PieEntry pieEntry = (PieEntry) e;
                    switch (pieEntry.getLabel()) {
                        case OrderTimeRankingInfo.MORNING_LABEL:
                            dataList = orderTimeRankingInfo.getMorningDataList();
                            label = OrderTimeRankingInfo.MORNING_LABEL_DESCRIPTION;
                            break;
                        case OrderTimeRankingInfo.NOON_LABEL:
                            dataList = orderTimeRankingInfo.getNoonDataList();
                            label = OrderTimeRankingInfo.NOON_LABEL_DESCRIPTION;
                            break;
                        case OrderTimeRankingInfo.AFTERNOON_LABEL:
                            dataList = orderTimeRankingInfo.getAfterNoonDataList();
                            label = OrderTimeRankingInfo.AFTERNOON_LABEL_DESCRIPTION;
                            break;
                        default:
                            dataList = orderTimeRankingInfo.getEveningDataList();
                            label = OrderTimeRankingInfo.EVENING_LABEL_DESCRIPTION;
                    }
                    intent.putExtra("dataList", GsonUtil.getInstance().toJson(dataList));
                    intent.putExtra("title", "接单时间：" + label);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });
        return pieChart;
    }

    /**
     * 统计报修年级，返回饼状图
     *
     * @param states     要统计的状态
     * @param analyzeAll 统计所有时间段或者当前一周
     * @return PieChart
     */
    private PieChart analyzeGrade(List<Integer> states, boolean analyzeAll) {
        List<PieEntry> entries = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>(GlobalUtil.GRADES.length);
        Map<String, List<Data>> gradeDataList = new HashMap<>(GlobalUtil.GRADES.length);
        for (String grade : GlobalUtil.GRADES) {
            map.put(grade, 0);
            gradeDataList.put(grade, new ArrayList<>());
        }
        List<Data> dataList = getDataListByTime(states, analyzeAll);
        int total = dataList.size();
        for (Data data : dataList) {
            String grade = data.getGrade();
            Integer integer = map.get(grade);
            integer++;
            map.remove(grade);
            map.put(grade, integer);
            List<Data> dataList1 = gradeDataList.get(grade);
            dataList1.add(data);
            gradeDataList.remove(grade);
            gradeDataList.put(grade, dataList1);
        }
        for (String key : map.keySet()) {
            Integer integer = map.get(key);
            if (integer != 0) {
                float f = (float) integer / total;
                entries.add(new PieEntry(f, key));
            }
        }
        PieChart pieChart = createPieChart(entries, DEFAULT_CHART_COLORS, "", "");
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    Intent intent = new Intent(context, HistoryActivity.class);
                    PieEntry pieEntry = (PieEntry) e;
                    String label = pieEntry.getLabel();
                    List<Data> dataList = gradeDataList.get(label);
                    intent.putExtra("dataList", GsonUtil.getInstance().toJson(dataList));
                    intent.putExtra("title", label);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });
        return pieChart;
    }

    /**
     * 统计报修人的电脑水平，返回饼状图
     *
     * @param states     要统计的状态
     * @param analyzeAll 统计所有时间段或者当前一周
     * @return PieChart
     */
    private PieChart analyzeMark(List<Integer> states, boolean analyzeAll) {
        List<PieEntry> entries = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>(GlobalUtil.MARKS.length);
        Map<String, List<Data>> markDataList = new HashMap<>(GlobalUtil.MARKS.length);
        for (String mark : GlobalUtil.MARKS) {
            map.put(mark, 0);
            markDataList.put(mark, new ArrayList<>());
        }
        List<Data> dataList = getDataListByTime(states, analyzeAll);
        int total = dataList.size();
        for (Data data : dataList) {
            String mark = data.getMark();
            Integer integer = map.get(mark);
            integer++;
            map.remove(mark);
            map.put(mark, integer);
            List<Data> dataList1 = markDataList.get(mark);
            dataList1.add(data);
            markDataList.remove(mark);
            markDataList.put(mark, dataList1);
        }
        for (String key : map.keySet()) {
            Integer integer = map.get(key);
            if (integer != 0) {
                float f = (float) integer / total;
                entries.add(new PieEntry(f, key));
            }
        }
        PieChart pieChart = createPieChart(entries, DEFAULT_CHART_COLORS, "", "");
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    Intent intent = new Intent(context, HistoryActivity.class);
                    PieEntry pieEntry = (PieEntry) e;
                    String label = pieEntry.getLabel();
                    List<Data> dataList = markDataList.get(label);
                    intent.putExtra("dataList", GsonUtil.getInstance().toJson(dataList));
                    intent.putExtra("title", label);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });
        return pieChart;
    }

    /**
     * 统计服务内容，返回饼状图
     *
     * @param states         要统计的状态
     * @param analyzeAll     统计所有时间段或者当前一周
     * @param analyzeAllUser 统计所有人
     * @return PieChart
     */
    private PieChart analyzeService(List<Integer> states, boolean analyzeAll, boolean analyzeAllUser) {
        List<PieEntry> entries = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>(GlobalUtil.SERVICES.length);
        Map<String, List<Data>> serviceDataList = new HashMap<>(GlobalUtil.SERVICES.length);
        for (String service : GlobalUtil.SERVICES) {
            map.put(service, 0);
            serviceDataList.put(service, new ArrayList<>());
        }
        List<Data> dataList = getDataListByRepairTimeAndRepairer(states, analyzeAll, analyzeAllUser);
        int total = dataList.size();
        for (Data data : dataList) {
            String service = data.getService();
            Integer integer = map.get(service);
            integer++;
            map.remove(service);
            map.put(service, integer);
            List<Data> dataList1 = serviceDataList.get(service);
            dataList1.add(data);
            serviceDataList.remove(service);
            serviceDataList.put(service, dataList1);
        }
        for (String key : map.keySet()) {
            Integer integer = map.get(key);
            if (integer != 0) {
                float f = (float) integer / total;
                entries.add(new PieEntry(f, key));
            }
        }
        PieChart pieChart = createPieChart(entries, DEFAULT_CHART_COLORS, "", "");
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    Intent intent = new Intent(context, HistoryActivity.class);
                    PieEntry pieEntry = (PieEntry) e;
                    String label = pieEntry.getLabel();
                    List<Data> dataList = serviceDataList.get(label);
                    intent.putExtra("dataList", GsonUtil.getInstance().toJson(dataList));
                    intent.putExtra("title", label);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });
        return pieChart;
    }

    /**
     * 根据接单时间和维修人获取dataList
     *
     * @param states         报修单状态
     * @param analyzeAll     是否统计所有时间，否的话当前一周
     * @param analyzeAllUser 是否统计所有人
     * @return dataList
     */
    private List<Data> getDataListByOrderTimeAndRepairer(List<Integer> states, boolean analyzeAll, boolean analyzeAllUser) {
        QueryBuilder<Data> queryBuilder;
        if (!analyzeAll) {
            //统计一周的报修单
            List<String> dateToWeek = DateUtil.getDateToWeek(new Date(System.currentTimeMillis()));
            String startDate = dateToWeek.get(0);
            String endDate = dateToWeek.get(6);
            long startTimeMills = XDateUtils.date2Millis(XDateUtils.string2Date(startDate + " 00:00:00", "yyyy/MM/dd HH:mm:ss"));
            long endTimeMills = XDateUtils.date2Millis(XDateUtils.string2Date(endDate + " 23:59:59", "yyyy/MM/dd HH:mm:ss"));
            queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                    DataDao.Properties.District.in(distinct),
                    DataDao.Properties.State.in(states),
                    DataDao.Properties.OrderDate.ge(startTimeMills),
                    DataDao.Properties.OrderDate.le(endTimeMills)
            );
            if (!analyzeAllUser) {
                //只统计自己
                queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                        DataDao.Properties.District.in(distinct),
                        DataDao.Properties.State.in(states),
                        DataDao.Properties.OrderDate.ge(startTimeMills),
                        DataDao.Properties.OrderDate.le(endTimeMills),
                        DataDao.Properties.Repairer.like("%" + MainViewModel.user.getUserName() + "%")
                );
            }
        } else {
            //统计所有时间段的有报修单
            queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                    DataDao.Properties.District.in(distinct),
                    DataDao.Properties.State.in(states)
            );
            if (!analyzeAllUser) {
                //只统计自己
                queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                        DataDao.Properties.District.in(distinct),
                        DataDao.Properties.State.in(states),
                        DataDao.Properties.Repairer.like("%" + MainViewModel.user.getUserName() + "%")
                );
            }
        }
        return queryBuilder.build().list();
    }

    /**
     * 根据反馈时间和维修人获取dataList
     *
     * @param states         报修单状态
     * @param analyzeAll     是否统计所有时间，否的话当前一周
     * @param analyzeAllUser 是否统计所有人
     * @return dataList
     */
    private List<Data> getDataListByRepairTimeAndRepairer(List<Integer> states, boolean analyzeAll, boolean analyzeAllUser) {
        QueryBuilder<Data> queryBuilder;
        if (!analyzeAll) {
            //统计一周的报修单
            List<String> dateToWeek = DateUtil.getDateToWeek(new Date(System.currentTimeMillis()));
            String startDate = dateToWeek.get(0);
            String endDate = dateToWeek.get(6);
            long startTimeMills = XDateUtils.date2Millis(XDateUtils.string2Date(startDate + " 00:00:00", "yyyy/MM/dd HH:mm:ss"));
            long endTimeMills = XDateUtils.date2Millis(XDateUtils.string2Date(endDate + " 23:59:59", "yyyy/MM/dd HH:mm:ss"));
            queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                    DataDao.Properties.District.in(distinct),
                    DataDao.Properties.State.in(states),
                    DataDao.Properties.RepairDate.ge(startTimeMills),
                    DataDao.Properties.RepairDate.le(endTimeMills)
            );
            if (!analyzeAllUser) {
                //只统计自己
                queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                        DataDao.Properties.District.in(distinct),
                        DataDao.Properties.State.in(states),
                        DataDao.Properties.RepairDate.ge(startTimeMills),
                        DataDao.Properties.RepairDate.le(endTimeMills),
                        DataDao.Properties.Repairer.like("%" + MainViewModel.user.getUserName() + "%")
                );
            }
        } else {
            //统计所有时间段的有报修单
            queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                    DataDao.Properties.District.in(distinct),
                    DataDao.Properties.State.in(states)
            );
            if (!analyzeAllUser) {
                //只统计自己
                queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                        DataDao.Properties.District.in(distinct),
                        DataDao.Properties.State.in(states),
                        DataDao.Properties.Repairer.like("%" + MainViewModel.user.getUserName() + "%")
                );
            }
        }
        return queryBuilder.build().list();
    }

    /**
     * 根据报修时间获取dataList
     *
     * @param states     报修单状态
     * @param analyzeAll 是否统计所有时间，否的话当前一周
     * @return dataList
     */
    private List<Data> getDataListByTime(List<Integer> states, boolean analyzeAll) {
        QueryBuilder<Data> queryBuilder;
        if (!analyzeAll) {
            //统计一周的报修单
            List<String> dateToWeek = DateUtil.getDateToWeek(new Date(System.currentTimeMillis()));
            String startDate = dateToWeek.get(0);
            String endDate = dateToWeek.get(6);
            long startTimeMills = XDateUtils.date2Millis(XDateUtils.string2Date(startDate + " 00:00:00", "yyyy/MM/dd HH:mm:ss"));
            long endTimeMills = XDateUtils.date2Millis(XDateUtils.string2Date(endDate + " 23:59:59", "yyyy/MM/dd HH:mm:ss"));
            queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                    DataDao.Properties.District.in(distinct),
                    DataDao.Properties.State.in(states),
                    DataDao.Properties.Date.ge(startTimeMills),
                    DataDao.Properties.Date.le(endTimeMills)
            );
        } else {
            queryBuilder = App.getDaoSession().getDataDao().queryBuilder().where(
                    DataDao.Properties.District.in(distinct),
                    DataDao.Properties.State.in(states)
            );
        }
        return queryBuilder.build().list();
    }

    public RankingAdapter getRankingAdapter1() {
        return rankingAdapter1;
    }

    public RankingAdapter getRankingAdapter2() {
        return rankingAdapter2;
    }

    public RecyclerView getRecyclerView1() {
        return recyclerView1;
    }

    public RecyclerView getRecyclerView2() {
        return recyclerView2;
    }

    /**
     * // TODO: 2020/1/22 更新图表，
     *
     * @param position 哪一个位置
     */
    public void updateChart(int position) {
        LinkedHashMap<String, View> views;
        views = position == 0 ? views1 : views2;
        for (String key : views.keySet()) {
            View view = views1.get(key);
            if (view instanceof Chart) {
                Chart chart = (Chart) view;
                chart.invalidate();
            }
        }
    }
}
