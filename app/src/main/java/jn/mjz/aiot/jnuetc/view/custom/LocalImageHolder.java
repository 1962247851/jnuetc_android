package jn.mjz.aiot.jnuetc.view.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bigkoo.convenientbanner.holder.Holder;
import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.youth.xframe.XFrame;

import jn.mjz.aiot.jnuetc.R;

/**
 * @author qq1962247851
 * @date 2020/1/18 19:49
 */
public class LocalImageHolder extends Holder<String> {

    private Context context;
    private PhotoView photoView;
    private ProgressBar progressBar;

    public LocalImageHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
    }

    @Override
    protected void initView(View itemView) {
        photoView = itemView.findViewById(R.id.photoView);
        progressBar = itemView.findViewById(R.id.progressBar);
    }

    @Override
    public void updateUI(String data) {
        Glide.with(context)
                .load(data)
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
                        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .error(R.drawable.xloading_error)
                .into(photoView);
    }

    public void setPhotoViewEnable(boolean enable) {
        if (enable) {
            photoView.enable();
        } else {
            photoView.disenable();
        }
    }
}
