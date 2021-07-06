package com.colman.trather.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.colman.trather.Consts;
import com.colman.trather.R;
import com.colman.trather.viewModels.UserInfoViewModel;

public class UserInfo extends BaseToolbarFragment {

    private UserInfoViewModel userInfoViewModel;
    private ImageView profileImg;
    private TextView fullName;
    private TextView email;
    private TextView bio;

    @Override
    protected boolean shouldAddSettingIcon() {
        return false;
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.user_info;
    }

    @Override
    protected int getActionId() {
        return R.id.settings_screen;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user_info;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userInfoViewModel = new ViewModelProvider(requireActivity()).get(UserInfoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        profileImg = view.findViewById(R.id.image);
        fullName = view.findViewById(R.id.full_name);
        bio = view.findViewById(R.id.bio_text);
        email = view.findViewById(R.id.email);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userInfoViewModel.getUserByUid(getArguments().getString(Consts.KEY_AUTHOR_UID)).observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                return;
            }

            email.setText(user.getEmail());
            bio.setText(user.getBio() == null ? "" : user.getBio());
            fullName.setText(user.getFullname() == null ? "" : user.getFullname());
            final String imageUrl = user.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty())
                Glide.with(requireActivity()).load(imageUrl).error(R.mipmap.ic_launcher).into(profileImg);

        });
    }
}
