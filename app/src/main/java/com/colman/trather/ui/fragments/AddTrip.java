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
import android.widget.ProgressBar;
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
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.GeoPoint;

public class AddTrip extends BaseToolbarFragment {
    public static final int PICK_IMAGE = 1;

    private AddTripViewModel addTripViewModel;
    private EditText title;
    private Button address;
    private Button addTrip;
    private EditText about;
    private ImageView image;
    private ImageView imageError;
    private ImageView waterImage;
    private CheckBox isWater;
    private GeoPoint location;
    private Slider level;
    private ProgressBar addTripPB;

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
        imageError = view.findViewById(R.id.imageError);
        isWater = view.findViewById(R.id.water);
        waterImage = view.findViewById(R.id.waterImage);
        addTrip = view.findViewById(R.id.addTrip);
        addTripPB = view.findViewById(R.id.addTripPB);
        level = view.findViewById(R.id.level);
        image.setOnClickListener(v -> pickImageForTrip());

        isWater.setOnCheckedChangeListener((buttonView, isChecked) -> waterImage.setImageResource(isChecked ? R.drawable.yes_water : R.drawable.no_water));
        waterImage.setOnClickListener(v -> isWater.setChecked(!isWater.isChecked()));
        address.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Consts.PICK_LOCATION, true);
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_pick_location, bundle);
        });

        addTrip.setOnClickListener(v -> {
            addTripPB.setVisibility(View.VISIBLE);
            addTrip.setEnabled(false);
            String authorUid = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
            Trip trip = new Trip(location, title.getText().toString(), about.getText().toString(), authorUid, level.getValue(), isWater.isChecked());
            addTripViewModel.addTrip(trip, imageUri, added -> requireActivity().runOnUiThread(() -> {
                addTrip.setEnabled(true);
                addTripPB.setVisibility(View.GONE);
                if (added)
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigateUp();
                else
                    Toast.makeText(getContext(), getString(R.string.error_while_adding_trip), Toast.LENGTH_SHORT).show();
            }));
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
            imageError.setVisibility(state.getImageError() != null ? View.VISIBLE : View.INVISIBLE);
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
        if (requestCode == PICK_IMAGE && data != null) {
            imageUri = data.getData();
            imageError.setVisibility(View.GONE);
            Glide.with(requireActivity()).load(imageUri).into(image);
        }
    }
}
