package jn.mjz.aiot.jnuetc.view.adapter.pager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.youth.xframe.XFrame;

import java.util.List;

import jn.mjz.aiot.jnuetc.R;


/**
 * @author 19622
 */
public class GalleryVPAdapter extends PagerAdapter {

    private boolean enable;
    private List<String> urls;
    private Context context;
    private IGalleryListener i;

    public GalleryVPAdapter(boolean enable, List<String> urls, Context context, IGalleryListener i) {
        this.enable = enable;
        this.urls = urls;
        this.context = context;
        this.i = i;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View viewWithTag = container.findViewWithTag(position);
        if (viewWithTag == null) {
            viewWithTag = View.inflate(context, R.layout.view_pager_item_gallery, null);
            viewWithTag.setTag(position);
            PhotoView photoView = viewWithTag.findViewById(R.id.photoView);
            ProgressBar progressBar = viewWithTag.findViewById(R.id.progressBar);
            Glide.with(context)
                    .load(urls.get(position))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            photoView.setBackgroundColor(XFrame.getColor(R.color.White));
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .error(R.drawable.xloading_error)
                    .into(photoView);
            photoView.setOnClickListener(v -> i.onPhotoClick());
            if (enable) {
                photoView.enable();
            } else {
                photoView.disenable();
            }
            container.addView(viewWithTag);
        }
        return viewWithTag;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public interface IGalleryListener {
        /**
         * 点击图片
         */
        void onPhotoClick();
    }
}
