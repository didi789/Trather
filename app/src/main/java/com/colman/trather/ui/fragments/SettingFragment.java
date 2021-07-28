package com.colman.trather.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.colman.trather.R;
import com.colman.trather.viewModels.SettingsViewModel;

public class SettingFragment extends BaseToolbarFragment implements View.OnClickListener {
    public static final int PICK_IMAGE = 1;

    private SettingsViewModel settingsViewModel;
    private ImageView profileImg;
    private EditText fullName;
    private TextView email;
    private EditText bio;
    private ProgressBar progressBar;
    private Button saveBtn;
    private Button logoutBtn;

    @Override
    protected boolean shouldAddSettingIcon() {
        return false;
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.settings;
    }

    @Override
    protected int getSettingsActionId() {
        return R.id.settings_screen;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        progressBar = view.findViewById(R.id.progressbar);
        saveBtn = view.findViewById(R.id.save);
        logoutBtn = view.findViewById(R.id.logout);

        profileImg = view.findViewById(R.id.image);
        fullName = view.findViewById(R.id.full_name);
        bio = view.findViewById(R.id.bio_text);
        email = view.findViewById(R.id.email);

        saveBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        profileImg.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar.setVisibility(View.VISIBLE);
        settingsViewModel.getUserMutableLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                return;
            }

            email.setText(user.getEmail());
            bio.setText(user.getBio() == null ? "" : user.getBio());
            fullName.setText(user.getFullname() == null ? "" : user.getFullname());
            final String imageUrl = user.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty())
                Glide.with(requireActivity()).load(imageUrl).error(R.mipmap.ic_launcher).into(profileImg);

            progressBar.setVisibility(View.GONE);
        });

        settingsViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            logoutBtn.setEnabled(!loading);
            saveBtn.setEnabled(!loading);
            profileImg.setEnabled(!loading);

            if (loading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image:
                chooseNewPicture();
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.save:
                settingsViewModel.saveClicked(getViewLifecycleOwner(), fullName.getText().toString(), bio.getText().toString());
                requireActivity().onBackPressed();
                break;
        }
    }

    public void logout() {
        settingsViewModel.logout();
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(SettingFragmentDirections.actionSettingsToLogOut());
    }

    private void chooseNewPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null) {
            settingsViewModel.updateProfileImage(getViewLifecycleOwner(), data.getData());
        }
    }
}
