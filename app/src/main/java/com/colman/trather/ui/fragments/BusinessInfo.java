package com.colman.trather.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.colman.trather.models.Business;
import com.colman.trather.models.Review;
import com.colman.trather.viewModels.BusinessInfoViewModel;
import com.colman.trather.ui.adapters.BusinessRecyclerViewAdapter;
import com.colman.trather.ui.adapters.ReviewsRecyclerViewAdapter;

public class BusinessInfo extends BaseToolbarFragment implements BusinessRecyclerViewAdapter.ItemClickListener, BusinessRecyclerViewAdapter.ItemDeleteListener, View.OnClickListener {
    private BusinessInfoViewModel businessInfoViewModel;
    private TextView title;
    private TextView address;
    private TextView about;
    private ImageView image;
    private ReviewsRecyclerViewAdapter mAdapter;
    private Business businessInfo = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        businessInfoViewModel = new ViewModelProvider(requireActivity()).get(BusinessInfoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        title = view.findViewById(R.id.title);
        address = view.findViewById(R.id.address);
        about = view.findViewById(R.id.about);
        image = view.findViewById(R.id.icon);
        Button takeNumberBtn = view.findViewById(R.id.takeNumberButton);
        ImageView addReview = view.findViewById(R.id.add_review);

        takeNumberBtn.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            bundle.putInt(Consts.BUSINESS_ID, getArguments().getInt(Consts.BUSINESS_ID));
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_take_number, bundle);
        });

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
                businessInfoViewModel.addReview(businessInfo, review, numStars);
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
        return R.string.business_info;
    }

    @Override
    protected int getActionId() {
        return R.id.business_info_to_settings;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_business_info;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int businessId = getArguments().getInt(Consts.BUSINESS_ID);
        businessInfoViewModel.getBusinessByIdLiveData(businessId).observe(getViewLifecycleOwner(), businessInfo -> {
            if (businessInfo == null) {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_back_to_list);
            }

            this.businessInfo = businessInfo;
            title.setText(businessInfo.getName());
            about.setText(businessInfo.getAbout());
            double locationLat = businessInfo.getLocationLat();
            double locationLon = businessInfo.getLocationLon();
            address.setText(getString(R.string.address_text, Double.toString(locationLat), Double.toString(locationLon)));
            Glide.with(requireActivity()).load(businessInfo.getImgUrl()).error(R.mipmap.ic_launcher).into(image);
        });

        businessInfoViewModel.getReviewsByBusinessIdLiveData(businessId).observe(getViewLifecycleOwner(), reviewList -> {
            mAdapter.setItems(reviewList);
        });
    }

    @Override
    public void onItemClick(View view, int position) {
    }

    @Override
    public void onItemDeleteClick(View view, int position) {
        final Review review = mAdapter.getItem(position);
        businessInfoViewModel.deleteReview(review);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_number:
                break;
            default:
                break;
        }
    }
}
