package jn.mjz.aiot.jnuetc.view.adapter.pager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.youth.xframe.widget.XToast;

import jn.mjz.aiot.jnuetc.R;

/**
 * @author 19622
 */
public class PhotoViewAdapter extends PagerAdapter {

    private String[] photoUrls;
    private Context context;
    private IPhotoViewPagerListener iPhotoViewPagerListener;

    public PhotoViewAdapter(String[] photoUrls, Context context, IPhotoViewPagerListener iPhotoViewPagerListener) {
        this.photoUrls = photoUrls;
        this.context = context;
        this.iPhotoViewPagerListener = iPhotoViewPagerListener;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (container.getChildAt(position) == null) {
            LinearLayout linearLayout = new LinearLayout(context);
            PhotoView photoView = new PhotoView(context);
            linearLayout.addView(photoView);
            photoView.enable();
            ViewGroup.LayoutParams layoutParams = photoView.getLayoutParams();
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            photoView.setLayoutParams(layoutParams);
            Glide.with(context)
                    .load(photoUrls[position])
                    .error(R.drawable.xloading_error)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            XToast.error("图片加载失败，请稍后重试");
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            photoView.setImageDrawable(resource);
                            photoView.setOnClickListener(v -> {
                                // TODO: 2019/9/23 有动画的消失
                                iPhotoViewPagerListener.onDismiss();
//                                    photoView.animaTo(mRectF, new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            iPhotoViewPagerListener.onDismiss();
//                                        }
//                                    });
                            });
                            return true;
                        }
                    }).into(photoView);
            container.addView(linearLayout);
            return linearLayout;
        } else {
            return container.getChildAt(position);
        }
    }

    @Override
    public int getCount() {
        return photoUrls.length;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public interface IPhotoViewPagerListener {
        void onDismiss();
    }

}