package com.colman.trather.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.colman.trather.Consts;
import com.colman.trather.R;
import com.colman.trather.models.Review;
import com.colman.trather.services.SharedPref;
import com.colman.trather.viewModels.TripInfoViewModel;


public class ReviewsRecyclerViewAdapter extends BaseRecyclerViewAdapter<Review, ReviewsRecyclerViewAdapter.ViewHolder> {
    private final String currentUserUid;
    private ItemDeleteListener deleteButtonClickListener;
    private final Context context;
    private TripInfoViewModel mTripInfoViewModel;

    public ReviewsRecyclerViewAdapter(Context context, TripInfoViewModel tripInfoViewModel) {
        super(context);
        this.context = context;
        mTripInfoViewModel = tripInfoViewModel;

        currentUserUid = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.reviews_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = mData.get(position);

        final String profileImgUrl = review.getProfileImgUrl();
        if (!TextUtils.isEmpty(profileImgUrl)) {
            // Review already loaded
            Glide.with(context).load(profileImgUrl).error(R.mipmap.ic_launcher).into(holder.profile);
            holder.author.setText(review.getAuthorName());
        } else {
            mTripInfoViewModel.getUserByUid(review.getAuthorUid()).observe((LifecycleOwner) context, user -> {
                if (user != null) {
                    review.setAuthorName(user.getFullname());
                    review.setProfileImgUrl(user.getImageUrl());

                    holder.author.setText(user.getFullname());
                    if (!TextUtils.isEmpty(user.getImageUrl()))
                        Glide.with(context).load(user.getImageUrl()).error(R.mipmap.ic_launcher).into(holder.profile);
                }
            });
        }

        holder.comment.setText(review.getComment());
        holder.ratingBar.setRating(review.getStars());
        if (currentUserUid.equals(review.getAuthorUid())) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        holder.deleteBtn.setOnClickListener(v -> deleteButtonClickListener.onItemDeleteClick(v, holder.getAdapterPosition()));
    }

    public void setDeleteButtonClickListener(ItemDeleteListener deleteButtonClickListener) {
        this.deleteButtonClickListener = deleteButtonClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView profile;
        TextView author;
        TextView comment;
        RatingBar ratingBar;
        ImageView deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.author);
            comment = itemView.findViewById(R.id.comment);
            profile = itemView.findViewById(R.id.profile);
            deleteBtn = itemView.findViewById(R.id.delete);
            ratingBar = itemView.findViewById(R.id.ratingBar);
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
