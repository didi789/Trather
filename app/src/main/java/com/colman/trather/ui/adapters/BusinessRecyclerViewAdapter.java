package com.colman.trather.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.colman.trather.R;
import com.colman.trather.models.Business;

public class BusinessRecyclerViewAdapter extends BaseRecyclerViewAdapter<Business, BusinessRecyclerViewAdapter.ViewHolder> {

    private final Context context;

    public BusinessRecyclerViewAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Business business = mData.get(position);
        Glide.with(context).load(business.getImgUrl()).error(R.mipmap.ic_launcher).into(holder.icon);
        holder.name.setText(business.getName());
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView icon;
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }
}
