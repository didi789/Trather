package com.colman.trather.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
import com.colman.trather.ui.adapters.TripRecyclerViewAdapter;
import com.colman.trather.viewModels.TripViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;

public class MainList extends BaseToolbarFragment implements TripRecyclerViewAdapter.ItemClickListener {
    private TripRecyclerViewAdapter mAdapter;
    private FusedLocationProviderClient mFusedLocationClient;
    private TripViewModel tripViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        final RecyclerView recyclerView = Objects.requireNonNull(view).findViewById(R.id.tripsRv);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new TripRecyclerViewAdapter(requireContext());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.map_button);
        floatingActionButton.setOnClickListener(v -> gotoMapMode());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        return view;
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.list;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tripViewModel.getTripsLiveData().observe(getViewLifecycleOwner(), tripList -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            final FragmentActivity activity = requireActivity();
            mFusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
                if (location == null) {
                    GeoPoint mMyLocation = new GeoPoint(32.6, 32.6);
                    tripList.sort(new SortLocation(mMyLocation));
                    activity.runOnUiThread(() -> mAdapter.setItems(tripList));
                }
            }).addOnFailureListener(e -> activity.runOnUiThread(() -> mAdapter.setItems(tripList))
            ).addOnCanceledListener(() -> activity.runOnUiThread(() -> mAdapter.setItems(tripList)));
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(Consts.BUSINESS_ID, mAdapter.getItem(position).getTripId());
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.trip_info, bundle);
    }

    public void gotoMapMode() {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_main_screen_list_to_mapsFragment);
    }
}
