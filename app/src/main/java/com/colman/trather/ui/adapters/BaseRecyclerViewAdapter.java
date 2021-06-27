package com.colman.trather.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerViewAdapter<Data, ViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<ViewHolder> {

    protected final LayoutInflater mInflater;
    protected List<Data> mData;
    protected ItemClickListener mClickListener;

    BaseRecyclerViewAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public Data getItem(int id) {
        return mData.get(id);
    }

    public void setItems(List<Data> tripList) {
        this.mData = tripList;
        notifyDataSetChanged();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface ItemDeleteListener {
        void onItemDeleteClick(View view, int position);
    }
}
