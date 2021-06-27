package com.colman.trather.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.colman.trather.TripDatabase;
import com.colman.trather.Consts;
import com.colman.trather.R;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.models.Review;
import com.colman.trather.services.SharedPref;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class ReviewsRecyclerViewAdapter extends BaseRecyclerViewAdapter<Review, ReviewsRecyclerViewAdapter.ViewHolder> {
    private final String currentUserEmail;
    private ItemDeleteListener deleteButtonClickListener;
    private final Context context;

    public ReviewsRecyclerViewAdapter(Context context) {
        super(context);
        this.context = context;
        currentUserEmail = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.reviews_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Review review = mData.get(position);

        final String profileImgUrl = review.getProfileImgUrl();
        if (!TextUtils.isEmpty(profileImgUrl)) {
            Glide.with(context).load(profileImgUrl).error(R.mipmap.ic_launcher).into(holder.profile);
        } else {
            Resources res = holder.itemView.getContext().getResources();
            holder.profile.setImageDrawable(res.getDrawable(R.mipmap.ic_launcher));

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Task<DocumentSnapshot> users = db.collection("users").document(review.getAuthor()).get();
            users.addOnCompleteListener(user -> {
                DocumentSnapshot result = user.getResult();
                String imageUrl = (String) result.get("image");
                if (!TextUtils.isEmpty(imageUrl)) {
                    TripDatabase.databaseWriteExecutor.execute(() -> {
                        TripDatabase database = TripDatabase.getDatabase(holder.author.getContext());
                        ReviewDao reviewDao = database.reviewDao();
                        review.setProfileImgUrl(imageUrl);
                        reviewDao.updateReviewerUrl(review);
                    });
                    Glide.with(context).load(imageUrl).error(R.mipmap.ic_launcher).into(holder.profile);
                }
            });
        }

        holder.author.setText(review.getAuthor());
        holder.comment.setText(review.getComment());
        holder.ratingBar.setRating(review.getStars());
        if (currentUserEmail.equals(review.getAuthor())) {
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
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }
}
