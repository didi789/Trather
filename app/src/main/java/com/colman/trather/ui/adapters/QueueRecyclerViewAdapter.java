package com.colman.trather.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.colman.trather.R;

import java.util.ArrayList;

public class QueueRecyclerViewAdapter extends RecyclerView.Adapter<QueueRecyclerViewAdapter.MyViewHolder> {

    ArrayList<String> queue;
    String myName;

    public QueueRecyclerViewAdapter(ArrayList<String> nQueue, String myName) {
        if (nQueue != null)
            this.queue = nQueue;
        else {
            queue = new ArrayList<String>();
            queue.add("התור ריק");
        }
        this.myName = myName;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.queue_list_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.personName.setText(queue.get(position));
        if (queue.get(position).equals(myName))
            holder.personCard.setCardBackgroundColor(Color.RED);
        //  holder.personName.setBackgroundColor(Color.argb(20, 250, 20, 20));
    }

    @Override
    public int getItemCount() {
        return queue.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView personName;
        CardView personCard;

        public MyViewHolder(View itemView) {
            super(itemView);
            personCard = itemView.findViewById(R.id.personCard);
            personName = itemView.findViewById(R.id.personName);
        }
    }
}
