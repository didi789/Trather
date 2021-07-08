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
import com.colman.trather.models.Trip;

import java.util.List;
import java.util.stream.Collectors;

public class TripRecyclerViewAdapter extends BaseRecyclerViewAdapter<Trip, TripRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private List<Trip> allTripList;

    public TripRecyclerViewAdapter(Context context) {
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
    public void setItems(List<Trip> tripList) {
        this.allTripList = tripList;
        super.setItems(tripList);
    }

    public void applyFilter(String filter) {
        super.setItems(allTripList.stream().filter(trip -> trip.filter(filter)).collect(Collectors.toList()));
    }

    @Override
    public void onBindViewHolder(TripRecyclerViewAdapter.ViewHolder holder, int position) {
        Trip trip = mData.get(position);
        Glide.with(context).load(trip.getImgUrl()).error(R.mipmap.ic_launcher).into(holder.icon);
        holder.title.setText(trip.getTitle());
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView icon;
        TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getBindingAdapterPosition());
            }
        }
    }
}
