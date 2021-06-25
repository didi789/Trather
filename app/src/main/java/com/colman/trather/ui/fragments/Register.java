package com.colman.trather.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.colman.trather.R;
import com.colman.trather.movdelviews.RegisterViewModel;
import com.google.firebase.auth.FirebaseUser;

public class Register extends Fragment {
    public static final String TAG = "Register";
    private RegisterViewModel registerViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerViewModel = new ViewModelProvider(requireActivity()).get(RegisterViewModel.class);

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
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        final EditText emailEditText = view.findViewById(R.id.username);
        final EditText passwordEditText = view.findViewById(R.id.password);
        final EditText fullNameEditText = view.findViewById(R.id.full_name);
        final Button registerButton = view.findViewById(R.id.register);
        final Button loginButton = view.findViewById(R.id.login);
        final ProgressBar loadingProgressBar = view.findViewById(R.id.loading);

        registerViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            registerButton.setEnabled(loginFormState.isDataValid());

            if (loginFormState.getUsernameError() != null) {
                emailEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getFullNameError() != null) {
                fullNameEditText.setError(getString(loginFormState.getFullNameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        registerViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            loadingProgressBar.setVisibility(View.GONE);

            if (firebaseUser == null) {
                showRegisterFailed(getString(R.string.register_failed));
            } else {
                updateUiWithUser(firebaseUser);
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(emailEditText.getText().toString(),
                        passwordEditText.getText().toString(), fullNameEditText.getText().toString());
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        fullNameEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registerViewModel.register(emailEditText.getText().toString(),
                        passwordEditText.getText().toString(), fullNameEditText.getText().toString());
            }
            return false;
        });

        registerButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            registerViewModel.register(emailEditText.getText().toString(),
                    passwordEditText.getText().toString(), fullNameEditText.getText().toString());
        });

        loginButton.setOnClickListener(v -> {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_login);
        });


        return view;
    }

    private void updateUiWithUser(FirebaseUser firebaseUser) {
        String welcome = getString(R.string.welcome) + firebaseUser.getEmail();
        Toast.makeText(getContext(), welcome, Toast.LENGTH_LONG).show();
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.sign_in);
    }

    private void showRegisterFailed(String errorString) {
        Toast.makeText(getContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
