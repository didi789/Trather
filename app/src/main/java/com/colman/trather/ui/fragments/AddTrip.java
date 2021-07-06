package com.colman.trather.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.colman.trather.Consts;
import com.colman.trather.R;
import com.colman.trather.models.Trip;
import com.colman.trather.services.SharedPref;
import com.colman.trather.services.Utils;
import com.colman.trather.viewModels.AddTripViewModel;
import com.google.firebase.firestore.GeoPoint;

public class AddTrip extends BaseToolbarFragment {
    public static final int PICK_IMAGE = 1;

    private AddTripViewModel addTripViewModel;
    private EditText title;
    private Button address;
    private Button saveTrip;
    private EditText about;
    private ImageView image;
    private CheckBox isWater;
    private NumberPicker levelPicker;
    private GeoPoint location;

    private Uri imageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addTripViewModel = new ViewModelProvider(requireActivity()).get(AddTripViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        title = view.findViewById(R.id.title);
        address = view.findViewById(R.id.address);
        about = view.findViewById(R.id.about);
        image = view.findViewById(R.id.icon);
        isWater = view.findViewById(R.id.water);
        levelPicker = view.findViewById(R.id.level);
        saveTrip = view.findViewById(R.id.saveTrip);

        levelPicker.setMinValue(1);
        levelPicker.setMaxValue(10);
        levelPicker.setValue(5);

        image.setOnClickListener(v -> pickImageForTrip());

        address.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Consts.PICK_LOCATION, true);
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_pick_location, bundle);
        });

        saveTrip.setOnClickListener(v -> {
            String authorUid = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
            Trip trip = new Trip(location, title.getText().toString(), about.getText().toString(), authorUid, levelPicker.getValue(), isWater.isChecked());
            addTripViewModel.addTrip(trip, imageUri);
        });

        addTripViewModel.getSelectedLocation().observe(getViewLifecycleOwner(), point -> {
            location = point;
            address.setText(Utils.getLocationText(point));
        });

        addTripViewModel.getTripState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                return;
            }
            if (state.getTitleError() != null) {
                title.setError(getString(state.getTitleError()));
            }
            if (state.getLocationError() != null) {
                address.setError(getString(state.getLocationError()));
            }
            if (state.getAboutError() != null) {
                about.setError(getString(state.getAboutError()));
            }
            if (state.getImageError() != null) {
                Toast.makeText(requireActivity(), getString(state.getImageError()), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void pickImageForTrip() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture for trip"), PICK_IMAGE);
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.add_trip;
    }

    @Override
    protected int getActionId() {
        return R.id.add_trip_to_settings;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_add_trip;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            Glide.with(requireActivity()).load(imageUri).into(image);
        }
    }
}
