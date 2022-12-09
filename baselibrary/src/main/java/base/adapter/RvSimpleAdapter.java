package base.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Created by gallon on 2019/7/26.
 */

public abstract class RvSimpleAdapter<T> extends RecyclerView.Adapter<RvViewHolder> {

    protected LayoutInflater mInflater;
    public List<T> mData;
    public int mHeaderCount = 0;
    public Context mContext;
    protected int mLayoutId;

    public static abstract class OnItemClickListener {
        public abstract void onItemClick(View view, int position, RvViewHolder holder);

        public void onItemLongClick(View view, int position, RvViewHolder holder) {
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public RvSimpleAdapter(Context context, List<T> data, int layoutId) {
        this.mContext = context;
        this.mData = data;
        this.mLayoutId = layoutId;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return null == mData ? 0 : mData.size();
    }

    @Override
    public void onBindViewHolder(final RvViewHolder holder, final int position) {
        convert(holder, mData.get(holder.getLayoutPosition() - mHeaderCount), position);
        setUpItemEvent(holder);
    }

    public abstract void convert(RvViewHolder holder, T bean, int position);

    public void setUpItemEvent(final RvViewHolder holder) {
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                //这个获取位置的方法，防止添加删除导致位置不变
                int layoutPosition = holder.getAdapterPosition() - mHeaderCount;
                onItemClickListener.onItemClick(holder.itemView, layoutPosition, holder);
            });
            holder.itemView.setOnLongClickListener(v -> {
                int layoutPosition = holder.getAdapterPosition() - mHeaderCount;
                onItemClickListener.onItemLongClick(holder.itemView, layoutPosition, holder);
                return false;
            });
        }
    }

    @Override
    public RvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RvViewHolder viewHolder = onCreateDefViewHolder(parent, viewType);
        return viewHolder;
    }

    protected RvViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        return new RvViewHolder(mInflater.inflate(mLayoutId, parent, false));
    }

    public void addData(T datas) {
        mData.add(mData.size(), datas);
        notifyItemInserted(mData.size());
    }

    public void addData(int pos, T datas) {
        mData.add(pos, datas);
        notifyItemInserted(pos);
    }


    public void deleteData(int pos) {
        mData.remove(pos);
        notifyItemRemoved(pos);
    }
}
