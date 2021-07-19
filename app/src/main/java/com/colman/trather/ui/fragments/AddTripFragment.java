package com.colman.trather.ui.fragments;

import android.app.Dialog;
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

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddTripFragment extends BaseToolbarFragment {
    public static final int PICK_IMAGE = 1;

    private AddTripViewModel addTripViewModel;
    private EditText title;
    private EditText tripSiteUrl;
    private Button address;
    private Button addTrip;
    private Button deleteTrip;
    private EditText about;
    private ImageView image;
    private ImageView imageError;
    private ImageView waterImage;
    private CheckBox isWater;
    private GeoPoint location;
    private Slider level;
    private ProgressBar addTripPB;

    private Uri imageUri;

    private Trip editedTrip;
    private boolean isImgEdited;

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
        tripSiteUrl = view.findViewById(R.id.trip_site_url);
        address = view.findViewById(R.id.address);
        about = view.findViewById(R.id.about);
        image = view.findViewById(R.id.icon);
        imageError = view.findViewById(R.id.imageError);
        isWater = view.findViewById(R.id.water);
        waterImage = view.findViewById(R.id.waterImage);
        addTrip = view.findViewById(R.id.addTrip);
        deleteTrip = view.findViewById(R.id.deleteTrip);
        addTripPB = view.findViewById(R.id.addTripPB);
        level = view.findViewById(R.id.level);
        image.setOnClickListener(v -> pickImageForTrip());

        isWater.setOnCheckedChangeListener((buttonView, isChecked) -> waterImage.setImageResource(isChecked ? R.drawable.yes_water : R.drawable.no_water));
        waterImage.setOnClickListener(v -> isWater.setChecked(!isWater.isChecked()));
        address.setOnClickListener(v -> Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(AddTripFragmentDirections.actionPickLocation(true)));

        deleteTrip.setOnClickListener(v -> new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.delete_trip))
                .setContentText(getString(R.string.sure_delete_trip))
                .setCancelButton(getString(R.string.dialog_cancel), Dialog::dismiss)
                .setConfirmButton(getString(R.string.delete_trip), sweetAlertDialog -> addTripViewModel.deleteTrip(editedTrip, success -> requireActivity().runOnUiThread(() -> {
                    sweetAlertDialog.dismissWithAnimation();
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(AddTripFragmentDirections.actionBackToList());
                }))).show());
        addTrip.setOnClickListener(v -> {
            addTripPB.setVisibility(View.VISIBLE);
            addTrip.setEnabled(false);
            String authorUid = SharedPref.getString(Consts.CURRENT_USER_KEY, "");

            if (editedTrip != null) {
                Trip trip = new Trip(editedTrip.getTripId(), location, title.getText().toString(), tripSiteUrl.getText().toString(), about.getText().toString(), authorUid, editedTrip.getRating(), level.getValue(), isWater.isChecked());
                addTripViewModel.editTrip(trip, imageUri, isImgEdited, added -> requireActivity().runOnUiThread(() -> {
                    addTrip.setEnabled(true);
                    addTripPB.setVisibility(View.GONE);
                    if (added)
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigateUp();
                    else
                        Toast.makeText(getContext(), getString(R.string.error_while_adding_trip), Toast.LENGTH_SHORT).show();
                }));
            } else {
                Trip trip = new Trip(location, title.getText().toString(), tripSiteUrl.getText().toString(), about.getText().toString(), authorUid, level.getValue(), isWater.isChecked());
                addTripViewModel.addTrip(trip, imageUri, added -> requireActivity().runOnUiThread(() -> {
                    addTrip.setEnabled(true);
                    addTripPB.setVisibility(View.GONE);
                    if (added)
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigateUp();
                    else
                        Toast.makeText(getContext(), getString(R.string.error_while_adding_trip), Toast.LENGTH_SHORT).show();
                }));
            }
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
            if (state.getSiteUrlError() != null)
                tripSiteUrl.setError(getString(state.getSiteUrlError()));

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

        final String tripId = AddTripFragmentArgs.fromBundle(getArguments()).getTripId();

        if (tripId != null && editedTrip == null) {
            addTrip.setText(R.string.save_edits);
            deleteTrip.setVisibility(View.VISIBLE);
            addTripViewModel.getTripByIdLiveData(tripId).observe(getViewLifecycleOwner(), trip -> {
                if (trip != null) {
                    editedTrip = trip;

                    title.setText(editedTrip.getTitle());
                    tripSiteUrl.setText(editedTrip.getTripSiteUrl());
                    about.setText(editedTrip.getAbout());
                    level.setValue((float) editedTrip.getLevel());
                    isWater.setChecked(editedTrip.isWater());
                    imageUri = Uri.parse(editedTrip.getImgUrl());
                    location = new GeoPoint(editedTrip.getLocationLat(), editedTrip.getLocationLon());
                    address.setText(Utils.getLocationText(location));
                    Glide.with(requireActivity()).load(imageUri).into(image);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null) {
            imageUri = data.getData();
            imageError.setVisibility(View.GONE);
            isImgEdited = true;
            Glide.with(requireActivity()).load(imageUri).into(image);
        }
    }
}
