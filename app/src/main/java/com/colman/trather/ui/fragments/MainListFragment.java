package com.colman.trather.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.colman.trather.R;
import com.colman.trather.models.SortLocation;
import com.colman.trather.ui.adapters.TripRecyclerViewAdapter;
import com.colman.trather.viewModels.TripViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;

public class MainListFragment extends BaseToolbarFragment implements TripRecyclerViewAdapter.ItemClickListener {
    private TripRecyclerViewAdapter mAdapter;
    private FusedLocationProviderClient mFusedLocationClient;
    private TripViewModel tripViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
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

        FloatingActionButton mapButton = view.findViewById(R.id.map_button);
        mapButton.setOnClickListener(v -> gotoMapMode());

        FloatingActionButton addTripButton = view.findViewById(R.id.add_trip_button);
        addTripButton.setOnClickListener(v -> goToAddTrip());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(() -> tripViewModel.reloadTrips());
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.trip_list_menu, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.applyFilter(query);
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mAdapter.applyFilter(s);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected boolean shouldAddSettingIcon() {
        return false;
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
            swipeRefreshLayout.setRefreshing(false);
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            final FragmentActivity activity = requireActivity();
            mFusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
                if (location != null) {
                    GeoPoint mMyLocation = new GeoPoint(32.6, 32.6);
                    tripList.sort(new SortLocation(mMyLocation));
                }
                activity.runOnUiThread(() -> mAdapter.setItems(tripList));
            }).addOnFailureListener(e -> activity.runOnUiThread(() -> mAdapter.setItems(tripList))
            ).addOnCanceledListener(() -> activity.runOnUiThread(() -> mAdapter.setItems(tripList)));
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(MainListFragmentDirections.listToTripInfo(mAdapter.getItem(position).getTripId()));
    }

    public void gotoMapMode() {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(MainListFragmentDirections.listToMapsFragment(false));
    }

    private void goToAddTrip() {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(MainListFragmentDirections.listToAddTrip(null));
    }
}
