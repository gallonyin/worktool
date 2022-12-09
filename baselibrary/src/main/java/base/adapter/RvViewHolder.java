package base.adapter;

import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by gallon on 2019/7/26.
 */

public class RvViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> mViews;

    public RvViewHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<View>();
    }

    //通过viewId获取控件
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 设置TextView的值
     */
    public RvViewHolder setText(int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    public RvViewHolder setImageResource(int viewId, int resId) {
        ImageView view = getView(viewId);
        view.setImageResource(resId);
        return this;
    }

    public RvViewHolder setImageBitamp(int viewId, Bitmap bitmap) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bitmap);
        return this;
    }

    public RvViewHolder setImageURI(int viewId, String uri) {
        ImageView view = getView(viewId);
//        Imageloader.getInstance().loadImg(view,uri);
        return this;
    }
}
