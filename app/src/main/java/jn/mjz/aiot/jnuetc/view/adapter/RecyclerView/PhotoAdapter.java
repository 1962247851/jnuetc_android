package jn.mjz.aiot.jnuetc.view.adapter.RecyclerView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bm.library.Info;
import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jn.mjz.aiot.jnuetc.R;


public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewRVHolder> {

    private Context context;
    private String[] urls;
    private IPhotoRecyclerViewListener iPhotoRecyclerViewListener;

    public PhotoAdapter(Context context, String[] urls, IPhotoRecyclerViewListener iPhotoRecyclerViewListener) {
        this.context = context;
        this.urls = urls;
        this.iPhotoRecyclerViewListener = iPhotoRecyclerViewListener;
    }

    @NonNull
    @Override
    public PhotoAdapter.PhotoViewRVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewRVHolder(LayoutInflater.from(context).inflate(R.layout.item_rv_photo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewRVHolder holder, int position, @NonNull List<Object> payloads) {
        Glide.with(context)
                .load(urls[position])
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.photoView.setImageDrawable(resource);
                        holder.photoView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                iPhotoRecyclerViewListener.OnPhotoClick(position, holder.photoView.getInfo());
                            }
                        });
                        return true;
                    }
                }).into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return urls.length;
    }

    class PhotoViewRVHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.photoView_item)
        PhotoView photoView;
        @BindView(R.id.progress_bar_item_photo)
        ProgressBar progressBar;

        PhotoViewRVHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull PhotoViewRVHolder holder, int position) {

    }


    public interface IPhotoRecyclerViewListener {
        void OnPhotoClick(int position, Info info);
    }
}



