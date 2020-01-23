package jn.mjz.aiot.jnuetc.util;

import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.youth.xframe.XFrame;

import java.util.List;

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.DataChangeLog;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.DataChangeLogAdapter;
import jn.mjz.aiot.jnuetc.view.adapter.recycler.DataTimeLineAdapter;
import jn.mjz.aiot.jnuetc.view.custom.LocalImageHolder;

/**
 * @author qq1962247851
 * @date 2020/1/19 10:39
 */
public class DataBindingUtils {

    @BindingAdapter({"currentItem", "entries", "onItemSelectedListener", "enable"})
    public static void setSpinner(Spinner spinner,
                                  String currentItem,
                                  List<String> entries,
                                  AdapterView.OnItemSelectedListener onItemSelectedListener,
                                  Boolean enable) {
        spinner.setSelection(entries.indexOf(currentItem));
        spinner.setOnItemSelectedListener(onItemSelectedListener);
        spinner.setEnabled(enable);
    }

    @BindingAdapter({"visibility"})
    public static void setVisibility(View view, Boolean visibility) {
        view.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter({"enable"})
    public static void setEnable(View view, Boolean enable) {
        view.setEnabled(enable);
    }

    @BindingAdapter(value = {"urls", "onItemClickListener", "firstIndex", "zoomEnable"}, requireAll = false)
    public static void setBanner(ConvenientBanner<String> convenientBanner,
                                 List<String> urls,
                                 OnItemClickListener onItemClickListener,
                                 Integer firstIndex,
                                 Boolean zoomEnable) {
        if (urls != null && !urls.isEmpty()) {
            convenientBanner.setPages(new CBViewHolderCreator() {
                @Override
                public Holder createHolder(View itemView) {
                    LocalImageHolder imageHolder = new LocalImageHolder(
                            itemView,
                            XFrame.getContext()
                    );
                    imageHolder.setPhotoViewEnable(zoomEnable == null ? false : zoomEnable);
                    return imageHolder;
                }

                @Override
                public int getLayoutId() {
                    return R.layout.view_pager_item_gallery;
                }
            }, urls);
            if (firstIndex != null) {
                convenientBanner.setFirstItemPos(firstIndex);
            }
            if (urls.size() != 1) {
                convenientBanner.setPageIndicator(new int[]{R.drawable.banner_unselect, R.drawable.banner_select});
                convenientBanner.startTurning();
            } else {
                convenientBanner.setCanLoop(false);
            }
            if (onItemClickListener != null) {
                convenientBanner.setOnItemClickListener(onItemClickListener);
            }
        }
    }

    @BindingAdapter({"fabVisibility"})
    public static void setFabVisiable(FloatingActionButton fab, Boolean fabVisibility) {
        if (fabVisibility) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    @BindingAdapter({"textWatcher"})
    public static void addTextWatcher(EditText editText, TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }
}
