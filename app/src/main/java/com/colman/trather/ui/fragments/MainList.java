package com.colman.trather.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.colman.trather.Consts;
import com.colman.trather.R;
import com.colman.trather.models.SortLocation;
import com.colman.trather.movdelviews.BusinessViewModel;
import com.colman.trather.ui.adapters.BusinessRecyclerViewAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;

import java.util.Collections;

public class MainList extends BaseToolbarFragment implements BusinessRecyclerViewAdapter.ItemClickListener {
    private BusinessRecyclerViewAdapter mAdapter;
    private FusedLocationProviderClient mFusedLocationClient;
    private BusinessViewModel businessViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        final RecyclerView recyclerView = view.findViewById(R.id.businessesRv);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new BusinessRecyclerViewAdapter(getContext());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.map_button);
        floatingActionButton.setOnClickListener(v -> gotoMapMode());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        return view;
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.map_list;
    }

    @Override
    protected int getActionId() {
        return R.id.list_to_settings;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel.getBusinessesLiveData().observe(getViewLifecycleOwner(), businessList -> {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            final FragmentActivity activity = requireActivity();
            mFusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
                if (location == null) {
                    GeoPoint mMyLocation = new GeoPoint(32.6, 32.6);
                    Collections.sort(businessList, new SortLocation(mMyLocation));
                    activity.runOnUiThread(() -> mAdapter.setItems(businessList));
                }
            }).addOnFailureListener(e -> activity.runOnUiThread(() -> mAdapter.setItems(businessList))
            ).addOnCanceledListener(() -> activity.runOnUiThread(() -> mAdapter.setItems(businessList)));
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(Consts.BUSINESS_ID, mAdapter.getItem(position).getBusinessId());
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.business_info, bundle);
    }

    public void gotoMapMode() {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_main_screen_list_to_mapsFragment);
    }
}
