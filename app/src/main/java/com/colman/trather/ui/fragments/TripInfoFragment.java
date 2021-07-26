package com.colman.trather.ui.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.colman.trather.viewModels.UserInfoViewModel;
import com.google.firebase.firestore.GeoPoint;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TripInfoFragment extends BaseToolbarFragment implements BaseRecyclerViewAdapter.ItemClickListener, BaseRecyclerViewAdapter.ItemDeleteListener {
    private TripInfoViewModel tripInfoViewModel;
    private TextView title;
    private TextView siteUrl;
    private TextView address;
    private ImageView navigate;
    private ImageView water;
    private TextView about;
    private TextView authorName;
    private ImageView difficulty;
    private ImageView image;
    private Button editTrip;
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
        siteUrl = view.findViewById(R.id.trip_site_url);
        navigate = view.findViewById(R.id.navigate);
        water = view.findViewById(R.id.water);
        address = view.findViewById(R.id.address);
        authorName = view.findViewById(R.id.author);
        editTrip = view.findViewById(R.id.edit_trip);
        about = view.findViewById(R.id.about);
        image = view.findViewById(R.id.icon);
        difficulty = view.findViewById(R.id.difficulty);
        ImageView addReview = view.findViewById(R.id.add_review);

        image.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            final AlertDialog dialog = builder.create();
            View dialogLayout = getLayoutInflater().inflate(R.layout.trip_image_preview, null);
            final ImageView preview = dialogLayout.findViewById(R.id.image);
            ((TextView)dialogLayout.findViewById(R.id.title)).setText(tripInfo.getTitle());
            Glide.with(requireActivity()).load(tripInfo.getImgUrl()).error(R.mipmap.ic_launcher).into(preview);
            dialog.setView(dialogLayout);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.show();
        });
        addReview.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_review_dialog, null);
            AppCompatRatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
            final EditText reviewEditText = dialogView.findViewById(R.id.review_text);

            new SweetAlertDialog(requireContext(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(getString(R.string.add_review))
                    .setCancelButton(getString(R.string.dialog_cancel), Dialog::dismiss)
                    .setConfirmButton(getString(R.string.add), sweetAlertDialog -> {
                        final String review = reviewEditText.getText().toString();
                        if (!review.isEmpty()) {
                            tripInfoViewModel.addReview(tripInfo, review, ratingBar.getRating());
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .setCustomView(dialogView).show();

        });
        authorName.setOnClickListener(v -> goToUserInfo(tripInfo.getAuthorUid()));
        navigate.setOnClickListener(this::startNavigation);
        address.setOnClickListener(this::startNavigation);
        RecyclerView recyclerViewReview = view.findViewById(R.id.reviews_recyclerview);
        recyclerViewReview.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ReviewsRecyclerViewAdapter(getContext(), new ViewModelProvider(requireActivity()).get(UserInfoViewModel.class));
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
    protected int getSettingsActionId() {
        return R.id.trip_info_to_settings;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_trip_info;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String tripId = TripInfoFragmentArgs.fromBundle(getArguments()).getTripId();
        tripInfoViewModel.getTripByIdLiveData(tripId).observe(getViewLifecycleOwner(), tripInfo -> {
            if (tripInfo == null) {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(TripInfoFragmentDirections.actionBackToList());
            }

            this.tripInfo = tripInfo;
            title.setText(tripInfo.getTitle());
            siteUrl.setText(tripInfo.getTripSiteUrl());
            about.setText(tripInfo.getAbout());

            if (tripInfo.getAuthorUid().equals(SharedPref.getString(Consts.CURRENT_USER_KEY, "")))
                editTrip.setVisibility(View.VISIBLE);

            initDifficulty();

            editTrip.setOnClickListener(v -> Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(TripInfoFragmentDirections.actionTripInfoToEditTrip(tripInfo.getTripId())));
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
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(getSettingsActionId());
        } else {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(TripInfoFragmentDirections.actionTripInfoToUserInfo(uid));
        }
    }

    private void startNavigation(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + tripInfo.getLocationLat() + "," + tripInfo.getLocationLon()));
        startActivity(intent);
    }
}
