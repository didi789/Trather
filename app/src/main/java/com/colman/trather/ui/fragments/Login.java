package com.colman.trather.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.colman.trather.R;
import com.colman.trather.viewModels.LoginViewModel;
import com.google.firebase.auth.FirebaseUser;

public class Login extends Fragment {

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (loginViewModel.isLoggedIn()) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.main_screen_list);
        }

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        final Button loginButton = view.findViewById(R.id.login);
        final ProgressBar loadingProgressBar = view.findViewById(R.id.loading);

        loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            loadingProgressBar.setVisibility(View.GONE);

            if (firebaseUser != null) {
                updateUiWithUser(firebaseUser);
            }
        });

        loginButton.setOnClickListener(v -> {
            startActivity(loginViewModel.login());

            loadingProgressBar.setVisibility(View.VISIBLE);
        });

        return view;
    }

    private void updateUiWithUser(FirebaseUser firebaseUser) {
        String welcome = getString(R.string.welcome) + firebaseUser.getDisplayName();
        Toast.makeText(getContext(), welcome, Toast.LENGTH_LONG).show();
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.main_screen_list);
    }
}
