package com.colman.trather.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.colman.trather.Consts;
import com.colman.trather.R;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;
import com.colman.trather.ui.adapters.BaseRecyclerViewAdapter;
import com.colman.trather.ui.adapters.ReviewsRecyclerViewAdapter;
import com.colman.trather.viewModels.TripInfoViewModel;

public class TripInfo extends BaseToolbarFragment implements BaseRecyclerViewAdapter.ItemClickListener, BaseRecyclerViewAdapter.ItemDeleteListener {
    private TripInfoViewModel tripInfoViewModel;
    private TextView title;
    private TextView address;
    private TextView about;
    private ImageView image;
    private ReviewsRecyclerViewAdapter mAdapter;
    private Trip tripInfo = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripInfoViewModel = new ViewModelProvider(requireActivity()).get(TripInfoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        title = view.findViewById(R.id.title);
        address = view.findViewById(R.id.address);
        about = view.findViewById(R.id.about);
        image = view.findViewById(R.id.icon);
        ImageView addReview = view.findViewById(R.id.add_review);

        addReview.setOnClickListener(v -> {
            final LayoutInflater factory = LayoutInflater.from(getContext());
            final View dialogViewLayout = factory.inflate(R.layout.custom_dialog, null);
            final AppCompatRatingBar ratingBar = dialogViewLayout.findViewById(R.id.ratingBar);
            final EditText reviewEditText = dialogViewLayout.findViewById(R.id.review_text);

            final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setView(dialogViewLayout);

            dialogViewLayout.findViewById(R.id.btn_yes).setOnClickListener(v1 -> {
                final String review = reviewEditText.getText().toString();
                final int numStars = (int) ratingBar.getRating();
                tripInfoViewModel.addReview(tripInfo, review, numStars);
                alertDialog.dismiss();
            });

            dialogViewLayout.findViewById(R.id.btn_no).setOnClickListener(v1 -> {
                alertDialog.dismiss();
            });

            alertDialog.show();
        });

        RecyclerView recyclerViewReview = view.findViewById(R.id.reviews_recyclerview);
        recyclerViewReview.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ReviewsRecyclerViewAdapter(getContext());
        mAdapter.setDeleteButtonClickListener(this);
        mAdapter.setClickListener(this);
        recyclerViewReview.setAdapter(mAdapter);

        return view;
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.trip_info;
    }

    @Override
    protected int getActionId() {
        return R.id.trip_info_to_settings;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_trip_info;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String tripId = getArguments().getString(Consts.TRIP_ID);
        tripInfoViewModel.getTripByIdLiveData(tripId).observe(getViewLifecycleOwner(), tripInfo -> {
            if (tripInfo == null) {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_back_to_list);
            }

            this.tripInfo = tripInfo;
            title.setText(tripInfo.getTitle());
            about.setText(tripInfo.getAbout());
            double locationLat = tripInfo.getLocationLat();
            double locationLon = tripInfo.getLocationLon();
            address.setText(getString(R.string.address_text, Double.toString(locationLat), Double.toString(locationLon)));
            Glide.with(requireActivity()).load(tripInfo.getImgUrl()).error(R.mipmap.ic_launcher).into(image);
        });

        tripInfoViewModel.getReviewsByTripIdLiveData(tripId).observe(getViewLifecycleOwner(), reviewList -> {
            mAdapter.setItems(reviewList);
        });
    }

    @Override
    public void onItemClick(View view, int position) {
    }

    @Override
    public void onItemDeleteClick(View view, int position) {
        final Review review = mAdapter.getItem(position);
        tripInfoViewModel.deleteReview(review);
    }
}
