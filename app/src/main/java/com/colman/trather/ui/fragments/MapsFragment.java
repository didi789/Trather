package com.colman.trather.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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

import com.colman.trather.Consts;
import com.colman.trather.R;
import com.colman.trather.models.Trip;
import com.colman.trather.models.SortLocation;
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
import java.util.Collections;
import java.util.List;

public class MapsFragment extends BaseToolbarFragment implements GoogleMap.OnMarkerClickListener {
    private static final int REQUEST_CODE = 101;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;
    private TripViewModel tripViewModel;
    private List<MarkerOptions> markersList;
    private List<Marker> regularMarkerslist;
    private Marker marker;
    private List<Trip> tripList;
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            googleMap.setOnMarkerClickListener(MapsFragment.this::onMarkerClick);

            MarkerOptions myLocation;
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            myLocation = new MarkerOptions().position(latLng).title("I am here").icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location_icon));
            marker = googleMap.addMarker(myLocation);

            regularMarkerslist = new ArrayList<>();
            Marker tempMarker;
            for (int i = 0; i < markersList.size(); i++) {
                tempMarker = googleMap.addMarker(markersList.get(i));
                tempMarker.setTag(tripList.get(i));
                regularMarkerslist.add(tempMarker);
            }

            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //  Log.d("tag","onCreate");
        super.onCreate(savedInstanceState);
        tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);
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
        toList.setOnClickListener(view1 -> gotoListMode());
        return view;
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.map;
    }

    @Override
    protected int getActionId() {
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
            //factory.setBackground(Drawable.);
            //factory.setContentView(R.drawable.my_location_icon);
            factory.setColor(Color.CYAN);
            Bitmap icon = factory.makeIcon();
            newMarker = new MarkerOptions().position(newLatLng).title(tripList.get(i).getName()).snippet(tripList.get(i).getAbout()).icon(BitmapDescriptorFactory.fromBitmap(factory.makeIcon(tripList.get(i).getName())));
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
            if (location != null) {
                currentLocation = location;
                Toast.makeText(getContext(), currentLocation.getLatitude() + "," + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                tripViewModel.getTripsLiveData().observe(getViewLifecycleOwner(), tripsList -> {
                    GeoPoint myLocation = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    Collections.sort(tripsList, new SortLocation(myLocation));
                    requireActivity().runOnUiThread(() -> markerListFactory(tripsList));
                });
                SupportMapFragment mapFragment =
                        (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(callback);
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTitle().equals("I am here"))
            return false;
        Log.d("tag", "marker clicked");
        Trip trip = (Trip) marker.getTag();
        Toast.makeText(requireActivity(), trip.getAbout(), Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        bundle.putInt(Consts.BUSINESS_ID, trip.getTripId());
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.trip_info, bundle);
        return false;
    }

    public void gotoListMode() {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_mapsFragment_to_main_screen_list);
    }
}
