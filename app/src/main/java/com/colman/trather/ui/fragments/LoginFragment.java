package com.colman.trather.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.colman.trather.R;
import com.colman.trather.viewModels.LoginViewModel;

public class LoginFragment extends Fragment {

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
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        if (loginViewModel.isLoggedIn()) {
            goToMain();
            return view;
        }

        final Button loginButton = view.findViewById(R.id.login);
        final ProgressBar loadingProgressBar = view.findViewById(R.id.loading);

        loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            loadingProgressBar.setVisibility(View.GONE);

            if (firebaseUser != null) {
                goToMain();
            }
        });

        loginButton.setOnClickListener(v -> {
            startActivity(loginViewModel.login());

            loadingProgressBar.setVisibility(View.VISIBLE);
        });

        return view;
    }

    private void goToMain() {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(LoginFragmentDirections.actionLogin());
    }
}
