package com.colman.trather.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.colman.trather.services.SharedPref;
import com.colman.trather.services.Utils;
import com.colman.trather.ui.adapters.BaseRecyclerViewAdapter;
import com.colman.trather.ui.adapters.ReviewsRecyclerViewAdapter;
import com.colman.trather.viewModels.TripInfoViewModel;
import com.google.firebase.firestore.GeoPoint;

public class TripInfoFragment extends BaseToolbarFragment implements BaseRecyclerViewAdapter.ItemClickListener, BaseRecyclerViewAdapter.ItemDeleteListener {
    private TripInfoViewModel tripInfoViewModel;
    private TextView title;
    private TextView address;
    private ImageView navigate;
    private ImageView water;
    private TextView about;
    private TextView authorName;
    private ImageView difficulty;
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
        navigate = view.findViewById(R.id.navigate);
        water = view.findViewById(R.id.water);
        address = view.findViewById(R.id.address);
        authorName = view.findViewById(R.id.author);
        about = view.findViewById(R.id.about);
        image = view.findViewById(R.id.icon);
        difficulty = view.findViewById(R.id.difficulty);
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
                tripInfoViewModel.addReview(tripInfo, review, ratingBar.getRating());
                alertDialog.dismiss();
            });

            dialogViewLayout.findViewById(R.id.btn_no).setOnClickListener(v1 -> alertDialog.dismiss());

            alertDialog.show();
        });
        authorName.setOnClickListener(v -> goToUserInfo(tripInfo.getAuthorUid()));
        navigate.setOnClickListener(this::startNavigation);
        address.setOnClickListener(this::startNavigation);
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

            initDifficulty();

            water.setImageResource(tripInfo.isWater() ? R.drawable.yes_water : R.drawable.no_water);
            water.setOnClickListener(v -> Toast.makeText(requireActivity(), tripInfo.getTitle() + " " + (tripInfo.isWater() ? getString(R.string.trip_water) : getString(R.string.trip_no_water)), Toast.LENGTH_SHORT).show());
            double locationLat = tripInfo.getLocationLat();
            double locationLon = tripInfo.getLocationLon();
            address.setText(Utils.getLocationText(new GeoPoint(locationLat, locationLon)));
            Glide.with(requireActivity()).load(tripInfo.getImgUrl()).error(R.mipmap.ic_launcher).into(image);

            tripInfoViewModel.getUserByUid(tripInfo.getAuthorUid()).observe(getViewLifecycleOwner(), user -> {
                if (user == null) {
                    return;
                }

                authorName.setText(user.getFullname());
            });
        });

        tripInfoViewModel.getReviewsByTripIdLiveData(tripId).observe(getViewLifecycleOwner(), reviewList -> mAdapter.setItems(reviewList));
    }

    private void initDifficulty() {
        double level = tripInfo.getLevel();

        if (level < 3) {
            difficulty.setImageResource(R.drawable.diff_1_2);
        } else if (level < 5)
            difficulty.setImageResource(R.drawable.diff_3_4);
        else if (level < 6)
            difficulty.setImageResource(R.drawable.diff_5);
        else if (level < 7)
            difficulty.setImageResource(R.drawable.diff_6);
        else if (level < 9)
            difficulty.setImageResource(R.drawable.diff_7_8);
        else if (level < 10)
            difficulty.setImageResource(R.drawable.diff_9);
        else
            difficulty.setImageResource(R.drawable.diff_10);
    }

    @Override
    public void onItemClick(View view, int position) {
        final Review review = mAdapter.getItem(position);
        goToUserInfo(review.getAuthorUid());
    }

    @Override
    public void onItemDeleteClick(View view, int position) {
        final Review review = mAdapter.getItem(position);
        tripInfoViewModel.deleteReview(review);
    }


    private void goToUserInfo(String uid) {
        if (uid.equals(SharedPref.getString(Consts.CURRENT_USER_KEY, ""))) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(getActionId());
        } else {
            Bundle bundle = new Bundle();
            bundle.putString(Consts.KEY_AUTHOR_UID, uid);
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.user_info_screen, bundle);
        }
    }

    private void startNavigation(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + tripInfo.getLocationLat() + "," + tripInfo.getLocationLon()));
        startActivity(intent);
    }
}
