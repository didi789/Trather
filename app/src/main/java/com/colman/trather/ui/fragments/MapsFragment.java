package com.colman.trather.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.colman.trather.R;
import com.colman.trather.models.SortLocation;
import com.colman.trather.models.Trip;
import com.colman.trather.viewModels.AddTripViewModel;
import com.colman.trather.viewModels.TripViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends BaseToolbarFragment implements GoogleMap.OnMarkerClickListener {
    private static final int REQUEST_CODE = 101;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    private TripViewModel tripViewModel;
    private AddTripViewModel addTripViewModel;
    private List<MarkerOptions> markersList;
    private List<Trip> tripList;
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            googleMap.setOnMarkerClickListener(MapsFragment.this);
            googleMap.setOnMapClickListener(latLng -> {
                if (addTripViewModel != null) {
                    addTripViewModel.selectLocation(new GeoPoint(latLng.latitude, latLng.longitude));
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigateUp();
                }
            });

            if (currentLocation != null) {
                LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            }

            for (int i = 0; i < markersList.size(); i++) {
                Marker tempMarker = googleMap.addMarker(markersList.get(i));
                Objects.requireNonNull(tempMarker).setTag(tripList.get(i));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);


        if (getArguments() != null && MapsFragmentArgs.fromBundle(getArguments()).getPickLocation())
            addTripViewModel = new ViewModelProvider(requireActivity()).get(AddTripViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        fetchLastLocationAndFillMarkers();

        FloatingActionButton toList = view.findViewById(R.id.gotoList);
        if (addTripViewModel == null) {
            toList.setOnClickListener(view1 -> gotoListMode());
        } else {
            toList.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.map;
    }

    @Override
    protected int getSettingsActionId() {
        return R.id.map_to_settings;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_maps;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void markerListFactory(List<Trip> tripList) {
        this.tripList = new ArrayList<>();
        this.tripList.addAll(tripList);
        MarkerOptions newMarker;
        LatLng newLatLng;
        markersList = new ArrayList<>();
        for (int i = 0; i < tripList.size(); i++) {
            newLatLng = new LatLng(tripList.get(i).getLocationLat(), tripList.get(i).getLocationLon());
            IconGenerator factory = new IconGenerator(requireActivity());
            factory.setColor(Color.CYAN);
            newMarker = new MarkerOptions().position(newLatLng).title(tripList.get(i).getTitle()).snippet(tripList.get(i).getAbout().length() > 10 ? tripList.get(i).getAbout().substring(0, 10).trim() : tripList.get(i).getAbout() + getString(R.string.dots)).icon(BitmapDescriptorFactory.fromBitmap(factory.makeIcon(tripList.get(i).getTitle())));
            markersList.add(newMarker);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocationAndFillMarkers();
                }
                break;
        }
    }

    private void fetchLastLocationAndFillMarkers() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            tripViewModel.getTripsLiveData().observe(getViewLifecycleOwner(), tripsList -> {
                if (location != null) {
                    currentLocation = location;
                    GeoPoint myLocation = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    tripsList.sort(new SortLocation(myLocation));
                }

                requireActivity().runOnUiThread(() -> markerListFactory(tripsList));
            });

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(callback);
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (addTripViewModel != null)
            return false;
        Trip trip = (Trip) marker.getTag();
        Toast.makeText(requireActivity(), Objects.requireNonNull(trip).getAbout(), Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(MapsFragmentDirections.actionMapsFragmentToTripInfo(trip.getTripId()));
        return false;
    }

    public void gotoListMode() {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(MapsFragmentDirections.actionMapsFragmentToMainScreenList());
    }
}
